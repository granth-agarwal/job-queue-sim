package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.database.DatabaseConnection;
import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.jobs.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobStatus;
import com.caeliusconsulting.jobqueuesim.jobs.JobType;
import com.caeliusconsulting.jobqueuesim.jobs.ReportGenerationJob;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of JobRepository.
 *
 * Syllabus topics demonstrated:
 *   - PreparedStatement for all value-parameterised SQL
 *   - executeQuery()  → SELECT
 *   - executeUpdate() → INSERT, UPDATE, DELETE
 *   - execute()       → DDL (CREATE TABLE) — demonstrated in initSchema()
 *   - try-with-resources for Connection, PreparedStatement, ResultSet
 *   - Transactions: setAutoCommit(false) / commit() / rollback()
 *   - SQLException wrapped in DatabaseException (preserves cause)
 *   - Atomic job claiming: UPDATE ... WHERE status = 'QUEUED'
 *
 * All SQL is contained in this class — no SQL leaks into workers or Main.
 */
public class JdbcJobRepository implements JobRepository {

    // ── DDL ──────────────────────────────────────────────────────────────────

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS jobs (" +
            "  id         VARCHAR(100) PRIMARY KEY," +
            "  type       VARCHAR(50)  NOT NULL," +
            "  status     VARCHAR(30)  NOT NULL," +
            "  created_at TIMESTAMP    NOT NULL," +
            "  updated_at TIMESTAMP    NOT NULL" +
            ")";

    // ── DML ──────────────────────────────────────────────────────────────────

    private static final String INSERT_SQL =
            "INSERT INTO jobs (id, type, status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, type, status, created_at, updated_at " +
            "FROM jobs WHERE id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT id, type, status, created_at, updated_at FROM jobs";

    private static final String UPDATE_SQL =
            "UPDATE jobs SET status = ?, updated_at = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM jobs WHERE id = ?";

    /**
     * Atomic claim: only succeeds if the job is still QUEUED.
     * Affected rows == 1 means this worker won the race.
     */
    private static final String CLAIM_SQL =
            "UPDATE jobs SET status = 'PROCESSING', updated_at = ? " +
            "WHERE id = ? AND status = 'QUEUED'";

    // ── Schema init ──────────────────────────────────────────────────────────

    /**
     * Creates the jobs table if it does not already exist.
     *
     * Uses Statement.execute() (not PreparedStatement) to demonstrate DDL
     * execution — DDL has no value parameters so PreparedStatement adds nothing.
     *
     * Syllabus: execute() for DDL
     */
    @Override
    public void initSchema() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement()) {

            // execute() returns true if the result is a ResultSet, false otherwise.
            // For DDL (CREATE TABLE) it always returns false.
            boolean hadResultSet = stmt.execute(CREATE_TABLE_SQL);
            System.out.println("[DB] Schema initialised. execute() returned: " + hadResultSet);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialise schema", e);
        }
    }

    /**
     * Demonstrates execute() explicitly on a SELECT — returns true because
     * SELECT produces a ResultSet.
     *
     * Syllabus: execute() returning true for queries
     */
    @Override
    public void demonstrateExecute() {
        String sql = "SELECT COUNT(*) FROM jobs";
        try (Connection conn  = DatabaseConnection.getConnection();
             Statement  stmt  = conn.createStatement()) {

            // execute() on a SELECT → returns true; result is accessible via getResultSet()
            boolean isResultSet = stmt.execute(sql);
            System.out.println("[DB] execute() on SELECT returned isResultSet=" + isResultSet);

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        System.out.println("[DB] Total jobs in table: " + rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("execute() demo failed", e);
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a job inside an explicit transaction.
     *
     * Demonstrates:
     *   - setAutoCommit(false)
     *   - PreparedStatement + executeUpdate()
     *   - commit() on success
     *   - rollback() on failure
     *
     * Syllabus: Transactions, PreparedStatement, executeUpdate()
     */
    @Override
    public void create(Job job) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // BEGIN TRANSACTION

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                ps.setString(1, job.getJobId());
                ps.setString(2, job.getType().name());
                ps.setString(3, job.getStatus().name());
                ps.setTimestamp(4, Timestamp.valueOf(job.getCreatedAt()));
                ps.setTimestamp(5, Timestamp.valueOf(job.getUpdatedAt()));

                int rowsAffected = ps.executeUpdate();  // INSERT → executeUpdate()
                System.out.println("[DB] Inserted job '" + job.getJobId() +
                                   "' — rows affected: " + rowsAffected);
            }

            conn.commit();  // COMMIT

        } catch (SQLException e) {
            // ROLLBACK on any failure — preserve data integrity
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[DB] Transaction rolled back for job: " + job.getJobId());
                } catch (SQLException rollbackEx) {
                    System.err.println("[DB] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Failed to create job: " + job.getJobId(), e);

        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) { }
            }
        }
    }

    /**
     * Retrieves a single job by its primary key.
     *
     * Syllabus: PreparedStatement, executeQuery(), ResultSet, try-with-resources
     */
    @Override
    public Optional<Job> findById(String id) {
        try (Connection conn        = DatabaseConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {  // SELECT → executeQuery()
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find job by id: " + id, e);
        }
    }

    /**
     * Retrieves all jobs.
     *
     * Syllabus: executeQuery(), ResultSet iteration, ArrayList for results
     */
    @Override
    public List<Job> findAll() {
        List<Job> jobs = new ArrayList<>();

        try (Connection conn      = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs         = ps.executeQuery()) {  // SELECT → executeQuery()

            while (rs.next()) {
                jobs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all jobs", e);
        }
        return jobs;
    }

    /**
     * Updates a job's status and updated_at timestamp.
     *
     * Syllabus: PreparedStatement, executeUpdate() for UPDATE
     */
    @Override
    public void update(Job job) {
        try (Connection conn      = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(   1, job.getStatus().name());
            ps.setTimestamp(2, Timestamp.valueOf(job.getUpdatedAt()));
            ps.setString(   3, job.getJobId());

            ps.executeUpdate();  // UPDATE → executeUpdate()

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update job: " + job.getJobId(), e);
        }
    }

    /**
     * Deletes a job by id.
     *
     * Syllabus: PreparedStatement, executeUpdate() for DELETE
     */
    @Override
    public void delete(String id) {
        try (Connection conn      = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setString(1, id);
            int rowsDeleted = ps.executeUpdate();  // DELETE → executeUpdate()
            System.out.println("[DB] Deleted job '" + id + "' — rows removed: " + rowsDeleted);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete job: " + id, e);
        }
    }

    /**
     * Atomically claims a QUEUED job for a single worker.
     *
     * This prevents two workers from processing the same job when both
     * find it in QUEUED state.
     *
     * Returning true means: this worker claimed it (affected rows == 1).
     * Returning false means: another worker already claimed it (affected rows == 0).
     *
     * Syllabus: Atomic DB operations, race condition mitigation, executeUpdate()
     */
    @Override
    public boolean claimJob(String id) {
        try (Connection conn      = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(CLAIM_SQL)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(   2, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to claim job: " + id, e);
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to a Job instance.
     *
     * Note: reconstructed jobs carry the status from the DB, not the initial QUEUED default.
     */
    private Job mapRow(ResultSet rs) throws SQLException {
        String     id        = rs.getString("id");
        JobType    type      = JobType.valueOf(rs.getString("type"));
        JobStatus  status    = JobStatus.valueOf(rs.getString("status"));
        LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();

        // Reconstruct the appropriate subclass based on type
        Job job = switch (type) {
            case EMAIL     -> new EmailJob(id);
            case REPORT    -> new ReportGenerationJob(id);
            case DATA_SYNC -> new DataSyncJob(id);
            case UNKNOWN   -> new DataSyncJob(id); // fallback
        };

        job.setStatus(status);
        return job;
    }
}

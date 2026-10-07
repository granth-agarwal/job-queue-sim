package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class JdbcJobRepository implements JobRepository {
    private static final String INSERT_SQL = """
            INSERT INTO jobs (id, type, status, attempt_count, max_attempts,
                              error_message, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String CLAIM_SQL = """
            UPDATE jobs SET status = 'PROCESSING', attempt_count = ?, error_message = NULL, updated_at = ?
            WHERE id = ? AND status = 'QUEUED' AND attempt_count = ? AND attempt_count < max_attempts
            """;
    private static final String UPDATE_SQL = """
            UPDATE jobs SET status = ?, error_message = ?, updated_at = ?
            WHERE id = ? AND status = 'PROCESSING' AND attempt_count = ?
            """;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public JdbcJobRepository() {
        this.dbUrl = requiredEnvironment("DB_URL");
        this.dbUser = requiredEnvironment("DB_USER");
        this.dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");
    }

    public void initSchema() {
        String schema;
        try (InputStream input = JdbcJobRepository.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                throw new DatabaseException("Missing schema.sql resource");
            }
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseException("Cannot read database schema", e);
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(schema)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot initialize jobs table", e);
        }
    }

    @Override
    public void create(Job job) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, job.getJobId());
            statement.setString(2, job.getType().name());
            statement.setString(3, job.getStatus().name());
            statement.setInt(4, job.getAttemptCount());
            statement.setInt(5, job.getMaxAttempts());
            statement.setString(6, job.getErrorMessage());
            statement.setTimestamp(7, Timestamp.valueOf(job.getCreatedAt()));
            statement.setTimestamp(8, Timestamp.valueOf(job.getUpdatedAt()));
            requireOneRow(statement.executeUpdate(), job);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot persist job " + job.getJobId(), e);
        }
    }

    @Override
    public boolean claim(Job job) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(CLAIM_SQL)) {
            statement.setInt(1, job.getAttemptCount());
            statement.setTimestamp(2, Timestamp.valueOf(job.getUpdatedAt()));
            statement.setString(3, job.getJobId());
            statement.setInt(4, job.getAttemptCount() - 1);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot claim job " + job.getJobId(), e);
        }
    }

    @Override
    public void update(Job job) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, job.getStatus().name());
            statement.setString(2, job.getErrorMessage());
            statement.setTimestamp(3, Timestamp.valueOf(job.getUpdatedAt()));
            statement.setString(4, job.getJobId());
            statement.setInt(5, job.getAttemptCount());
            requireOneRow(statement.executeUpdate(), job);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot update job " + job.getJobId(), e);
        }
    }

    @Override
    public JobStatus findStatus(String jobId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status FROM jobs WHERE id = ?")) {
            statement.setString(1, jobId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new DatabaseException("Job not found: " + jobId);
                }
                return JobStatus.valueOf(result.getString("status"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Cannot read job " + jobId, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }
        return value;
    }

    private static void requireOneRow(int affectedRows, Job job) {
        if (affectedRows != 1) {
            throw new DatabaseException("Unexpected persisted state for job " + job.getJobId());
        }
    }
}

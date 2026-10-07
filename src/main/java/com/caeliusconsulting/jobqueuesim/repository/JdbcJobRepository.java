package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.domain.ExecutionSummary;
import com.caeliusconsulting.jobqueuesim.domain.Job;
import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private final DataSource dataSource;

    public JdbcJobRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(schema)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Cannot initialize jobs table", e);
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id, type, status, attempt_count, max_attempts, error_message, created_at, updated_at
                     FROM jobs WHERE 1 = 0
                     """);
             ResultSet result = statement.executeQuery()) {
            result.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException("Incompatible jobs table; apply db/migrate-v1.sql when upgrading", e);
        }
    }

    @Override
    public void create(Job job) {
        try (Connection connection = dataSource.getConnection();
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
        try (Connection connection = dataSource.getConnection();
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
        try (Connection connection = dataSource.getConnection();
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
    public ExecutionSummary summarize(List<String> jobIds) {
        if (jobIds.isEmpty()) {
            return new ExecutionSummary(0, 0, 0, 0, 0);
        }
        String placeholders = String.join(",", Collections.nCopies(jobIds.size(), "?"));
        String sql = """
                SELECT COUNT(*) AS submitted,
                       COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completed,
                       COALESCE(SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failed,
                       COALESCE(SUM(GREATEST(attempt_count - 1, 0)), 0) AS retries,
                       COALESCE(SUM(attempt_count), 0) AS total_attempts
                FROM jobs WHERE id IN (%s)
                """.formatted(placeholders);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < jobIds.size(); index++) {
                statement.setString(index + 1, jobIds.get(index));
            }
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new DatabaseException("Database returned no execution summary");
                }
                ExecutionSummary summary = new ExecutionSummary(result.getInt("submitted"),
                        result.getInt("completed"), result.getInt("failed"),
                        result.getInt("retries"), result.getInt("total_attempts"));
                if (summary.submitted() != jobIds.size()
                        || summary.completed() + summary.failed() != summary.submitted()) {
                    throw new DatabaseException("Batch has missing or unfinished persisted jobs");
                }
                return summary;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Cannot query execution summary", e);
        }
    }

    private static void requireOneRow(int affectedRows, Job job) {
        if (affectedRows != 1) {
            throw new DatabaseException("Unexpected persisted state for job " + job.getJobId());
        }
    }
}

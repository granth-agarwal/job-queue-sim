package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.jobs.Job;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Job persistence.
 *
 * Keeps the worker layer decoupled from JDBC implementation details.
 * Only the concrete JdbcJobRepository knows about SQL.
 *
 * Syllabus: Interfaces, abstraction, repository pattern
 */
public interface JobRepository {

    /**
     * Initialises the database schema (CREATE TABLE IF NOT EXISTS).
     * Safe to call on every startup.
     */
    void initSchema();

    /** Persists a new Job with status QUEUED. */
    void create(Job job);

    /** Retrieves a job by its primary key. */
    Optional<Job> findById(String id);

    /** Retrieves all jobs currently in the table. */
    List<Job> findAll();

    /** Updates an existing job's status and updated_at timestamp. */
    void update(Job job);

    /** Deletes a job by id. */
    void delete(String id);

    /**
     * Atomically claims a QUEUED job for processing.
     *
     * Executes:
     *   UPDATE jobs SET status = 'PROCESSING' WHERE id = ? AND status = 'QUEUED'
     *
     * @return true if this worker successfully claimed the job (affected rows == 1),
     *         false if another worker already claimed it
     */
    boolean claimJob(String id);

    /**
     * Demonstrates execute() usage as required by the syllabus.
     * Runs a raw DDL statement that is not value-parameterised.
     */
    void demonstrateExecute();
}

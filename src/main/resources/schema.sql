-- =============================================================================
-- Job Queue Simulator — SQL Learning File
-- =============================================================================
-- All examples use the actual `jobs` table defined in this project.
-- This file covers: CREATE, INSERT, SELECT, WHERE, ORDER BY, COUNT,
--                   GROUP BY, UPDATE, DELETE
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- DDL: CREATE TABLE
-- ─────────────────────────────────────────────────────────────────────────────

-- Creates the jobs table if it does not already exist.
-- In Java: demonstrated via Statement.execute() in JdbcJobRepository.initSchema()

CREATE TABLE IF NOT EXISTS jobs (
    id         VARCHAR(100) PRIMARY KEY,   -- unique job identifier
    type       VARCHAR(50)  NOT NULL,       -- EMAIL | REPORT | DATA_SYNC | UNKNOWN
    status     VARCHAR(30)  NOT NULL,       -- QUEUED | PROCESSING | COMPLETED | FAILED
    created_at TIMESTAMP    NOT NULL,       -- when the job was first created
    updated_at TIMESTAMP    NOT NULL        -- when status was last changed
);


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: INSERT
-- ─────────────────────────────────────────────────────────────────────────────

-- In Java: PreparedStatement + executeUpdate()
-- The Java code uses a transaction (setAutoCommit(false) / commit / rollback)

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('email-1', 'EMAIL', 'QUEUED', NOW(), NOW());

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('report-1', 'REPORT', 'QUEUED', NOW(), NOW());

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('sync-1', 'DATA_SYNC', 'QUEUED', NOW(), NOW());

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('sync-fail-1', 'DATA_SYNC', 'QUEUED', NOW(), NOW());


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: SELECT — basic retrieval
-- ─────────────────────────────────────────────────────────────────────────────

-- Retrieve all columns for all jobs
-- In Java: PreparedStatement + executeQuery() → ResultSet
SELECT * FROM jobs;

-- Retrieve specific columns
SELECT id, type, status FROM jobs;


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: SELECT with WHERE — filtering rows
-- ─────────────────────────────────────────────────────────────────────────────

-- All QUEUED jobs
SELECT * FROM jobs
WHERE status = 'QUEUED';

-- All EMAIL jobs
SELECT * FROM jobs
WHERE type = 'EMAIL';

-- Jobs that have failed
SELECT * FROM jobs
WHERE status = 'FAILED';

-- A specific job by primary key
SELECT * FROM jobs
WHERE id = 'email-1';

-- Jobs created in the last hour
SELECT * FROM jobs
WHERE created_at >= NOW() - INTERVAL 1 HOUR;


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: SELECT with ORDER BY — sorting results
-- ─────────────────────────────────────────────────────────────────────────────

-- Most recently updated jobs first
SELECT id, type, status, updated_at
FROM jobs
ORDER BY updated_at DESC;

-- Alphabetical by job id
SELECT id, type, status
FROM jobs
ORDER BY id ASC;

-- Sort by type, then by created_at descending
SELECT id, type, status, created_at
FROM jobs
ORDER BY type ASC, created_at DESC;


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: SELECT with COUNT — aggregate function
-- ─────────────────────────────────────────────────────────────────────────────

-- Total number of jobs in the table
-- In Java: Statement.execute() demo in JdbcJobRepository.demonstrateExecute()
SELECT COUNT(*) AS total_jobs
FROM jobs;

-- Count of COMPLETED jobs
SELECT COUNT(*) AS completed_count
FROM jobs
WHERE status = 'COMPLETED';

-- Count of FAILED jobs
SELECT COUNT(*) AS failed_count
FROM jobs
WHERE status = 'FAILED';


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: SELECT with GROUP BY — aggregate grouping
-- ─────────────────────────────────────────────────────────────────────────────

-- Number of jobs per status
SELECT status, COUNT(*) AS count
FROM jobs
GROUP BY status;

-- Number of jobs per type
SELECT type, COUNT(*) AS count
FROM jobs
GROUP BY type;

-- Number of jobs per type, per status — combined grouping
SELECT type, status, COUNT(*) AS count
FROM jobs
GROUP BY type, status
ORDER BY type, status;

-- Types that have more than 1 job (HAVING filters on aggregate)
SELECT type, COUNT(*) AS count
FROM jobs
GROUP BY type
HAVING COUNT(*) > 1;


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: UPDATE — modifying rows
-- ─────────────────────────────────────────────────────────────────────────────

-- Mark a job as COMPLETED
-- In Java: PreparedStatement + executeUpdate()
UPDATE jobs
SET status     = 'COMPLETED',
    updated_at = NOW()
WHERE id = 'email-1';

-- Mark a job as FAILED
UPDATE jobs
SET status     = 'FAILED',
    updated_at = NOW()
WHERE id = 'sync-fail-1';

-- Atomic job claim — used in JdbcJobRepository.claimJob()
-- Only updates if status is still QUEUED (prevents double-processing)
UPDATE jobs
SET status     = 'PROCESSING',
    updated_at = NOW()
WHERE id = 'report-1'
  AND status = 'QUEUED';
-- Check affected rows == 1 → successful claim
-- Check affected rows == 0 → already claimed by another worker


-- ─────────────────────────────────────────────────────────────────────────────
-- DML: DELETE — removing rows
-- ─────────────────────────────────────────────────────────────────────────────

-- Delete a specific job
-- In Java: PreparedStatement + executeUpdate()
DELETE FROM jobs
WHERE id = 'sync-1';

-- Delete all COMPLETED jobs (cleanup)
DELETE FROM jobs
WHERE status = 'COMPLETED';

-- Delete all jobs (dangerous — use with caution)
-- DELETE FROM jobs;


-- ─────────────────────────────────────────────────────────────────────────────
-- Transactions — explicit control
-- ─────────────────────────────────────────────────────────────────────────────
-- In Java: conn.setAutoCommit(false) / conn.commit() / conn.rollback()
-- The JdbcJobRepository.create() method demonstrates this fully.

START TRANSACTION;

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('tx-email-1', 'EMAIL', 'QUEUED', NOW(), NOW());

INSERT INTO jobs (id, type, status, created_at, updated_at)
VALUES ('tx-report-1', 'REPORT', 'QUEUED', NOW(), NOW());

-- If both inserts succeed:
COMMIT;

-- If something goes wrong, roll back all changes:
-- ROLLBACK;

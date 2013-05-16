--
-- Create cache tables for jobs that finished within the last month
-- Creating indexes for the appropriate collumns speeds up queries
--

DROP TABLE IF EXISTS audit_user;

CREATE TABLE audit_user
AS
  SELECT
    user AS user,
    cores AS cores,
    jobtype AS jobtype,
    done AS done,
    executable AS executable,
    IF(done>start, cores*(done-start)/3600,0) AS core_hours,
    IF(start>qtime,(start-qtime)/3600,0) AS waiting_time
  FROM audit
  WHERE done>UNIX_TIMESTAMP(LAST_DAY(NOW() - INTERVAL 1 MONTH));

CREATE INDEX user_index ON audit_user(user);
CREATE INDEX done_index ON audit_user(done);


DROP TABLE IF EXISTS audit_project; 

CREATE TABLE audit_project
AS
  SELECT
    user AS user,
    cores AS cores,
    IF(executable LIKE '%.globus%', 1, 0) AS grid_jobs,
    IF(executable LIKE '%.globus%', IF(DONE>START, CORES*(DONE-START)/3600,0), 0) AS total_grid_core_hours,
    account AS project,
    jobtype AS jobtype,
    done AS done,
    IF(done>start, cores*(done-start)/3600,0) AS core_hours,
    IF(start>qtime,(start-qtime)/3600,0) AS waiting_time
  FROM audit
  WHERE
    account LIKE 'nesi%' AND
    done>UNIX_TIMESTAMP(LAST_DAY(NOW() - INTERVAL 1 MONTH));

CREATE INDEX user_index ON audit_project(user);
CREATE INDEX done_index ON audit_project(done);


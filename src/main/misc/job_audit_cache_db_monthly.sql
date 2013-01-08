--
-- Create statistics cache tables for each user for each month in the past
-- Creating indexes for the appropriate collumns speeds up queries
--

DROP TABLE IF EXISTS audit_user_prev;

CREATE TABLE audit_user_prev
AS
  SELECT
    user AS user,
    count(*) AS jobs,
    SUM(IF(executable LIKE '%.globus%', 1, 0)) AS grid_jobs,
    SUM(cores) AS total_cores,
    TRUNCATE(SUM(IF(done>start, cores*(done-start)/3600,0)),2) AS core_hours,
    TRUNCATE(SUM(IF(start>qtime, (start-qtime)/3600,0)),2) AS waiting_time,
    FROM_UNIXTIME(done, '%m') as month,
    FROM_UNIXTIME(done, '%Y') as year,
    SUM(IF(jobtype='serial',1,0)) AS serial_jobs,
    SUM(IF(jobtype='parallel',1,0)) AS parallel_jobs,
    TRUNCATE(SUM(IF(jobtype='serial' AND done>start, cores*(done-start), 0))/3600,2) AS serial_core_hours,
    TRUNCATE(SUM(IF(jobtype='parallel' AND done>start, cores*(done-start), 0))/3600,2) AS parallel_core_hours,
    TRUNCATE(SUM(IF(done>start, IF(executable LIKE '%.globus%', cores*(done-start), 0),0))/3600, 2) AS total_grid_core_hours
  FROM audit 
  GROUP BY user, year, month;

CREATE INDEX user_index ON audit_user_prev(user);



DROP TABLE IF EXISTS audit_project_prev; 

CREATE TABLE audit_project_prev
AS
  SELECT account AS project,
    user as user,
    COUNT(*) AS jobs,
    SUM(cores) As total_cores,
    SUM(IF(done>start, cores*(done-start)/3600,0)) AS core_hours,
    SUM(IF(start>qtime,(start-qtime)/3600,0)) AS waiting_time,
    FROM_UNIXTIME(done, '%m') as month,
    FROM_UNIXTIME(done, '%Y') as year,
    SUM(IF(jobtype='serial',1,0)) AS serial_jobs,
    SUM(IF(jobtype='parallel',1,0)) AS parallel_jobs,
    TRUNCATE(SUM(IF(jobtype='serial' AND done>start, cores*(done-start), 0))/3600,2) AS serial_core_hours,
    TRUNCATE(SUM(IF(jobtype='parallel' AND done>start, cores*(done-start), 0))/3600,2) AS parallel_core_hours
  FROM audit
  WHERE account LIKE 'nesi%'
  GROUP BY project, user, year, month;

CREATE INDEX user_index ON audit_project_prev(user);


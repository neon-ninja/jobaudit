package eresearch.audit.pojo;

public class UserStatistics {

	private String user;
	private String jobs;
	private String total_cores;
	private String total_core_hours;
	private String total_waiting_time;
	private String average_waiting_time;

	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getJobs() {
		return jobs;
	}
	public void setJobs(String jobs) {
		this.jobs = jobs;
	}
	public String getTotal_cores() {
		return total_cores;
	}
	public void setTotal_cores(String total_cores) {
		this.total_cores = total_cores;
	}
	public String getTotal_core_hours() {
		return total_core_hours;
	}
	public void setTotal_core_hours(String total_core_hours) {
		this.total_core_hours = total_core_hours;
	}
	public String getTotal_waiting_time() {
		return total_waiting_time;
	}
	public void setTotal_waiting_time(String total_waiting_time) {
		this.total_waiting_time = total_waiting_time;
	}
	public String getAverage_waiting_time() {
		return average_waiting_time;
	}
	public void setAverage_waiting_time(String average_waiting_time) {
		this.average_waiting_time = average_waiting_time;
	}
}

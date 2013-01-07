package eresearch.audit.db;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import eresearch.audit.pojo.AuditRecord;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;

public interface AuditRecordDao {

	public Future<List<AuditRecord>> getRecords(String upi, String orderby, String sortorder, long offset, long amount) throws Exception;
	public Future<Integer> getNumberRecords(String user) throws Exception;
	public Future<List<String>> getProjectNames() throws Exception;
	public Future<List<String>> getAuditYears() throws Exception;
	public Future<List<UserStatistics>> getStatisticsForUser(List<String> users, Calendar from, Calendar to) throws Exception;
	public Future<List<UserStatistics>> getStatisticsForProjectSet(List<String> projects, Calendar from, Calendar to) throws Exception;
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForProjectSetCurr(List<String> projects, String bottom, String mid, String top) throws Exception;
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForAllUsers(String bottom, String top) throws Exception;
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForUserSet(List<String> uids, String bottom, String top) throws Exception;
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForUserSetCurr(List<String> uids, String bottom, String mid, String top) throws Exception;
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForProjectSet(List<String> projects, String bottom, String top) throws Exception;

}

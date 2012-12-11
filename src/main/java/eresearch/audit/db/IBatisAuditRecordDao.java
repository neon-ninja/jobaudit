package eresearch.audit.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import eresearch.audit.pojo.AuditRecord;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class IBatisAuditRecordDao extends SqlMapClientDaoSupport implements AuditRecordDao {

	private ExecutorService executorService;
	
	public Future<Integer> getNumberRecords(final String user) throws Exception {
		return this.executorService.submit(
			new Callable<Integer>() {
				public Integer call() throws Exception {
					return (Integer) getSqlMapClientTemplate().queryForObject("getNumberAuditRecordsOfUser", user);
				}
			}
		);
	}

	public Future<List<AuditRecord>> getRecords(final String upi, final String orderby, final String sortorder, final long offset, final long amount) throws Exception {
		return this.executorService.submit(
			new Callable<List<AuditRecord>>() {
				public List<AuditRecord> call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("upi", upi);
					params.put("orderby", orderby);
					params.put("sortorder", sortorder);
					params.put("offset", offset);
					params.put("amount", amount);
					return getSqlMapClientTemplate().queryForList("getAuditRecordsOfUser", params);
				}
			}
		);
	}
	
	public Future<UserStatistics> getStatisticsForUser(final String uid, final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<UserStatistics>() {
				public UserStatistics call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("bottom", bottom);
					params.put("top", top);
					params.put("upi", uid);
					return (UserStatistics) getSqlMapClientTemplate().queryForObject("getStatisticsForUser", params);
				}
			}
		);
	}

	public Future<List<UserStatistics>> getStatisticsForProjectSet(final List<String> projects, final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<List<UserStatistics>>() {
				public List<UserStatistics> call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("bottom", bottom);
					params.put("top", top);
					params.put("projects", projects);
					return getSqlMapClientTemplate().queryForList("getStatisticsForProjectSet", params);
				}
			}
		);
	}

	public Future<BarDiagramStatistics> getBarDiagramStatisticsForAllUsers(final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("bottom", bottom);
					params.put("top", top);
					BarDiagramStatistics bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForAllUsersForInterval", params);
					bds.setBottom(Integer.parseInt(bottom));
					bds.setTop(Integer.parseInt(top));
					return bds;
				}
			}
		);
	}

	public Future<BarDiagramStatistics> getBarDiagramStatisticsForUserSet(final List<String> uids, final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("bottom", bottom);
					params.put("top", top);
					params.put("uids", uids);
					BarDiagramStatistics bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForUserSetForInterval", params);
					bds.setBottom(Integer.parseInt(bottom));
					bds.setTop(Integer.parseInt(top));
					return bds;
				}
			}
		);
	}

	public Future<BarDiagramStatistics> getBarDiagramStatisticsForProjectSet(final List<String> projects, final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("bottom", bottom);
					params.put("top", top);
					params.put("projects", projects);
					BarDiagramStatistics bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForProjectSetForInterval", params);
					bds.setBottom(Integer.parseInt(bottom));
					bds.setTop(Integer.parseInt(top));
					return bds;
				}
			}
		);
	}

	public Future<List<String>> getProjectNames() throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					return getSqlMapClientTemplate().queryForList("getProjectNames");
				}
			}
		);
	}
	
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public Future<List<String>> getAuditYears() throws Exception {
		return this.executorService.submit(new Callable<List<String>>() {
			public List<String> call() throws Exception {
				return getSqlMapClientTemplate().queryForList("getAuditYears");
			}
		});
	}

}

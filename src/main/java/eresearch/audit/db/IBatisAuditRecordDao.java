package eresearch.audit.db;

import java.util.Calendar;
//import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import eresearch.audit.pojo.AuditRecord;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;

import org.apache.log4j.Logger;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class IBatisAuditRecordDao extends SqlMapClientDaoSupport implements AuditRecordDao {

	private ExecutorService executorService;
	Logger log = Logger.getLogger(Thread.currentThread().getClass());
 	
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
	
	public List<UserStatistics> getStatisticsForUser(final List<String> userlist, final Calendar from, final Calendar to) throws Exception {		
		String high = ""+(to.getTimeInMillis()/1000);
		String mid = null;
		
		Calendar now = Calendar.getInstance();
		int currMonth = now.get(Calendar.MONTH);
		int currYear = now.get(Calendar.YEAR);
		String mmFrom=null;
		String mmTo=null;
		int tempFrom;
		int tempTo;
		
		//if the selected date range overlaps with past 24 hours' time span
		if((to.getTimeInMillis())>(System.currentTimeMillis()-86400000)){
			mid=""+((System.currentTimeMillis()-86400000)/1000);

			Calendar newTo = Calendar.getInstance();
			newTo.set(currYear, currMonth-1, 1, 0, 0, 0);
			Calendar curr=Calendar.getInstance();
			curr.set(currYear, currMonth, 1, 0, 0, 0);
			
			mmFrom=""+(from.get(Calendar.MONTH)+1);
			mmTo=""+(newTo.get(Calendar.MONTH)+1);
			
			return getStatisticsForUser(userlist, ""+(curr.getTimeInMillis()/1000), mid, high,""+from.get(Calendar.YEAR)+(mmFrom.length()==1?"0"+mmFrom:mmFrom), ""+newTo.get(Calendar.YEAR)+(mmTo.length()==1?"0"+mmTo:mmTo));
		}
		else{
			tempFrom=(from.get(Calendar.MONTH)+1);
			tempTo=(to.get(Calendar.MONTH));
			mmFrom = tempFrom+"";
			mmTo = tempTo+"";
			return getStatisticsForUser(userlist, ""+from.get(Calendar.YEAR)+(mmFrom.length()==1?"0"+mmFrom:mmFrom), ""+to.get(Calendar.YEAR)+(mmTo.length()==1?"0"+mmTo:mmTo));
		}
	}	

	//get statistics for data older than 24 hours
//rf	public Future<List<UserStatistics>> getStatisticsForUser(final List<String> users, final String bottom, final String top) throws Exception {
	public List<UserStatistics> getStatisticsForUser(final List<String> users, final String bottom, final String top) throws Exception {	
//rf		return this.executorService.submit(
//rf			new Callable<List<UserStatistics>>() {
//rf				public List<UserStatistics> call() throws Exception {
					List<UserStatistics> list = null;
					if (users == null || users.size() == 0) {
						list = new LinkedList<UserStatistics>();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", bottom);
						params.put("top", top);
						params.put("users", users);
						list = (List<UserStatistics>) getSqlMapClientTemplate().queryForList("getStatisticsForUser", params);
					}
					return list;
//rf				}
//rf			}
//rf		);
	}
	
	//get statisctics for past 24 hours' data
//rf	public Future<List<UserStatistics>> getStatisticsForUser(final List<String> users, final String bottom, final String mid, final String top, final String start, final String end) throws Exception {
	public List<UserStatistics> getStatisticsForUser(final List<String> users, final String bottom, final String mid, final String top, final String start, final String end) throws Exception {
//rf	return this.executorService.submit(
//rf			new Callable<List<UserStatistics>>() {
//rf				public List<UserStatistics> call() throws Exception {
					List<UserStatistics> list = null;
					if (users == null || users.size() == 0) {
						list = new LinkedList<UserStatistics>();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
					    params.put("bottom", bottom);
				    	params.put("mid", mid);
					    params.put("top", top);
				    	params.put("users", users);
			    		params.put("start", start);
			    		params.put("end", end);
				    	list = (List<UserStatistics>) getSqlMapClientTemplate().queryForList("getStatisticsForUserLatest", params);
					}
					return list;
//rf				}
//rf			}
//rf		);
	}
	
//rf	public Future<List<UserStatistics>> getStatisticsForProjectSet(final List<String> projects, final Calendar from, final Calendar to) throws Exception {
	public List<UserStatistics> getStatisticsForProjectSet(final List<String> projects, final Calendar from, final Calendar to) throws Exception {
		String high = ""+(to.getTimeInMillis()/1000);
		String mid = null;
		
//		int currMonth = new GregorianCalendar().get(Calendar.MONTH);
//		int currYear = new GregorianCalendar().get(Calendar.YEAR);
		
		Calendar now = Calendar.getInstance();
		int currMonth = now.get(Calendar.MONTH);
		int currYear = now.get(Calendar.YEAR);
		
		String mmFrom=null;
		String mmTo=null;
		
		//if the selected date range overlaps with past 24 hours' time span
		if((to.getTimeInMillis())>(System.currentTimeMillis()-86400000)){
			mid=""+((System.currentTimeMillis()-86400000)/1000);
			
			Calendar newTo = Calendar.getInstance();
			newTo.set(currYear, currMonth-1, 1, 0, 0, 0);
			Calendar curr=Calendar.getInstance();
			curr.set(currYear, currMonth, 1, 0, 0, 0);
			
			mmFrom=""+(from.get(Calendar.MONTH)+1);
			mmTo=""+(newTo.get(Calendar.MONTH)+1);
			//return getStatisticsForProjectSet(projects, ""+(curr.getTimeInMillis()/1000), mid, high,""+from.get(Calendar.YEAR)+(mmFrom.length()==1?"0"+mmFrom:mmFrom), ""+newTo.get(Calendar.YEAR)+(mmTo.length()==1?"0"+mmTo:mmTo));
			return getStatisticsForProjectSet(projects, ""+(curr.getTimeInMillis()/1000), mid, high,""+from.get(Calendar.YEAR)+(mmFrom.length()==1?"0"+mmFrom:mmFrom), ""+newTo.get(Calendar.YEAR)+(mmTo.length()==1?"0"+mmTo:mmTo));			
		}
		else{
			mmFrom=""+(from.get(Calendar.MONTH)+1);
			mmTo=""+(to.get(Calendar.MONTH));
			return getStatisticsForProjectSet(projects, ""+from.get(Calendar.YEAR)+(mmFrom.length()==1?"0"+mmFrom:mmFrom), ""+to.get(Calendar.YEAR)+(mmTo.length()==1?"0"+mmTo:mmTo));
		}
	}	
	
	//get statistics for data older than 24 hours
//rf	public Future<List<UserStatistics>> getStatisticsForProjectSet(final List<String> projects, final String bottom, final String top) throws Exception {
		public List<UserStatistics> getStatisticsForProjectSet(final List<String> projects, final String bottom, final String top) throws Exception {	
//rf		return this.executorService.submit(
//rf			new Callable<List<UserStatistics>>() {
//rf				public List<UserStatistics> call() throws Exception {
					List<UserStatistics> list = null;
					if (projects == null || projects.size() == 0) {
						list = new LinkedList<UserStatistics>();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", bottom);
						params.put("top", top);
						params.put("projects", projects);
						list = (List<UserStatistics>) getSqlMapClientTemplate().queryForList("getStatisticsForProjectSet", params);
					}
					return list;
//rf				}
//rf			}
//rf		);
	}

	//get statistics for past 24 hours' data
//rf	public Future<List<UserStatistics>> getStatisticsForProjectSet(final List<String> projects, final String bottom, final String mid, final String top, final String start, final String end) throws Exception {
	public List<UserStatistics> getStatisticsForProjectSet(final List<String> projects, final String bottom, final String mid, final String top, final String start, final String end) throws Exception {	
//rf		return this.executorService.submit(
//rf			new Callable<List<UserStatistics>>() {
//rf				public List<UserStatistics> call() throws Exception {
					List<UserStatistics> list = null;
					if (projects == null || projects.size() == 0) {
						list = new LinkedList<UserStatistics>();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", bottom);
						params.put("mid", mid);
						params.put("top", top);
						params.put("projects", projects);
						params.put("start", start);
						params.put("end", end);
						list = (List<UserStatistics>) getSqlMapClientTemplate().queryForList("getStatisticsForProjectSetLatest", params);
					}
					return list;
//rf				}
//rf			}
		//);
	}
	
	public List<Future<BarDiagramStatistics>> getBarDiagramUserStatistics(final List<String> userlist,
			Integer startYear, Integer startMonth, 
			Integer endYear, Integer endMonth) throws Exception{
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		
		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
		
//		System.out.println(startYear);
//		System.out.println(startMonth);
//		System.out.println(endYear);
//		System.out.println(endMonth);
		
//		int currMonth = new GregorianCalendar().get(Calendar.MONTH);
//		int currYear = new GregorianCalendar().get(Calendar.YEAR);
		
		int currMonth = from.get(Calendar.MONTH);
		int currYear = from.get(Calendar.YEAR);
		
		//get the bar diagram statistics
		to.set(endYear, endMonth, 1,0,0,0);
		int month = startMonth;
		
		boolean currMonthInRange=false; 
		//if current month lies in the range of the selected time period
		if((to.get(Calendar.YEAR)>currYear) || (to.get(Calendar.MONTH) >= currMonth) && (to.get(Calendar.YEAR) ==currYear))	{
			currMonthInRange=true;
			to.set(currYear, currMonth-1,1,0,0,0);
		}
		
		from.set(startYear, month, 1, 0, 0, 0);
		while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && 
			!(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH))))		
		{
			if (userlist.size() < 1) {
				fbdslist.add(getBarDiagramStatisticsForAllUsers(
						"" + (from.get(Calendar.MONTH) + 1),"" + from.get(Calendar.YEAR)));
			} else {
				fbdslist.add(getBarDiagramStatisticsForUserSet(
						userlist, "" + (from.get(Calendar.MONTH) + 1), ""+ from.get(Calendar.YEAR)));
			}
		    month += 1;
		    from.set(startYear, month, 1, 0, 0, 0);
//		    System.out.println("month:"+month);
		}
		if(currMonthInRange) //get the data for the current month
		{
			from.set(currYear, currMonth, 1, 0, 0, 0);
			long bottom = from.getTimeInMillis()/1000;
            from.set(currYear, currMonth+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    fbdslist.add(getBarDiagramStatisticsForUserSetCurr(userlist,""+bottom ,""+((System.currentTimeMillis()-86400000)/1000), ""+top));
		}
		
		return fbdslist;		
	}

	//get bar diagram statistics for data up to previous month
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
	
	//get bar diagram statistics for current month's data
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForUserSetCurr(final List<String> uids, final String bottom, final String mid, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					BarDiagramStatistics bds = null;
					if (uids == null || uids.size() == 0) {
						bds = new BarDiagramStatistics();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", bottom);
						params.put("top", top);
						params.put("mid", mid);
						params.put("uids", uids);
						bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForUserSetForIntervalCurr", params);
					}
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
					BarDiagramStatistics bds = null;
					if (uids == null || uids.size() == 0) {
						bds = new BarDiagramStatistics();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", (bottom.length()==1?"0"+bottom:bottom));
						params.put("top", (top.length()==1?"0"+top:top));
						params.put("uids", uids);
						bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForUserSetForInterval", params);
					}
					int month = Integer.parseInt(bottom);
					int year = Integer.parseInt(top);
					Calendar cal= Calendar.getInstance();
					cal.set(year, month-1, 1, 0, 0, 0);
					bds.setBottom(Integer.parseInt(""+(cal.getTimeInMillis()/1000)));
					cal.set(year, month, 1, 0, 0, 0);
					bds.setTop(Integer.parseInt(""+(cal.getTimeInMillis()/1000)));
					return bds;
				}
			}
		);
	}
	
	//temp start
	
		public List<Future<BarDiagramStatistics>> getProjectStats(final List<String> projects,
				Integer startYear, Integer startMonth, 
				Integer endYear, Integer endMonth) throws Exception{
			List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();

			Calendar from = Calendar.getInstance();
			Calendar to= Calendar.getInstance();
			int currMonth = from.get(Calendar.MONTH);
			int currYear = from.get(Calendar.YEAR);
			
			//get the bar diagram statistics
			to.set(endYear, endMonth, 1,0,0,0);
			int month = startMonth;
			
			boolean currMonthInRange=false; 
			//if current month lies in the range of the selected time period
			//and set the end month/year to the current month/year
			if((to.get(Calendar.YEAR)>currYear) || (to.get(Calendar.MONTH) >= currMonth) && (to.get(Calendar.YEAR) ==currYear))	{
				currMonthInRange=true;
				to.set(currYear, currMonth-1,1,0,0,0);
			}
			
			//while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && !(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH)))) {
		//	from.set(startYear, month, 1, 0, 0, 0);
			from.set(startYear, month, 1, 0, 0, 0);

			//for time-span (months) before current month
			while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && 
				!(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH))))		
			{
	        	//long bottom = from.getTimeInMillis()/1000;
	            //from.set(startYear, month+1, 1, 0, 0, 0);
			  //  long top = from.getTimeInMillis()/1000;
			    fbdslist.add(getBarDiagramStatisticsForProjectSet(projects, "" + (from.get(Calendar.MONTH) + 1), ""+ from.get(Calendar.YEAR)));
			    month += 1;
			  //  from.set(startYear, month+1, 1, 0, 0, 0);
			    from.set(startYear, month, 1, 0, 0, 0);
			}
			//for current month
			if(currMonthInRange) //get the data for the current month
			{
				from.set(currYear, currMonth, 1, 0, 0, 0);
				long bottom = from.getTimeInMillis()/1000;
	            from.set(currYear, currMonth+1, 1, 0, 0, 0);
			    long top = from.getTimeInMillis()/1000;
			    fbdslist.add(getBarDiagramStatisticsForProjectSetCurr(projects,""+bottom ,""+((System.currentTimeMillis()-86400000)/1000), ""+top));
			}
			
			return fbdslist;
			
		}
//		(final List<String> projects, final String bottom, final String top)
//		(final List<String> projects, final String bottom, final String mid, final String top)
//		currMonthInRange
//		{
//			if()
//		}
	//temp end	
	
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForProjectSet(final List<String> projects, final String bottom, final String top) throws Exception {
		
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					BarDiagramStatistics bds = null;
					if (projects == null || projects.size() == 0) {
						bds = new BarDiagramStatistics();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", (bottom.length()==1?"0"+bottom:bottom));
						params.put("top", (top.length()==1?"0"+top:top));
						params.put("projects", projects);
						bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForProjectSetForInterval", params);
					}
					int month = Integer.parseInt(bottom);
					int year = Integer.parseInt(top);
					Calendar cal= Calendar.getInstance();
					cal.set(year, month-1, 1, 0, 0, 0);
					bds.setBottom(Integer.parseInt(""+(cal.getTimeInMillis()/1000)));
					cal.set(year, month, 1, 0, 0, 0);
					bds.setTop(Integer.parseInt(""+(cal.getTimeInMillis()/1000)));
					return bds;
				}
			}
		);
	}
	
	//get bar diagram statistics for current month's data
	public Future<BarDiagramStatistics> getBarDiagramStatisticsForProjectSetCurr(final List<String> projects, final String bottom, final String mid, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<BarDiagramStatistics>() {
				public BarDiagramStatistics call() throws Exception {
					BarDiagramStatistics bds = null;
					if (projects == null || projects.size() == 0) {
						bds = new BarDiagramStatistics();
					} else {
						Map<String,Object> params = new HashMap<String,Object>();
						params.put("bottom", bottom);
						params.put("top", top);
						params.put("mid", mid);
						params.put("projects", projects);
						bds = (BarDiagramStatistics) getSqlMapClientTemplate().queryForObject("getBarDiagramStatisticsForProjectSetForIntervalCurr", params);
					}
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
	
	
//For Reporting queries	
	
	public String getTotalCoreHoursInterval(String start, String end) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("end", end);
		return (String) getSqlMapClientTemplate().queryForObject("getTotalCoreHoursInterval", params);
	}	
	
}

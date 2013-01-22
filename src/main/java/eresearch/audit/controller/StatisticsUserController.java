package eresearch.audit.controller;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;

public class StatisticsUserController extends StatisticsController {

	static Logger log = Logger.getLogger("StatisticsUserController.class");
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("Inside handleRequestInternal");
		ModelAndView mav = super.handleRequestInternal(request, response);
		List<UserStatistics> userstatslist = new LinkedList<UserStatistics>();
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();

		Map params = request.getParameterMap();
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
		// get statistics for each user
		List<String> userlist = this.createUserList(request);

		//time period related changes
		Future<List<UserStatistics>> uslist;
		
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);

		int currMonth = new GregorianCalendar().get(Calendar.MONTH);
		int currYear = new GregorianCalendar().get(Calendar.YEAR);
		
		uslist=this.auditRecordDao.getStatisticsForUser(userlist, from, to);
		// collect information from futures
		userstatslist=uslist.get();
		
		userlist=new LinkedList<String>();
		for(UserStatistics stats: userstatslist){
			userlist.add(stats.getUser());
		}
		
		//get the bar diagram statistics
		to.set(super.historyEndYear, super.historyEndMonth, 1,0,0,0);
		int month = super.historyStartMonth;
		
		boolean currMonthInRange=false; 
		//if current month lies in the range of the selected time period
		if((to.get(Calendar.YEAR)>currYear) || (to.get(Calendar.MONTH) >= currMonth) && (to.get(Calendar.YEAR) ==currYear))	{
			currMonthInRange=true;
			to.set(currYear, currMonth-1,1,0,0,0);
		}
		
		from.set(this.historyStartYear, month, 1, 0, 0, 0);
		while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && 
			!(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH))))		
		{
			if (userlist.size() < 1) {
				fbdslist.add(auditRecordDao.getBarDiagramStatisticsForAllUsers(
						"" + (from.get(Calendar.MONTH) + 1),"" + from.get(Calendar.YEAR)));
			} else {
				fbdslist.add(auditRecordDao.getBarDiagramStatisticsForUserSet(
						userlist, "" + (from.get(Calendar.MONTH) + 1), ""+ from.get(Calendar.YEAR)));
			}
		    month += 1;
		    from.set(this.historyStartYear, month, 1, 0, 0, 0);
		}
		if(currMonthInRange) //get the data for the current month
		{
			from.set(currYear, currMonth, 1, 0, 0, 0);
			long bottom = from.getTimeInMillis()/1000;
            from.set(currYear, currMonth+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForUserSetCurr(userlist,""+bottom ,""+((System.currentTimeMillis()-86400000)/1000), ""+top));
		}
        for (Future<BarDiagramStatistics> fbds : fbdslist) {
        	bdslist.add(fbds.get());
        }

        mav.addObject("user_statistics", userstatslist);
		mav.addObject("job_statistics", bdslist);
		
		//for retaining dropdown values
		mav.addObject("startYear", historyStartYear);
		mav.addObject("startMonth", historyStartMonth);
		mav.addObject("endYear", historyEndYear);
		mav.addObject("endMonth", historyEndMonth);
		mav.addObject("selectedUser", selectedUser);
		mav.addObject("selectedAffiliation", selectedAffiliation);
	    return mav;
	}

	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		log.info("Inside createUserList");
		
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		String user=((String[]) params.get("user"))[0];
		
		if (params.containsKey("user")) 
		{
			if (user.equalsIgnoreCase("all")) {
				Calendar from = Calendar.getInstance();
				Calendar to= Calendar.getInstance();

				from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
				to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
			
				users.addAll(super.userDao.getUserNames(""+(from.getTimeInMillis()/1000),""+(to.getTimeInMillis()/1000)).get());
			} else {
				users.add(user);
			}
			setSelectedUser(user);
		} 
		else 
		{
			Calendar from = Calendar.getInstance();
			Calendar to= Calendar.getInstance();

			from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
			to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
			// list of all user names
			users.addAll(super.userDao.getUserNames(""+(from.getTimeInMillis()/1000),""+(to.getTimeInMillis()/1000)).get());
		}
		log.info("Returning from createUserList. list size="+users.size());
		return users;
	}
}

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
		
		// get list of users based on the values in user dropdown
		List<String> userlist = this.createUserList(request);

		//time period related changes
//rf		Future<List<UserStatistics>> uslist;
		List<UserStatistics> uslist;
		
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
		
		//get statistics
		uslist=this.auditRecordDao.getStatisticsForUser(userlist, from, to);
		// collect information from futures
//		userstatslist=uslist.get();
		userstatslist=uslist;
		
		userlist=new LinkedList<String>();
		for(UserStatistics stats: userstatslist){
			userlist.add(stats.getUser());
		}
		
		//get bar diagram statistics
		fbdslist = auditRecordDao.getBarDiagramUserStatistics(userlist,
				super.historyStartYear, super.historyStartMonth, 
				super.historyEndYear, super.historyEndMonth);
		
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
		
		if (params.containsKey("user") && !(user.equalsIgnoreCase("all"))) 
		{
			users.add(user);
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
			if(user.equalsIgnoreCase("all"))
			{
				setSelectedUser(user);
			}
		}
		log.info("Returning from createUserList. list size="+users.size());
		return users;
	}
}

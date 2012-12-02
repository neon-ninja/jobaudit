package eresearch.audit.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;

public class StatisticsUserController extends StatisticsController {

	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
    	
		ModelAndView mav = super.handleRequestInternal(request, response);
		List<UserStatistics> userstatslist = new LinkedList<UserStatistics>();
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();
		List<Future<UserStatistics>> uslist = new LinkedList<Future<UserStatistics>>();

		// get statistics for each user
		List<String> userlist = this.createUserList(request);
		for (String user: userlist) {
			uslist.add(this.auditRecordDao.getStatisticsForUser(user, "0", new Long(new Date().getTime()/1000).toString()));
		}
        
		Calendar c = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		int month = super.historyStartMonth - 1;
		c.set(super.historyStartYear, month, 1, 0, 0, 0);
		
		// get data for bar diagrams
        while (c.get(Calendar.YEAR) <= now.get(Calendar.YEAR) && c.get(Calendar.MONTH) <= now.get(Calendar.MONTH)) {
        	long bottom = c.getTimeInMillis()/1000;
            c.set(this.historyStartYear, month+1, 1, 0, 0, 0);
		    long top = c.getTimeInMillis()/1000;
		    if (userlist.size() < 1) {
			    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForAllUsers(Long.toString(bottom), Long.toString(top)));
		    } else {
			    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForUserSet(userlist, Long.toString(bottom), Long.toString(top)));		        	
		    }
		    month += 1;
		    c.set(this.historyStartYear, month, 1, 0, 0, 0);
		}
        
        // collect information from futures
        for (Future<UserStatistics> fus: uslist) {
        	UserStatistics tmp = fus.get();
        	userstatslist.add(tmp);
        }

        for (Future<BarDiagramStatistics> fbds : fbdslist) {
        	bdslist.add(fbds.get());
        }
		mav.addObject("user_statistics", userstatslist);
		mav.addObject("job_statistics", bdslist);
	    return mav;
	}

	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		if (params.containsKey("user")) {
			users.add(((String[]) params.get("user"))[0]);
		} else {
			// list of all user names
			users.addAll(super.userDao.getUserNames().get());
		}
		return users;
	}

}

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

		Map params = request.getParameterMap();
		
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
		// get data for bar diagrams
		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();

		// get statistics for each user
		List<String> userlist = this.createUserList(request);
		//String top = (from.getTimeInMillis()/1000)
		
		//time period related changes		
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
		
		String low =  ""+(from.getTimeInMillis()/1000);
		String high = ""+(to.getTimeInMillis()/1000);
		
		for (String user: userlist) {
			uslist.add(this.auditRecordDao.getStatisticsForUser(user, low, high));
	//		uslist.add(this.auditRecordDao.getStatisticsForUser(user, "0", ""+System.currentTimeMillis()/1000));
		}
		
		to.set(super.historyEndYear, super.historyEndMonth, 1,0,0,0);
		int month = super.historyStartMonth - 1;
		
		while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && !(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH)))) {
        	long bottom = from.getTimeInMillis()/1000;
            from.set(this.historyStartYear, month+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    if (userlist.size() < 1) {
			    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForAllUsers(Long.toString(bottom), Long.toString(top)));
		    } else {
			    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForUserSet(userlist, Long.toString(bottom), Long.toString(top)));		        	
		    }
		    month += 1;
		    from.set(this.historyStartYear, month, 1, 0, 0, 0);
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
		return users;
	}
}

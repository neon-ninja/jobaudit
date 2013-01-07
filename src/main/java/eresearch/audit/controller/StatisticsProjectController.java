package eresearch.audit.controller;

import java.util.Calendar;
import java.util.Date;
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

public class StatisticsProjectController extends StatisticsController {

	static Logger log = Logger.getLogger("StatisticsProjectController.class");
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
    	
		log.info("Inside handleRequestInternal");
		ModelAndView mav = super.handleRequestInternal(request, response);
		
		Map params = request.getParameterMap();
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
		List<String> projects = this.createProjectList(request, mav);
		Future<List<UserStatistics>> fprojectstats;

		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
	
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);		
		
		int currMonth = new GregorianCalendar().get(Calendar.MONTH);
		int currYear = new GregorianCalendar().get(Calendar.YEAR);		

		fprojectstats = this.auditRecordDao.getStatisticsForProjectSet(projects, from, to);
		
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();

		//get the bar diagram statistics
		to.set(super.historyEndYear, super.historyEndMonth, 1,0,0,0);
		int month = super.historyStartMonth;
		
		boolean currMonthInRange=false; 
		//if current month lies in the range of the selected time period
		if((to.get(Calendar.YEAR)>currYear) || (to.get(Calendar.MONTH) >= currMonth) && (to.get(Calendar.YEAR) ==currYear))	{
			currMonthInRange=true;
			to.set(currYear, currMonth-1,1,0,0,0);
		}
		
		//while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && !(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH)))) {
		from.set(this.historyStartYear, month, 1, 0, 0, 0);
		while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && 
			!(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH))))		
		{
        	long bottom = from.getTimeInMillis()/1000;
            from.set(this.historyStartYear, month+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForProjectSet(projects, "" + (from.get(Calendar.MONTH) + 1), ""+ from.get(Calendar.YEAR)));
		    month += 1;
		    from.set(this.historyStartYear, month, 1, 0, 0, 0);
		}
		if(currMonthInRange) //get the data for the current month
		{
			from.set(currYear, currMonth, 1, 0, 0, 0);
			long bottom = from.getTimeInMillis()/1000;
            from.set(currYear, currMonth+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForProjectSetCurr(projects,""+bottom ,""+((System.currentTimeMillis()-86400000)/1000), ""+top));
		}
        for (Future<BarDiagramStatistics> fbds : fbdslist) {
        	bdslist.add(fbds.get());
        }
		mav.addObject("user_statistics", fprojectstats.get());
		mav.addObject("job_statistics", bdslist);
		mav.addObject("startYear", historyStartYear);
		mav.addObject("startMonth", historyStartMonth);
		mav.addObject("endYear", historyEndYear);
		mav.addObject("endMonth", historyEndMonth);
		
		//retaining dropdown selections in UI
		mav.addObject("selectedProject", selectedProject);
		mav.addObject("selectedUser", selectedUser);
		mav.addObject("selectedAffiliation", selectedAffiliation);
		
		log.info("Returning from handleRequestInternal");
	    return mav;
	}

	protected List<String> createProjectList(HttpServletRequest req, ModelAndView mav) throws Exception {
		log.info("Inside createProjectList");
		Map params = req.getParameterMap();
		List<String> projects = new LinkedList<String>();
		String project = ((String[]) params.get("project"))[0];

		if (params.containsKey("project")) {
			if (project.equalsIgnoreCase("all")) {
				projects.addAll(super.auditRecordDao.getProjectNames().get());
			} else {
				projects.add(project);
			}
			setSelectedProject(project);
		} else {
			projects = (List<String>) mav.getModelMap().get("projects");
		}
		
		log.info("Returning from createProjectList. list size="+projects.size());
		return projects;
	}
}

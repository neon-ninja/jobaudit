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

public class StatisticsProjectController extends StatisticsController {

	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
    	
		ModelAndView mav = super.handleRequestInternal(request, response);
		List<String> projects = this.createProjectList(request, mav);
		Future<List<UserStatistics>> fprojectstats;
		
		fprojectstats = this.auditRecordDao.getStatisticsForProjectSet(projects, "0", new Long(new Date().getTime()/1000).toString());
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();
        
		Calendar c = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		int month = super.historyStartMonth - 1;
		c.set(super.historyStartYear, month, 1, 0, 0, 0);
        while (c.get(Calendar.YEAR) <= now.get(Calendar.YEAR) && c.get(Calendar.MONTH) <= now.get(Calendar.MONTH)) {
        	long bottom = c.getTimeInMillis()/1000;
		    month += 1;
            c.set(this.historyStartYear, month, 1, 0, 0, 0);
		    long top = c.getTimeInMillis()/1000;
			fbdslist.add(auditRecordDao.getBarDiagramStatisticsForProjectSet(projects, Long.toString(bottom), Long.toString(top)));		        	
		    c.set(this.historyStartYear, month, 1, 0, 0, 0);
		}
        
        for (Future<BarDiagramStatistics> fbds : fbdslist) {
        	bdslist.add(fbds.get());
        }
		mav.addObject("user_statistics", fprojectstats.get());
		mav.addObject("job_statistics", bdslist);
	    return mav;
	}

	protected List<String> createProjectList(HttpServletRequest req, ModelAndView mav) throws Exception {
		Map params = req.getParameterMap();
		List<String> projects = new LinkedList<String>();
		if (params.containsKey("project")) {
			projects.add(((String[]) params.get("project"))[0]);
		} else {
			projects = (List<String>)mav.getModelMap().get("projects");
		}
		return projects;
	}
	
}

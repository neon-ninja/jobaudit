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

		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
		int month = super.historyStartMonth - 1;
		
		from.set(super.historyStartYear, super.historyStartMonth, 1);
		to.set(super.historyEndYear, super.historyEndMonth, 1);

		while ((from.get(Calendar.YEAR) <= to.get(Calendar.YEAR) && !(from.get(Calendar.YEAR) == to.get(Calendar.YEAR) && from.get(Calendar.MONTH) > to.get(Calendar.MONTH)))) {
        	long bottom = from.getTimeInMillis()/1000;
            from.set(this.historyStartYear, month+1, 1, 0, 0, 0);
		    long top = from.getTimeInMillis()/1000;
		    fbdslist.add(auditRecordDao.getBarDiagramStatisticsForProjectSet(projects, Long.toString(bottom), Long.toString(top)));
		    month += 1;
		    from.set(this.historyStartYear, month, 1, 0, 0, 0);
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

	    return mav;
	}

	protected List<String> createProjectList(HttpServletRequest req, ModelAndView mav) throws Exception {
		Map params = req.getParameterMap();
		List<String> projects = new LinkedList<String>();
		
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
		if (params.containsKey("project")) {
			projects.add(((String[]) params.get("project"))[0]);
			setSelectedProject(((String[]) params.get("project"))[0]);
		} else {
			projects = (List<String>)mav.getModelMap().get("projects");
		}
		return projects;
	}
	
}

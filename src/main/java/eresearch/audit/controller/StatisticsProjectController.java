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
		
		//get the list of projects, depending on the value selected in project dropdown
		List<String> projects = this.createProjectList(request, mav);
		Future<List<UserStatistics>> fprojectstats;

		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
	
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);		

		//get statistics
		fprojectstats = this.auditRecordDao.getStatisticsForProjectSet(projects, from, to);
		
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();
		
		//get bar diagram statistics
		fbdslist= auditRecordDao.getProjectStats( projects,
				super.historyStartYear, super.historyStartMonth, 
				super.historyEndYear, super.historyEndMonth);

		
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

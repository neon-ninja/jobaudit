package eresearch.audit.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.db.UserDao;
import eresearch.audit.pojo.User;

/**
 *  Base class for the specialized statistics controllers
 */
public class StatisticsController extends AbstractController {

	protected UserDao userDao;
	protected AuditRecordDao auditRecordDao;
	protected Integer historyStartYear;
	protected Integer historyStartMonth;
	protected Integer historyEndYear;
	protected Integer historyEndMonth;
	
	//created for retaining the drop-down values
	protected String selectedUser;
	protected String selectedProject;
	protected String selectedAffiliation;

	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("statistics");
		Future<List<User>> fuser = this.userDao.getUsers();
		Future<List<String>> faffil = this.userDao.getAffiliations();
		Future<List<String>> fproys = this.auditRecordDao.getProjectNames();
		//Future<List<String>> fyears = this.auditRecordDao.getAuditYears();
		
		List<String> fyears = new ArrayList<String>();
		for(int year=historyStartYear; year<=historyEndYear;year++)
		{
			fyears.add(""+year);
		}
		
		mav.addObject("users", fuser.get());
		mav.addObject("affiliations", faffil.get());
		mav.addObject("projects", fproys.get());
		
		mav.addObject("years", fyears);

		//for retaining drop-down values in UI
		mav.addObject("startYear", historyStartYear);
		mav.addObject("startMonth", historyStartMonth);
		mav.addObject("endYear", historyEndYear);
		mav.addObject("endMonth", historyEndMonth);

		return mav;
	}

	public void setAuditRecordDao(AuditRecordDao auditRecordDao) {
		this.auditRecordDao = auditRecordDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setHistoryStartYear(Integer historyStartYear) {
		this.historyStartYear = historyStartYear;
	}

	public void setHistoryStartMonth(Integer historyStartMonth) {
		this.historyStartMonth = historyStartMonth;
	}
	

	public void setHistoryEndYear(Integer historyEndYear) {
		this.historyEndYear = historyEndYear;
	}

	public void setHistoryEndMonth(Integer historyEndMonth) {
		this.historyEndMonth = historyEndMonth;
	}

	public String getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(String selectedUser) {
		this.selectedUser = selectedUser;
	}

	public String getSelectedProject() {
		return selectedProject;
	}

	public void setSelectedProject(String selectedProject) {
		this.selectedProject = selectedProject;
	}

	public String getSelectedAffiliation() {
		return selectedAffiliation;
	}

	public void setSelectedAffiliation(String selectedAffiliation) {
		this.selectedAffiliation = selectedAffiliation;
	}	
}

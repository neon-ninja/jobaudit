package eresearch.audit.controller;

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

	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("statistics");
		Future<List<User>> fuser = this.userDao.getUsers();
		Future<List<String>> faffil = this.userDao.getAffiliations();
		Future<List<String>> fproys = this.auditRecordDao.getProjectNames();
		mav.addObject("users", fuser.get());
		mav.addObject("affiliations", faffil.get());
		mav.addObject("projects", fproys.get());
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
	
}

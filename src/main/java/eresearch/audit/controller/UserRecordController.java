package eresearch.audit.controller;

import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.db.UserDao;
import eresearch.audit.pojo.User;

public class UserRecordController extends AbstractController {

	private UserDao userDao;
	private AuditRecordDao auditRecordDao;
	private String maxJobRecordsPerPage;
	
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
    	ModelAndView mav = null;
		String upi = request.getParameter("upi");
		mav = new ModelAndView("userrecords");	    	
		if (upi != null && !upi.trim().equals("")) {
			Future<User> fuser = this.userDao.getUser(upi);
			Future<Integer> fnr = this.auditRecordDao.getNumberRecords(upi);
	        mav.addObject("maxJobRecordsPerPage", this.maxJobRecordsPerPage);
  	        mav.addObject("user", fuser.get());
	        mav.addObject("totalNumberRecords", fnr.get());
		}
		return mav;
	}

	public void setAuditRecordDao(AuditRecordDao auditRecordDao) {
		this.auditRecordDao = auditRecordDao;
	}

	public void setuserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setMaxJobRecordsPerPage(String maxJobRecordsPerPage) {
		this.maxJobRecordsPerPage = maxJobRecordsPerPage;
	}

}

package eresearch.audit.util;

import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.db.UserDao;

public class MyRunnable implements Runnable {

	private UserDao userDao;
	private AuditRecordDao auditRecordDao;
	private ModelAndView mav;
	
	public MyRunnable(UserDao ud, AuditRecordDao ard, ModelAndView mav) {
		this.userDao = ud;
		this.auditRecordDao = ard;
		this.mav = mav;
	}
	
	public void run() {
		
	}
}

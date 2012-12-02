package eresearch.audit.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import eresearch.audit.db.UserDao;

public class UserController extends AbstractController {

	private UserDao userDao;
	
	public ModelAndView handleRequestInternal(HttpServletRequest request,
		HttpServletResponse response) throws Exception {
    	ModelAndView mav = new ModelAndView("users");
		mav.addObject("users", this.userDao.getUsers().get());
		return mav;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}

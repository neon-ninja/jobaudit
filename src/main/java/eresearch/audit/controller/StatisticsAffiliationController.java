package eresearch.audit.controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class StatisticsAffiliationController extends StatisticsUserController {

	static Logger log = Logger.getLogger(Thread.currentThread().getClass());
	
	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		Future<List<String>> fuserlist = null;
		
		// get data for bar diagrams
		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
		
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
		
		if (params.containsKey("affiliation")) {
			String affil = ((String[]) params.get("affiliation"))[0];
			setSelectedAffiliation(affil);
			
			String[] subs = affil.split("/");
			List<String> usersWithAtLeastOneJob = this.userDao.getUserNames(""+(from.getTimeInMillis()/1000),""+(to.getTimeInMillis()/1000)).get();
			if (affil.equals("all")) {
				return usersWithAtLeastOneJob;
			} else {
				if (StringUtils.countMatches(affil, "/") == 1) {
					fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim());
				} else if (StringUtils.countMatches(affil, "/") == 2) {
					fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim(), subs[2].trim());
				} else if (StringUtils.countMatches(affil, "/") == 3) {
					fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim(), subs[2].trim(), subs[3].trim());
				} else {
					throw new Exception("Unexpected affilation string: " + affil);
				}
				List<String> usersForAffil = fuserlist.get();
				for (String u: usersForAffil) {
					if (usersWithAtLeastOneJob.contains(u)) {
						users.add(u);					
					}
				}
			}
		}
		log.info("Returning from createUserList. list size="+users.size());
		return users;
	}
}

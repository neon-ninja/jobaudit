package eresearch.audit.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

public class StatisticsAffiliationController extends StatisticsUserController {

	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		Future<List<String>> fuserlist = null;
		
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
		if (params.containsKey("affiliation")) {
			String affil = ((String[]) params.get("affiliation"))[0];
			setSelectedAffiliation(affil);
			
			String[] subs = affil.split("/");
			List<String> usersWithAtLeastOneJob = this.userDao.getUserNames().get();
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
		return users;
	}
}

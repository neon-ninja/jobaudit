package eresearch.audit.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import eresearch.audit.pojo.Affiliation;
import eresearch.audit.pojo.Department;
import eresearch.audit.pojo.User;
import eresearch.audit.pojo.UserStatistics;
import eresearch.audit.util.UserComparator;

public class IBatisUserDao extends SqlMapClientDaoSupport implements UserDao {

	private ExecutorService executorService;
	Logger log = Logger.getLogger(Thread.currentThread().getClass());

	public Future<User> getUser(final String upi) throws Exception {
		return this.executorService.submit(
			new Callable<User>() {
				public User call() throws Exception {
					return (User) getSqlMapClientTemplate().queryForObject("getUser", upi);
				}
			}
		);
	}
    
	public Future<List<User>> getUsers() throws Exception {
		return this.executorService.submit(
			new Callable<List<User>>() {
				public List<User> call() throws Exception {
					List<String> upis = getSqlMapClientTemplate().queryForList("getUsersWithAtLeastOneJob");
					List<User> users = new LinkedList<User>();
					if (upis != null) {
						for (String upi: upis) {
							User u = (User) getSqlMapClientTemplate().queryForObject("getUser", upi);
							if (u != null) {
								users.add(u);
							}
						}
					}
					Collections.sort(users, new UserComparator());
					return users;
				}
			}
		);
	}

	public Future<List<String>> getUserNames(final String bottom, final String top) throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
//					System.out.println("top:"+top+"bottom:"+bottom);
					Map<String,Object> params=new HashMap<String, Object>();
					params.put("top", top);
					params.put("bottom", bottom);
					return getSqlMapClientTemplate().queryForList("getUsersWithAtLeastOneJobInterval", params);
				}
			}
		);
	}

	public Future<List<String>> getUsersForProject(final String project) throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					return getSqlMapClientTemplate().queryForList("getUsersForProject", project);
				}
			}
		);
	}

	public Future<List<String>> getUsersForAllProjects() throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					return getSqlMapClientTemplate().queryForList("getUsersForAllProjects");
				}
			}
		);
	}

	public Future<List<String>> getUsersForAffiliation(final String code) throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					return getSqlMapClientTemplate().queryForList("getUsersForAffiliationCode", code);
				}
			}
		);
	}

	public Future<List<String>> getUsersForAffiliation(final String code, final String dept1) throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("code", code);
					params.put("dept1", dept1);
					return getSqlMapClientTemplate().queryForList("getUsersForAffiliationCodeAndDept1", params);
				}
			}
		);
	}

	public Future<List<String>> getUsersForAffiliation(final String code, final String dept1, final String dept2) throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					Map<String,Object> params = new HashMap<String,Object>();
					params.put("code", code);
					params.put("dept1", dept1);
					params.put("dept2", dept2);
					return getSqlMapClientTemplate().queryForList("getUsersForAffiliationCodeAndDept1AndDept2", params);
				}
			}
		);
	}

	public Future<List<String>> getAffiliations() throws Exception {
		return this.executorService.submit(
			new Callable<List<String>>() {
				public List<String> call() throws Exception {
					List<Affiliation> tmp = getSqlMapClientTemplate().queryForList("getAffiliations");
					Set <String> affiliations = new HashSet<String>();
					for (Affiliation a: tmp) {
						StringBuffer affil = new StringBuffer("");
						String sep = "/";
						String code = (a.getCode() == null || a.getCode().trim().length() == 0) ? " " : a.getCode();
						String dept1 = (a.getDept1() == null || a.getDept1().trim().length() == 0) ? " " : a.getDept1();
						String dept2 = (a.getDept2() == null || a.getDept2().trim().length() == 0) ? " " : a.getDept2();
						affil.append(sep).append(code).append(sep).append(dept1).append(sep).append(dept2);
						affiliations.add(affil.toString().replaceAll("/ $", "").replaceAll("/ $", ""));
						affiliations.add(new StringBuffer("/").append(code).toString());
						if (dept1 != " ") {
							affiliations.add(new StringBuffer("/").append(code).append(sep).append(dept1).toString());
						}			
					}
					List<String> list = new ArrayList(affiliations);
					Collections.sort(list);
					return list;
				}
			}
		);
	}
	
	
//For Report generation queries	
	
	public Department getDepartmentDetails(String affil) throws Exception {
//		return this.executorService.submit(
//			new Callable<User>() {
//				public User call() throws Exception {
//					return (User) getSqlMapClientTemplate().queryForObject("getDepartmentInfo", affil);
//				}
//			}
//		);
		return (Department) getSqlMapClientTemplate().queryForObject("getDepartmentInfo", affil);
	}
	
	public List<Department> getDepartmentList() throws Exception {	
		//rf		return this.executorService.submit(
		//rf			new Callable<List<UserStatistics>>() {
		//rf				public List<UserStatistics> call() throws Exception {
							List<Department> list = null;
//							if (users == null || users.size() == 0) {
//								list = new LinkedList<UserStatistics>();
//							} else {
//								Map<String,Object> params = new HashMap<String,Object>();
//								params.put("bottom", bottom);
//								params.put("top", top);
//								params.put("users", users);
								list = (List<Department>) getSqlMapClientTemplate().queryForList("getAllDepartments");
//							}
							return list;
		//rf				}
		//rf			}
		//rf		);
			}

	public String getUserName(String upi) throws Exception {
		return (String) getSqlMapClientTemplate().queryForObject("getUserName", upi);
	}	

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

}

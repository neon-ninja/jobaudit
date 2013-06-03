package eresearch.audit.db;

import java.util.List;
import java.util.concurrent.Future;

import eresearch.audit.pojo.Department;
import eresearch.audit.pojo.User;

public interface UserDao {

	public Future<User> getUser(String upi) throws Exception;
	public Future<List<User>> getUsers() throws Exception;
	public Future<List<String>> getUserNames(String top, String bottom) throws Exception;
	public Future<List<String>> getUsersForProject(String project) throws Exception;	
	public Future<List<String>> getUsersForAllProjects() throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code) throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code, String dept1) throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code, String dept1, String dept2) throws Exception;
	public Future<List<String>> getAffiliations() throws Exception;
	public Department getDepartmentDetails(String affil) throws Exception;
	public List<Department> getDepartmentList() throws Exception;
	public String getUserName(String upi) throws Exception;
}

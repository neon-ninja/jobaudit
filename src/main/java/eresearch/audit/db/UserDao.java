package eresearch.audit.db;

import java.util.List;
import java.util.concurrent.Future;

import eresearch.audit.pojo.User;

public interface UserDao {

	public Future<User> getUser(String upi) throws Exception;
	public Future<List<User>> getUsers() throws Exception;
	public Future<List<String>> getUserNames() throws Exception;
	public Future<List<String>> getUsersForProject(String project) throws Exception;	
	public Future<List<String>> getUsersForAllProjects() throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code) throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code, String dept1) throws Exception;
	public Future<List<String>> getUsersForAffiliation(String code, String dept1, String dept2) throws Exception;
	public Future<List<String>> getAffiliations() throws Exception;

}

package eresearch.audit.rest;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.db.UserDao;
import eresearch.audit.pojo.User;

/**
 * 
 */
@Path("/user")
public class UserResource {
    
	private UserDao userDao;
	private AuditRecordDao auditRecordDao;
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() throws Exception {
        return this.userDao.getUsers().get();
    }

    @GET
    @Path("{id}/numjobs")
    @Produces(MediaType.APPLICATION_JSON)
    public int getNumberRecords (@PathParam("id") String id) throws Exception {
        return this.auditRecordDao.getNumberRecords(id).get();
    }

	public void setAuditRecordDao(AuditRecordDao auditRecordDao) {
		this.auditRecordDao = auditRecordDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
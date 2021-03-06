package eresearch.audit.rest;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.pojo.AuditRecord;
import eresearch.audit.pojo.UserStatistics;

/**
 * 
 */
@Path("/records")
public class AuditRecordResource {
    
	private AuditRecordDao auditRecordDao;
	private long maxJobRecordsPerPage;
	
	@GET
    @Path("/{upi}/{orderby}/{sortorder}/{offset}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuditRecord> getRecords(@PathParam("upi") String upi, @PathParam("orderby") String orderby,
    	@PathParam("sortorder") String sortorder, @PathParam("offset") long offset,
    	@PathParam("amount") long amount) throws Exception {
        return this.auditRecordDao.getRecords(upi, orderby, sortorder, offset, maxJobRecordsPerPage).get();
    }

	@GET
	@Path("/userstats/{uid}/{bottom}/{top}")
	@Produces(MediaType.APPLICATION_JSON)
	public UserStatistics getStatisticsForIndividualUser(@PathParam("uid") String uid, @PathParam("bottom") String bottom, 
	    @PathParam("top") String top) throws Exception {
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		List<String> l = new LinkedList<String>();
		l.add(uid);
		from.setTimeInMillis(Long.valueOf(bottom) * 1000);
		to.setTimeInMillis(Long.valueOf(top) * 1000);
		return (UserStatistics) this.auditRecordDao.getStatisticsForUser(l, from, to);
	}

	public void setAuditRecordDao(AuditRecordDao auditRecordDao) {
		this.auditRecordDao = auditRecordDao;
	}
	
	public void setMaxJobRecordsPerPage(long maxJobRecordsPerPage) {
		this.maxJobRecordsPerPage = maxJobRecordsPerPage;
	}

}

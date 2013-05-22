package eresearch.audit.report;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import eresearch.audit.controller.StatisticsController;
import eresearch.audit.db.UserDao;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;

public class ReportTest {

	public ReportTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		ApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "audit-servlet.xml", "root-context.xml",
						"rest-audit-records-servlet.xml" });
		ReportUtils r = (ReportUtils) appContext.getBean("reportUtils");

		//parse command line arguments
		try{
			new JCommander(r, args);
		}
		catch(ParameterException pe){
			System.out.println("Invalid parameters");
			System.exit(0);
		}

		try{
			r.initReport();
		}catch(Exception e){
			System.exit(0);
		}
		
//populate report content for for current/specified month/time-period (command line arguments)		
		try {
			//calculate the report data
			r.getReportContent(null, r.getHistoryStartYear(),
					r.getHistoryStartMonth()-1, r.getHistoryEndYear(),
					r.getHistoryEndMonth()-1, true);
			List<BarDiagramStatistics> bds = r.getBdslist();
			List<UserStatistics> us = r.getUserstatslist();

			//populate the report with the above data
			r.createReport(us, bds);
		} catch (Exception e) {
			System.out.println("invalid parameters");
		}

//populate report content for the period - Jan 2012 to present
		try {
			//calculate the report data
			r.getReportContent(null, 2012,
					0, r.getHistoryEndYear(),
					r.getHistoryEndMonth()-1, false);
			List<BarDiagramStatistics> bds = r.getBdslist();
			List<UserStatistics> us = r.getUserstatslist();

			//populate the report with the above data
			r.createReport(us, bds);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
//print the report		
		r.printReport();
		
		System.exit(0);
	}
}

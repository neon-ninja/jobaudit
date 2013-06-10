package eresearch.audit.report;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
		
		Logger.getRootLogger().setLevel(Level.INFO);
		long start = System.currentTimeMillis();
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
		
		r.generateReportForAllDepartments();
		System.exit(0);
	}
}

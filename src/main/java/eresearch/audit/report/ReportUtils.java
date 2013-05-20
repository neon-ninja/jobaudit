package eresearch.audit.report;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.beust.jcommander.Parameter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import eresearch.audit.db.AuditRecordDao;
import eresearch.audit.db.UserDao;
import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.Department;
import eresearch.audit.pojo.UserStatistics;

public class ReportUtils {

	private UserDao userDao;
	private AuditRecordDao auditRecordDao;
	private int historyStartYear;
	private int historyStartMonth;
	private int historyEndYear;
	private int historyEndMonth;
	private List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();
	private List<UserStatistics> userstatslist = new LinkedList<UserStatistics>();
	
	private Document document;
	
	private PdfWriter writer = null;
	private Department dept = null;
	

	// parse from and to dates to get start and end years and months

	@Parameter(names = "-from", description = "start date", required = false)
	private String fromDate;

	@Parameter(names = "-to", description = "end date", required = false)
	private String toDate;

	@Parameter(names = "-dept", description = "department", required = true)
	private String department;

	
	
	public void initReport(){
		
		 document = new Document();
		
		try {
			 writer = PdfWriter.getInstance(document, new
			 FileOutputStream("userTest1"+System.currentTimeMillis()+".pdf"));
//			writer = PdfWriter.getInstance(document, new FileOutputStream(
//					"userTest1.pdf"));
		} catch (FileNotFoundException ef) {
			ef.printStackTrace();
		} catch (DocumentException e2) {
			e2.printStackTrace();
		}
		
		
		

		try {
			dept = userDao.getDepartmentDetails(department);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		document.open();
		
		Paragraph reportTitle;
		
//		if((historyStartMonth!=historyEndMonth)&&(historyStartYear!=historyEndYear)){
		
		if(fromDate.equals(toDate)){
			reportTitle = new Paragraph("Monthly usage report of Auckland NeSI cluster for "
					+ dept.getDepartmentName() + ", for "
					+ toDate
					+ "\n", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(39, 64, 139)));
		}
		else{
			reportTitle = new Paragraph("Usage report of Auckland NeSI cluster for "
					+ department + ", for the period "
					+ fromDate+ " to "
					+ toDate
					+ "\n", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(39, 64, 139)));
		}
//			}
//			else{
//				reportTitle = new Paragraph("Monthly usage report of Auckland NeSI cluster for "
//						+ dept.getDepartmentName() + ", for "
//						+ (1900 + startdate.getYear()) + "/"
//						+ (startdate.getMonth() + 1) 
//						+ "\n", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(39, 64, 139)));
//			}		
		
			try {
				document.add(reportTitle);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public void getReportContent(List<String> usrList, int startYear,
			int startMonth, int endYear, int endMonth, boolean fiveMonthFlag) throws Exception {

		System.out.println(fromDate);
		System.out.println(toDate);

		if ((startYear > endYear)
				|| ((startYear == endYear) && (startMonth > endMonth))) {
			throw new Exception("from-date is greater that to-date");
		}
		historyStartYear = startYear;
		historyStartMonth = startMonth;
		historyEndYear = endYear;
		historyEndMonth = endMonth;

		System.out.println(historyStartYear);
		System.out.println(historyStartMonth);
		System.out.println(historyEndYear);
		System.out.println(historyEndMonth);

		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		// get list of users

		List<String> userlist = null;
		if (usrList == null) // if being called from main
		{
			from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
			to.set(historyEndYear, historyEndMonth + 1, 1, 0, 0, 0);

			System.out.println("dept:" + department);
			String affil;
			if (department != null) {
				affil = department;
			} else {
				affil = "all";
			}
			userlist = createUserList(affil, from, to);
		} else {
			userlist = usrList;
		}

		from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
		to.set(historyEndYear, historyEndMonth+1, 1, 0, 0, 0);

		userstatslist = auditRecordDao.getStatisticsForUser(userlist, from, to);

		userlist = new LinkedList<String>();
		for (UserStatistics stats : userstatslist) {
			userlist.add(stats.getUser());
		}

		bdslist = new LinkedList<BarDiagramStatistics>();

		if(fiveMonthFlag){
			int diff = historyStartMonth-4;
			if(diff<0){
				historyStartMonth =diff+11;
				historyStartYear-=1;
			}
			else{
				historyStartMonth=diff;
			}
			
//			historyStartMonth=(diff<0?diff+11:diff);
		}
		
		// get bar diagram statistics
		fbdslist = auditRecordDao.getBarDiagramUserStatistics(userlist,
				historyStartYear, historyStartMonth, historyEndYear,
				historyEndMonth);

		for (Future<BarDiagramStatistics> fbds : fbdslist) {
			bdslist.add(fbds.get());
		}
		
		if(fiveMonthFlag){
			int diff = historyStartMonth+4;
			if(diff>11){
				historyStartMonth =diff-11;
				historyStartYear+=1;
			}
			else{
				historyStartMonth=diff;
			}
//			historyStartMonth=(diff<0?diff+11:diff);
		}
	}

	public List<String> createUserList(String affil, Calendar from, Calendar to)
			throws Exception {
		List<String> users = new LinkedList<String>();
		Future<List<String>> fuserlist = null;

		String[] subs = affil.split("/");
		List<String> usersWithAtLeastOneJob = this.userDao.getUserNames(
				"" + (from.getTimeInMillis() / 1000),
				"" + (to.getTimeInMillis() / 1000)).get();

		if (affil.equals("all")) {
			return usersWithAtLeastOneJob;
		} else {
			if (StringUtils.countMatches(affil, "/") == 1) {
				fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim());
			} else if (StringUtils.countMatches(affil, "/") == 2) {
				fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim(),
						subs[2].trim());
			} else if (StringUtils.countMatches(affil, "/") == 3) {
				fuserlist = this.userDao.getUsersForAffiliation(subs[1].trim(),
						subs[2].trim(), subs[3].trim());
			} else {
				throw new Exception("Unexpected affilation string: " + affil);
			}
			List<String> usersForAffil = fuserlist.get();
			System.out.println("usersforaf:" + usersForAffil.size());
			for (String u : usersForAffil) {
				if (usersWithAtLeastOneJob.contains(u)) {
					users.add(u);
				}
			}
		}

		return users;
	}

	public void createReport(List<UserStatistics> users,
			List<BarDiagramStatistics> bdslist) {

		long start = System.currentTimeMillis();
//		Department dept = null;
//
		System.out.println("usersize:" + users.size());
		System.out.println("bdssize:" + bdslist.size());
//
//		try {
//			dept = userDao.getDepartmentDetails(department);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
/**
//		Document document = new Document();
		PdfWriter writer = null;
		try {
			 writer = PdfWriter.getInstance(document, new
			 FileOutputStream("userTest1"+System.currentTimeMillis()+".pdf"));
//			writer = PdfWriter.getInstance(document, new FileOutputStream(
//					"userTest1.pdf"));
		} catch (FileNotFoundException ef) {
			ef.printStackTrace();
		} catch (DocumentException e2) {
			e2.printStackTrace();
		}
		**/

//		document.open();

		document.addTitle("Report");
		Paragraph title = new Paragraph("Report", FontFactory.getFont(
				FontFactory.HELVETICA, 16, Font.BOLD, Color.BLACK));
		title.setAlignment(1);
		title.setSpacingAfter(20);
/*
		try {
			document.add(title);
		} catch (DocumentException e2) {
			e2.printStackTrace();
		}
*/
		PdfPTable table = new PdfPTable(5);
		table.setHeaderRows(1);

		PdfPCell table_cell;

		table_cell = new PdfPCell(new Phrase("Researcher"));
		table_cell.setPadding(5);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Jobs"));
		table_cell.setPadding(5);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Total Core hours"));
		table_cell.setPadding(5);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("% of group total"));
		table_cell.setPadding(5);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("% of cluster total"));
		table_cell.setPadding(5);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		try {
			DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
			DefaultCategoryDataset dataSet2 = new DefaultCategoryDataset();
			DefaultCategoryDataset dataSet3 = new DefaultCategoryDataset();
			DefaultCategoryDataset dataSet4 = new DefaultCategoryDataset();

			int jobCount = 0;
			double coreHourCount = 0;
			int jobInt = 0;
			for (UserStatistics temp : users) {

				jobInt = Integer.parseInt(temp.getJobs());
				jobCount += jobInt;
				coreHourCount += Double.parseDouble(temp.getTotal_core_hours());

				dataSet.setValue(Integer.parseInt(temp.getJobs()), "Users",
						temp.getUser());
			}

			int monthCount = historyStartMonth;
			int yearCount = historyStartYear;
			String monthName = null;
			for (BarDiagramStatistics bs : bdslist) {

				monthName = new DateFormatSymbols().getMonths()[monthCount]
						+ " " + yearCount;
				dataSet2.addValue(bs.getParallel_jobs(), "paralleljobcount",
						monthName);
				dataSet2.addValue(bs.getSerial_jobs(), "serialjobcount",
						monthName);

				dataSet3.addValue(bs.getParallel_core_hours(),
						"parallel core hours", monthName);
				dataSet3.addValue(bs.getSerial_core_hours(),
						"serial core hours", monthName);

				dataSet4.addValue(bs.getAvg_waiting_hours(),
						"average waiting hours", monthName);

				monthCount++;
				if (monthCount > 11) {
					monthCount = 0;
					yearCount++;
				}
			}
			table.setSpacingBefore(20);
			table.setSpacingAfter(10);

			DecimalFormat decFormat = new DecimalFormat("#.##");
			DecimalFormat dec4Format = new DecimalFormat("#.####");
			Double jobDouble;
			Double coreHourDouble;

			long clusterCoreHours = 0;
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
			to.set(historyEndYear, historyEndMonth + 1, 1, 0, 0, 0);

			Date startdate = from.getTime();
			Date endDate = to.getTime();
			
			System.out.println("startNend:"+startdate.toString()+","+endDate.toString());

			long starttime = startdate.getTime();
			long endtime = endDate.getTime();

			System.out.println("starttime" + starttime);
			System.out.println("endtime" + endtime);

			try {
				clusterCoreHours = Long.parseLong(auditRecordDao
						.getTotalCoreHoursInterval((starttime / 1000) + "",
								(endtime / 1000) + ""));
			} catch (Exception e1) {
				System.out.println("No data available");
			}

			for (UserStatistics temp : users) {
				try {
					table_cell = new PdfPCell(new Phrase(
							userDao.getUserName(temp.getUser())));
					table_cell.setPadding(5);
					table.addCell(table_cell);
				} catch (Exception e) {
					table.addCell(temp.getUser());
				}

				table_cell = new PdfPCell(new Phrase(temp.getJobs()));
				table_cell.setPadding(5);
				table.addCell(table_cell);

				table_cell = new PdfPCell(
						new Phrase(temp.getTotal_core_hours()));
				table_cell.setPadding(5);
				table.addCell(table_cell);

				jobDouble = Double.parseDouble(temp.getJobs());
				coreHourDouble = Double.parseDouble(temp.getTotal_core_hours());
				System.out.println("core hours:" + coreHourDouble);

				table_cell = new PdfPCell(new Phrase(Double.valueOf(decFormat
						.format((coreHourDouble * 100) / coreHourCount)) + "%"));
				table_cell.setPadding(5);
				table.addCell(table_cell);

				table_cell = new PdfPCell(new Phrase(
						(Double.valueOf(dec4Format
								.format((coreHourDouble * 100)
										/ clusterCoreHours)) + "%")));
				table_cell.setPadding(5);
				table.addCell(table_cell);
			}

			Paragraph clusterUsage = new Paragraph();
			Paragraph reportTitle;
			
			Paragraph p2=null;
			
			Boolean startDateJan2012=false;
			
			if(historyStartMonth==0 && historyStartYear==2012){
				startDateJan2012=true;
			}
		
		
			if((historyStartMonth!=historyEndMonth)&&(historyStartYear!=historyEndYear)){
			reportTitle = new Paragraph("Usage report of Auckland NeSI cluster for "
					+ dept.getDepartmentName() + ", for the period "
					+ (1900 + startdate.getYear()) + "/"
					+ (startdate.getMonth() + 1) + " to "
					+ (1900 + endDate.getYear()) + "/" + (endDate.getMonth())
					+ "\n", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(39, 64, 139)));
			
			if(startDateJan2012){
				clusterUsage.add("\nTotal usage of Auckland NeSI cluster since 2012/01:\n");
				//look
				 p2 = new Paragraph("");
			}
			else{
			clusterUsage.add("\nUsage from "+ (1900 + startdate.getYear()) + "/"
					+ (startdate.getMonth() + 1) + " to "
					+ (1900 + endDate.getYear()) + "/" + (endDate.getMonth()+"\n"));
			//look
			 p2 = new Paragraph("Department investment from "+historyStartYear+"/"+(historyStartMonth+1)+" to "+historyEndYear+"/"+(historyEndMonth+1)+": ", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, Color.BLACK));
			}
			}
			else{
				reportTitle = new Paragraph("Monthly usage report of Auckland NeSI cluster for "
						+ dept.getDepartmentName() + ", for "
						+ (1900 + startdate.getYear()) + "/"
						+ (startdate.getMonth() + 1) +": "
						+ "\n", FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new Color(39, 64, 139)));
				
				clusterUsage.add("\nUsage in "+ (1900 + startdate.getYear()) + "/"
						+ (startdate.getMonth() + 1+":\n"));
				
				 p2 = new Paragraph("Department investment for "+historyStartYear+"/"+(historyStartMonth+1)+": ", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, Color.BLACK));
			}
			
			
			
//			if((startdate.getYear()==endDate.getYear()) && (startdate.getMonth()==endDate.getMonth())){
//				clusterUsage.add("Usage report of Auckland NeSI cluster for "
//						+ dept.getDepartmentName() + ", for the month "
//						+ (1900 + startdate.getYear()) + "/"
//						+ (startdate.getMonth() + 1) 
//						+ ":\n");
//			}
//			else{
//				clusterUsage.add("Usage report of Auckland NeSI cluster for "
//						+ dept.getDepartmentName() + ", for the period "
//						+ (1900 + startdate.getYear()) + "/"
//						+ (startdate.getMonth() + 1) + " to "
//						+ (1900 + endDate.getYear()) + "/" + (endDate.getMonth())
//						+ ":\n");
//				
			//	clusterUsage.setFont(arg0)
//			}

			clusterUsage.add("\nNumber of Jobs: " + jobCount);
			clusterUsage.add("\nCore Hours: "
					+ Double.valueOf(decFormat.format(coreHourCount)));
		//	document.add(reportTitle);
			document.add(clusterUsage);

//			Paragraph p2 = new Paragraph("User Statistics:");
//			p2.setSpacingBefore(10);
//			document.add(p2);
//			table.setSpacingAfter(30);

			table.setWidthPercentage(100);

			table.setHeadersInEvent(true);

			document.add(table);
		if(!startDateJan2012){	
			int investment = dept.getInvestment();
//			Paragraph deptDets = new Paragraph("Department investment for "+historyEndYear+"/"+historyEndMonth, FontFactory.getFont(FontFactory.HELVETICA, 14, Font.UNDERLINE, new Color(0, 0, 255)) );
			
			
			document.add(p2);
			
			Paragraph deptDets = new Paragraph("\nCPU cores contributed by department: " + investment);

			long diffDays = (endtime - starttime) / (24 * 60 * 60 * 1000);

			long availCoreHours = investment * diffDays * 24;

			deptDets.add("\nMax possible number of core hours on department investment: "
					+ availCoreHours);
			deptDets.add("\nAccumulated monthly core hours: "
					+ Double.valueOf(decFormat.format(coreHourCount)));

			deptDets.add("\nPercentage of department investment: "
					+ Double.valueOf(decFormat.format(coreHourCount * 100
							/ availCoreHours)) + "%");

			document.add(deptDets);
		}

		
		
		
//Bar Diagrams		
		
			PdfPTable barChartsTable = new PdfPTable(2);
			
			barChartsTable.getDefaultCell().setBorderWidth(0);
			System.out.println("barcharttableheight:"+barChartsTable.getDefaultCell().getHeight());
			barChartsTable.getDefaultCell().setMinimumHeight(200);
		
			JFreeChart chart = ChartFactory.createBarChart("User Statistics",
					"User", "jobs", dataSet, PlotOrientation.HORIZONTAL, false,
					true, false);

			// stacked chart for serial and parallel jobs (job count)
			JFreeChart stackedChart = ChartFactory.createStackedBarChart(
					"", "Month", "Number of Jobs", dataSet2,
					PlotOrientation.HORIZONTAL, true, true, false);

			stackedChart.setBackgroundPaint(Color.WHITE);

			// get a reference to the plot for further customisation...
			BarRenderer bsr = (BarRenderer) stackedChart.getCategoryPlot()
					.getRenderer();
			bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			bsr.setBaseItemLabelsVisible(true);
			bsr.setItemLabelsVisible(true);
			bsr.setMaximumBarWidth(0.20); // if not used, it displays giant bars
											// for single month data

			stackedChart.getCategoryPlot().setRenderer(bsr);

			BufferedImage bufferedImage = chart.createBufferedImage(300, 3);
			Image image = null;
			try {
				image = Image.getInstance(writer, bufferedImage, 1.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			document.newPage();
			
			Paragraph graphTitle;
			

//			if((historyStartMonth==0) && (historyStartYear == 2012)){
			if(startDateJan2012){
				graphTitle = new Paragraph("Statistics since 2012/01", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new Color(39, 64, 139)));
			}
			else{
				graphTitle = new Paragraph("Statistics for past 5 months", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new Color(39, 64, 139)));
			}

			graphTitle.setSpacingBefore(20);
			graphTitle.setAlignment(1);
			
			document.add(graphTitle);

//			document.add(image);
//			barChartsTable.addCell(image);

			// stacked chart for serial and parallel jobs (job count)
			bufferedImage = stackedChart.createBufferedImage(500, 300);
			image = null;
			try {
				image = Image.getInstance(writer, bufferedImage, 1.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			document.add(image);
			barChartsTable.addCell(image);

			// core hours
			stackedChart = ChartFactory.createStackedBarChart("", "Month",
					"Number of core hours", dataSet3,
					PlotOrientation.HORIZONTAL, true, true, false);
			stackedChart.setBackgroundPaint(Color.WHITE);

			bsr = (BarRenderer) stackedChart.getCategoryPlot().getRenderer();
			bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			bsr.setBaseItemLabelsVisible(true);
			bsr.setItemLabelsVisible(true);
			bsr.setMaximumBarWidth(0.20); // if not used, it displays giant bars
											// for single month data

			stackedChart.getCategoryPlot().setRenderer(bsr);

			bufferedImage = stackedChart.createBufferedImage(500, 300);
			image = null;
			try {
				image = Image.getInstance(writer, bufferedImage, 1.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
//			document.add(image);
			barChartsTable.addCell(image);

			// chart for average waiting hours
			stackedChart = ChartFactory.createStackedBarChart("", "Month",
					"Average waiting time", dataSet4,
					PlotOrientation.HORIZONTAL, true, true, false);
			stackedChart.setBackgroundPaint(Color.WHITE);

			bsr = (BarRenderer) stackedChart.getCategoryPlot().getRenderer();
			bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			bsr.setBaseItemLabelsVisible(true);
			bsr.setItemLabelsVisible(true);
			bsr.setMaximumBarWidth(0.20); // if not used, it displays giant bars
											// for single month data

			stackedChart.getCategoryPlot().setRenderer(bsr);

			bufferedImage = stackedChart.createBufferedImage(500, 300);
			image = null;
			try {
				image = Image.getInstance(writer, bufferedImage, 1.0f);
			} catch (IOException e) {
				e.printStackTrace();
			}
//Disabling "Average Waiting Time" display		document.add(image);

			
			barChartsTable.setSpacingBefore(10);
			barChartsTable.setWidthPercentage(100);
		//	barChartsTable.setSpacingAfter(20);
			document.add(barChartsTable);
			
			
//			document.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println(end - start);
		
//		document.newPage();
	}
	
	
	public void printReport(){
		
		document.close();
		
	}

	public int getHistoryStartYear() {
		if (fromDate != null) {
			historyStartYear = Integer.parseInt(fromDate.substring(0, 4));
		} else {
			historyStartYear = Calendar.getInstance().get(Calendar.YEAR);
		}
		System.out.println("gethiststartyr");
		return historyStartYear;
	}

	public void setHistoryStartYear(int historyStartYear) {
		this.historyStartYear = historyStartYear;
	}

	public int getHistoryStartMonth() {
		if (fromDate != null) {
			historyStartMonth = Integer.parseInt(fromDate.substring(5));
		} else {
			historyStartMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}
		System.out.println("gethiststrtm");
		return historyStartMonth;
	}

	public void setHistoryStartMonth(int historyStartMonth) {
		this.historyStartMonth = historyStartMonth;
	}

	public int getHistoryEndYear() {
		if (toDate != null) {
			historyEndYear = Integer.parseInt(toDate.substring(0, 4));
		} else {
			historyEndYear = Calendar.getInstance().get(Calendar.YEAR);
		}
		System.out.println("gethistendyr");
		return historyEndYear;
	}

	public void setHistoryEndYear(int historyEndYear) {
		this.historyEndYear = historyEndYear;
	}

	public int getHistoryEndMonth() {
		if (toDate != null) {
			historyEndMonth = Integer.parseInt(toDate.substring(5));
		} else {
			historyEndMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}
		System.out.println("gethistendm");
		return historyEndMonth;
	}

	public void setHistoryEndMonth(int historyEndMonth) {
		this.historyEndMonth = historyEndMonth;
	}

	public List<BarDiagramStatistics> getBdslist() {
		return bdslist;
	}

	public void setBdslist(List<BarDiagramStatistics> bdslist) {
		this.bdslist = bdslist;
	}

	public List<UserStatistics> getUserstatslist() {
		return userstatslist;
	}

	public void setUserstatslist(List<UserStatistics> userstatslist) {
		this.userstatslist = userstatslist;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public AuditRecordDao getAuditRecordDao() {
		return auditRecordDao;
	}

	public void setAuditRecordDao(AuditRecordDao auditRecordDao) {
		this.auditRecordDao = auditRecordDao;
	}
}

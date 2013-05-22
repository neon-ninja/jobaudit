package eresearch.audit.report;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import com.lowagie.text.Element;
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

	// parse department, from and to dates to get start and end years and months

	@Parameter(names = "-from", description = "start date", required = false)
	private String fromDate;

	@Parameter(names = "-to", description = "end date", required = false)
	private String toDate;

	@Parameter(names = "-dept", description = "department", required = true)
	private String department;

	//initiates the report generation 
	public void initReport() throws Exception {

		document = new Document();

		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(
					"Report" + System.currentTimeMillis() + ".pdf"));
		} catch (FileNotFoundException ef) {
			ef.printStackTrace();
		} catch (DocumentException e2) {
			e2.printStackTrace();
		}

		 //get the info for the specified department, and terminate if it is absent
		try {
			dept = userDao.getDepartmentDetails(department);
		} catch (Exception e1) {
			System.out.println("Department information missing.");
		}

		if (dept == null) {
			System.out.println("Department information missing.");
			throw new Exception();
		}

		document.open();

		Paragraph reportTitle;

		reportTitle = new Paragraph("NeSI Pan Cluster Usage Report",
				FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD,
						Color.BLACK));
		try {
			reportTitle.setAlignment(1);
			document.add(reportTitle);
			reportTitle = new Paragraph(dept.getDepartmentName(),
					FontFactory.getFont(FontFactory.HELVETICA, 16, Font.NORMAL,
							Color.BLACK));
			reportTitle.setSpacingAfter(30);
			reportTitle.setAlignment(1);

			document.add(reportTitle);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	//get the report data for the given time-period
	public void getReportContent(List<String> usrList, int startYear,
			int startMonth, int endYear, int endMonth, boolean fiveMonthFlag)
			throws Exception {

		// time range validation
		if ((startYear > endYear)
				|| ((startYear == endYear) && (startMonth > endMonth))) {
			throw new Exception("from-date is greater that to-date");
		}
		historyStartYear = startYear;
		historyStartMonth = startMonth;
		historyEndYear = endYear;
		historyEndMonth = endMonth;

		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();

		// get list of users
		List<String> userlist = null;
		if (usrList == null) // if being called from main
		{
			from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
			to.set(historyEndYear, historyEndMonth + 1, 1, 0, 0, 0);

			String affil;
			if (department != null) {
				affil = department;
			} else {
				affil = "all";
			}
			userlist = createUserList(affil, from, to);
		} else {	//if being called from Statistics/StatisticsAffiliationController
			userlist = usrList;
		}

		from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
		to.set(historyEndYear, historyEndMonth + 1, 1, 0, 0, 0);

		//get user statistics (table data)
		userstatslist = auditRecordDao.getStatisticsForUser(userlist, from, to);

		userlist = new LinkedList<String>();
		for (UserStatistics stats : userstatslist) {
			userlist.add(stats.getUser());
		}

		bdslist = new LinkedList<BarDiagramStatistics>();

		if (fiveMonthFlag) {
			int diff = historyStartMonth - 4;
			if (diff < 0) {
				historyStartMonth = diff + 11;
				historyStartYear -= 1;
			} else {
				historyStartMonth = diff;
			}
		}

		// get bar diagram statistics
		fbdslist = auditRecordDao.getBarDiagramUserStatistics(userlist,
				historyStartYear, historyStartMonth, historyEndYear,
				historyEndMonth);

		for (Future<BarDiagramStatistics> fbds : fbdslist) {
			bdslist.add(fbds.get());
		}
		
		//if bar-chart for past 5 months is to be shown
		if (fiveMonthFlag) {
			int diff = historyStartMonth + 4;
			if (diff > 11) {
				historyStartMonth = diff - 11;
				historyStartYear += 1;
			} else {
				historyStartMonth = diff;
			}
		}
	}

	//create userlist for the given period
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
			for (String u : usersForAffil) {
				if (usersWithAtLeastOneJob.contains(u)) {
					users.add(u);
				}
			}
		}

		return users;
	}

	//populate the report using the given data
	public void createReport(List<UserStatistics> users,
			List<BarDiagramStatistics> bdslist) {

		long start = System.currentTimeMillis();

		DateFormatSymbols dfSym = new DateFormatSymbols();
		NumberFormat numform = NumberFormat.getIntegerInstance(Locale.US);

		Boolean startDateJan2012 = false;

		//data from Jan2012 is displayed in a different way 
		if (historyStartMonth == 0 && historyStartYear == 2012) {
			startDateJan2012 = true;
		}

		Collections.sort(users, new Comparator<UserStatistics>() {

			public int compare(UserStatistics o1, UserStatistics o2) {
				return ((Double) (Double.parseDouble(o1.getTotal_core_hours()) - Double
						.parseDouble(o2.getTotal_core_hours()))).intValue();
			}
		});

		document.addTitle("Report");
		Paragraph title = new Paragraph("Report", FontFactory.getFont(
				FontFactory.HELVETICA, 16, Font.BOLD, Color.BLACK));
		title.setAlignment(1);
		title.setSpacingAfter(20);

		PdfPTable table = new PdfPTable(5);
		table.setHeaderRows(1);
		float[] columnWidths = new float[] { 30f, 10f, 10f, 10f, 10f };
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e2) {
			e2.printStackTrace();
		}

		PdfPCell table_cell;

		table_cell = new PdfPCell(new Phrase("Researcher"));
		table_cell.setPadding(5);
		table_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Jobs"));
		table_cell.setPadding(5);
		table_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Core hours"));
		table_cell.setPadding(5);
		table_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Group %"));
		table_cell.setPadding(5);
		table_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table_cell.setBackgroundColor(Color.GRAY);
		table.addCell(table_cell);

		table_cell = new PdfPCell(new Phrase("Cluster %"));
		table_cell.setPadding(5);
		table_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
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

			if (!startDateJan2012) {
				int diff = historyStartMonth - 4;
				if (diff < 0) {
					monthCount = diff + 11;
					yearCount -= 1;
				} else {
					monthCount = diff;
				}
			}

			String monthName = null;
			for (BarDiagramStatistics bs : bdslist) {

				monthName = dfSym.getMonths()[monthCount] + " " + yearCount;
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

			DecimalFormat decFormat = new DecimalFormat("#.#");
			Double coreHourDouble;

			long clusterCoreHours = 0;
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();

			from.set(historyStartYear, historyStartMonth, 1, 0, 0, 0);
			to.set(historyEndYear, historyEndMonth + 1, 1, 0, 0, 0);

			Date startdate = from.getTime();
			Date endDate = to.getTime();

			long starttime = startdate.getTime();
			long endtime = endDate.getTime();

			try {
				clusterCoreHours = Long.parseLong(auditRecordDao
						.getTotalCoreHoursInterval((starttime / 1000) + "",
								(endtime / 1000) + ""));
			} catch (Exception e1) {
				System.out.println("No data available");
			}

			//populate table data
			for (UserStatistics temp : users) {
				try {
					table_cell = new PdfPCell(new Phrase(
							userDao.getUserName(temp.getUser())));
					table_cell.setPadding(5);
					table_cell.setNoWrap(true);
					table.addCell(table_cell);
				} catch (Exception e) {
					table.addCell(temp.getUser());
				}

				table_cell = new PdfPCell(new Phrase(temp.getJobs()));
				table_cell.setPadding(5);
				table_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(table_cell);

				coreHourDouble = Double.parseDouble(temp.getTotal_core_hours());

				table_cell = new PdfPCell(new Phrase(
						numform.format(coreHourDouble)));
				table_cell.setPadding(5);
				table_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(table_cell);

				table_cell = new PdfPCell(new Phrase(Double.valueOf(decFormat
						.format((coreHourDouble * 100) / coreHourCount)) + ""));
				table_cell.setPadding(5);
				table_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(table_cell);

				table_cell = new PdfPCell(new Phrase(
						(Double.valueOf(decFormat
								.format((coreHourDouble * 360000)
										/ clusterCoreHours)) + ""))); // clusterCoreHours
																		// value
																		// is in
																		// seconds
																		// and
																		// needs
																		// to be
																		// divided
																		// by
																		// 3600
				table_cell.setPadding(5);
				table_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(table_cell);
			}

			Paragraph clusterUsage = new Paragraph();
			Paragraph p2 = null;

			if ((historyStartMonth != historyEndMonth)
					&& (historyStartYear != historyEndYear)) {
				p2 = new Paragraph(dfSym.getMonths()[historyStartMonth] + " "
						+ historyStartYear + " - "
						+ dfSym.getMonths()[historyEndMonth] + " "
						+ historyEndYear, FontFactory.getFont(
						FontFactory.HELVETICA, 16, Font.BOLD, Color.BLACK));
			} else {
				p2 = new Paragraph(dfSym.getMonths()[historyStartMonth] + " "
						+ historyStartYear, FontFactory.getFont(
						FontFactory.HELVETICA, 16, Font.BOLD, Color.BLACK));
			}

			clusterUsage.add(String.format("%s %10s", "\nNumber of Jobs:   ",
					jobCount));
			clusterUsage.add(String.format("%s %20s", "\nCore Hours:   ",
					numform.format(coreHourCount)));

			//display ROI only for the selected time time period and not for data from Jan2012
			if (!startDateJan2012) {
				int investment = dept.getInvestment();
				//don't display ROI if the department investment is null
				if (investment > 0) {
					long diffDays = (endtime - starttime)
							/ (24 * 60 * 60 * 1000);
					long availCoreHours = investment * diffDays * 24;

					clusterUsage.add(String
							.format("%s %30s",
									"\nROI�:",
									numform.format(coreHourCount * 100
											/ availCoreHours))
							+ "%");
				}
			}

			p2.setSpacingBefore(30);

			document.add(p2);
			document.add(clusterUsage);

			table.setWidthPercentage(100);
			table.setHeadersInEvent(true);
			document.add(table);

	// Bar Diagrams
			if (startDateJan2012) {
				PdfPTable barChartsTable = new PdfPTable(2);

				barChartsTable.getDefaultCell().setBorderWidth(0);

				JFreeChart chart = ChartFactory.createBarChart(
						"User Statistics", "User", "jobs", dataSet,
						PlotOrientation.HORIZONTAL, false, true, false);

				// stacked chart for serial and parallel jobs (job count)
				JFreeChart stackedChart = ChartFactory.createStackedBarChart(
						"", "Month", "Number of Jobs", dataSet2,
						PlotOrientation.HORIZONTAL, true, true, false);

				stackedChart.setBackgroundPaint(Color.WHITE);

				// get a reference to the plot for further customisation...
				BarRenderer bsr = (BarRenderer) stackedChart.getCategoryPlot()
						.getRenderer();
				bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
				bsr.setMaximumBarWidth(0.20); // if not used, it displays giant
												// bars
												// for single month data
				bsr.setShadowVisible(false);
				stackedChart.getCategoryPlot().setRenderer(bsr);
				bsr.setItemMargin(10);

				BufferedImage bufferedImage = chart.createBufferedImage(300, 3);
				Image image = null;
				try {
					image = Image.getInstance(writer, bufferedImage, 1.0f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Paragraph graphTitle;

				if (startDateJan2012) {
					graphTitle = new Paragraph("Statistics since January 2012",
							FontFactory.getFont(FontFactory.HELVETICA, 16,
									Font.BOLD, Color.BLACK));
				} else {
					graphTitle = new Paragraph("Statistics for past 5 months",
							FontFactory.getFont(FontFactory.HELVETICA, 16,
									Font.BOLD, Color.BLACK));
				}

				graphTitle.setSpacingBefore(20);
				document.add(graphTitle);

				// stacked chart for serial and parallel jobs (job count)
				if (startDateJan2012) {
					bufferedImage = stackedChart.createBufferedImage(500, 500);
				} else {
					bufferedImage = stackedChart.createBufferedImage(500, 400);
				}
				image = null;
				try {
					image = Image.getInstance(writer, bufferedImage, 1.0f);
				} catch (IOException e) {
					e.printStackTrace();
				}

				barChartsTable.addCell(image);

				// core hours
				stackedChart = ChartFactory.createStackedBarChart("", "Month",
						"Number of core hours", dataSet3,
						PlotOrientation.HORIZONTAL, true, true, false);
				stackedChart.setBackgroundPaint(Color.WHITE);

				bsr = (BarRenderer) stackedChart.getCategoryPlot()
						.getRenderer();
				bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
				bsr.setMaximumBarWidth(0.20); // if not used, it displays giant
												// bars
												// for single month data
				bsr.setShadowVisible(false);
				bsr.setDrawBarOutline(false);
				bsr.setItemMargin(10);

				stackedChart.getCategoryPlot().setRenderer(bsr);

				if (startDateJan2012) {
					bufferedImage = stackedChart.createBufferedImage(500, 500);
				} else {
					bufferedImage = stackedChart.createBufferedImage(500, 400);
				}

				image = null;
				try {
					image = Image.getInstance(writer, bufferedImage, 1.0f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				barChartsTable.addCell(image);

				// chart for average waiting hours
				stackedChart = ChartFactory.createStackedBarChart("", "Month",
						"Average waiting time", dataSet4,
						PlotOrientation.HORIZONTAL, true, true, false);
				stackedChart.setBackgroundPaint(Color.WHITE);

				bsr = (BarRenderer) stackedChart.getCategoryPlot()
						.getRenderer();
				bsr.setMaximumBarWidth(0.20); // if not used, it displays giant
												// bars
												// for single month data
				bsr.setShadowVisible(false);
				bsr.setItemMargin(10);

				stackedChart.getCategoryPlot().setRenderer(bsr);

				bufferedImage = stackedChart.createBufferedImage(500, 300);
				image = null;
				try {
					image = Image.getInstance(writer, bufferedImage, 1.0f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Disabling "Average Waiting Time" display document.add(image);

				barChartsTable.setSpacingBefore(10);
				barChartsTable.setWidthPercentage(100);
				document.add(barChartsTable);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		System.out.println("Time taken for Report Generation: " + (end - start)
				+ "ms");
	}

	//print the report footnotes and close it
	public void printReport() {

		Paragraph endNotes = new Paragraph(
				"1. ROI i.e. Return on Investment is calculated as a percentage of the department's actual cluster usage against their investment",
				FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL,
						Color.BLACK));
		endNotes.setSpacingBefore(30);
		try {
			document.add(endNotes);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		document.close();

	}

	
//getters-setters	
	public int getHistoryStartYear() {
		if (fromDate != null) {
			historyStartYear = Integer.parseInt(fromDate.substring(0, 4));
		} else {
			historyStartYear = Calendar.getInstance().get(Calendar.YEAR);
		}
		// System.out.println("gethiststartyr");
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
		// System.out.println("gethiststrtm");
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
		// System.out.println("gethistendyr");
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
		// System.out.println("gethistendm");
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

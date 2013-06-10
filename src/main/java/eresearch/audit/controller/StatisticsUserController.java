package eresearch.audit.controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.DefaultCategoryItemRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.web.servlet.ModelAndView;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;

import eresearch.audit.pojo.BarDiagramStatistics;
import eresearch.audit.pojo.UserStatistics;
import eresearch.audit.report.ReportUtils;

public class StatisticsUserController extends StatisticsController {

	static Logger log = Logger.getLogger("StatisticsUserController.class");

	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("Inside handleRequestInternal");
		ModelAndView mav = super.handleRequestInternal(request, response);
		List<UserStatistics> userstatslist = new LinkedList<UserStatistics>();
		List<Future<BarDiagramStatistics>> fbdslist = new LinkedList<Future<BarDiagramStatistics>>();
		List<BarDiagramStatistics> bdslist = new LinkedList<BarDiagramStatistics>();

		Map params = request.getParameterMap();
		setHistoryStartYear(Integer.parseInt(((String[]) params.get("from_y"))[0]));
		setHistoryStartMonth(Integer.parseInt(((String[]) params.get("from_m"))[0]));
		setHistoryEndYear(Integer.parseInt(((String[]) params.get("to_y"))[0]));
		setHistoryEndMonth(Integer.parseInt(((String[]) params.get("to_m"))[0]));
		
//		Calendar from = Calendar.getInstance();
//		Calendar to= Calendar.getInstance();
		
		
//		
//		reportUtil.get
//		
		// get list of users based on the values in user dropdown
		List<String> userlist = this.createUserList(request);
		
		//get user and bar diagram statistics
		

		
		
		//time period related changes
//rf		Future<List<UserStatistics>> uslist;
//report		List<UserStatistics> uslist;
		
//		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
//		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
		
		//get statistics
//report		uslist=this.auditRecordDao.getStatisticsForUser(userlist, from, to);
		
		
		
		
		// collect information from futures
//		userstatslist=uslist.get();
/** report	
 * 	userstatslist=uslist;
		
		userlist=new LinkedList<String>();
		for(UserStatistics stats: userstatslist){
			userlist.add(stats.getUser());
		}
		
		//get bar diagram statistics
		fbdslist = auditRecordDao.getBarDiagramUserStatistics(userlist,
				super.historyStartYear, super.historyStartMonth, 
				super.historyEndYear, super.historyEndMonth);
		
        for (Future<BarDiagramStatistics> fbds : fbdslist) {
        	bdslist.add(fbds.get());
        }
**/
		
		reportUtils.getReportContent(userlist, super.historyStartYear, super.historyStartMonth, super.historyEndYear, super.historyEndMonth, false);
		userstatslist = reportUtils.getUserstatslist();
		bdslist = reportUtils.getBdslist();
		
//		System.out.println("list size:"+bdslist.size());
		
        mav.addObject("user_statistics", userstatslist);
		mav.addObject("job_statistics", bdslist);
		
		//for retaining dropdown values
		mav.addObject("startYear", historyStartYear);
		mav.addObject("startMonth", historyStartMonth);
		mav.addObject("endYear", historyEndYear);
		mav.addObject("endMonth", historyEndMonth);
		mav.addObject("selectedUser", selectedUser);
		mav.addObject("selectedAffiliation", selectedAffiliation);
		
	//	createReport(userstatslist, bdslist);
		
		
	    return mav;
	}
	

	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		log.info("Inside createUserList");
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		String user=((String[]) params.get("user"))[0];
		
		if (params.containsKey("user") && !(user.equalsIgnoreCase("all"))) 
		{
			users.add(user);
			setSelectedUser(user);
		} 
		else 
		{
			Calendar from = Calendar.getInstance();
			Calendar to= Calendar.getInstance();

			from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
			to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
			// list of all user names
			users.addAll(super.userDao.getUserNames(""+(from.getTimeInMillis()/1000),""+(to.getTimeInMillis()/1000)).get());
			if(user.equalsIgnoreCase("all"))
			{
				setSelectedUser(user);
			}
		}
		log.info("Returning from createUserList. list size="+users.size());
		return users;
	}
	
	private void createReport(List<UserStatistics> users, List<BarDiagramStatistics> bdslist) {
		// TODO Auto-generated method stub
		
//		UserStatistics stats = new UserStatistics();
//		stats.setUser("user1");
//		stats.setJobs("10");
//		stats.setGrid_jobs("6");
//		stats.setTotal_cores("20");
//		stats.setTotal_core_hours("100");
//		stats.setTotal_grid_core_hours("60");
//		stats.setTotal_waiting_time("10");
//		stats.setAverage_waiting_time("20");
		long start= System.currentTimeMillis();
//		
		    Document document = new Document();
			PdfWriter writer = null;
		      try {
				writer = PdfWriter.getInstance(document, new FileOutputStream("userTest.pdf"));
			} catch (FileNotFoundException ef) {
				// TODO Auto-generated catch block
				ef.printStackTrace();
			}catch (DocumentException e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		      
		      document.open();
		      
		      PdfPTable table = new PdfPTable(8);
		      table.setHeaderRows(1);
		      
		      table.addCell("User");//+"\t\t"+
				table.addCell("Jobs");//+"\t\t"+
				table.addCell("Grid Jobs");//+"\t\t"+
				table.addCell("Total cores");//+"\t\t"+
				table.addCell("Total core hours");//+"\t\t"+
				table.addCell("Total grid core hours");//+"\t\t"+
				table.addCell("Total waiting time (hours)");//+"\t\t"+
				table.addCell("Average waiting time (hours)");//+"\t\t");
		      
		      try {
//				document.add( new Chunk("User\tJobs\tGrid jobs\tTotal cores\tTotal core hours"));
				
				DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
				DefaultCategoryDataset dataSet2 = new DefaultCategoryDataset();
				DefaultCategoryDataset dataSet3 = new DefaultCategoryDataset();
				DefaultCategoryDataset dataSet4 = new DefaultCategoryDataset();
				
				
				//Paragraph p1 = new Paragraph("hello");
				for(UserStatistics temp: users){
					
//					document.add(new Chunk(temp.getUser()+"\t"+
//							temp.getJobs()+"\t"+
//							temp.getGrid_jobs()+"\t"+
//							temp.getTotal_cores()+"\t"+
//							temp.getTotal_core_hours()+"\t"+
//							temp.getTotal_grid_core_hours()+"\t"+
//							temp.getTotal_waiting_time()+"\t"+
//							temp.getAverage_waiting_time()+"\t"));
					
					Paragraph p1 = new Paragraph();
					//com.lowagie.text.Table t = new Table(arg0)

//					p1.add("\n"+temp.getUser()+"\t\t"+
//							temp.getJobs()+"\t\t"+
//							temp.getGrid_jobs()+"\t\t"+
//							temp.getTotal_cores()+"\t\t"+
//							temp.getTotal_core_hours()+"\t\t"+
//							temp.getTotal_grid_core_hours()+"\t\t"+
//							temp.getTotal_waiting_time()+"\t\t"+
//							temp.getAverage_waiting_time()+"\t\t");
					
					table.addCell(temp.getUser());//+"\t\t"+
					table.addCell(temp.getJobs());//+"\t\t"+
					table.addCell(temp.getGrid_jobs());//+"\t\t"+
					table.addCell(temp.getTotal_cores());//+"\t\t"+
					table.addCell(temp.getTotal_core_hours());//+"\t\t"+
					table.addCell(temp.getTotal_grid_core_hours());//+"\t\t"+
					table.addCell(temp.getTotal_waiting_time());//+"\t\t"+
					table.addCell(temp.getAverage_waiting_time());//+"\t\t");
					
					
					dataSet.setValue(Integer.parseInt(temp.getJobs()), "Users", temp.getUser());
					
					document.add(p1);
				}
//				document.add(table);
				
				int monthCount =historyStartMonth;
				String monthName=null;
				for(BarDiagramStatistics bs: bdslist){
					monthName= new DateFormatSymbols().getMonths()[monthCount];
					dataSet2.addValue(bs.getParallel_jobs(), "paralleljobcount", monthName);
					dataSet2.addValue(bs.getSerial_jobs(), "serialjobcount", monthName);
					
					dataSet3.addValue(bs.getParallel_core_hours(), "parallel core hours", monthName);
					dataSet3.addValue(bs.getSerial_core_hours(), "serial core hours", monthName);
					
					dataSet4.addValue(bs.getAvg_waiting_hours(), "average waiting hours", monthName);
					
					monthCount++;
				}
			 
			        JFreeChart chart = ChartFactory.createBarChart(
			                "User Statistics", "User", "jobs",
			                dataSet, PlotOrientation.HORIZONTAL, false, true, false);
			 
			   //     ChartFactory.createStackedBarChart(title, domainAxisLabel, rangeAxisLabel, dataset, orientation, legend, tooltips, urls)

  //stacked chart for serial and parallel jobs (job count)			        
			        JFreeChart stackedChart = ChartFactory.createStackedBarChart("User statistics", "Month", "Number of Jobs", dataSet2, PlotOrientation.HORIZONTAL, true, true, false);

			        stackedChart.setBackgroundPaint(Color.WHITE);

			     // get a reference to the plot for further customisation...
			     final CategoryPlot plot = stackedChart.getCategoryPlot();


//			     final CategoryItemRenderer renderer = new StackedBarRenderer(
////			     new Paint[] {Color.red, Color.blue, Color.green,
////			     Color.yellow, Color.orange, Color.cyan,
////			     Color.magenta, Color.blue}
//			     );

//			     renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator("{2}",NumberFormat.getCurrencyInstance(Locale.US)));

//			     renderer.setSeriesItemLabelsVisible(0, true);			        
			        
//			     CategoryItemRenderer cir = stackedChart.getCategoryPlot().getRendererForDataset(dataSet2);
//			     cir.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator("{2}",NumberFormat.getNumberInstance()));
//		
//			     cir.setSeriesItemLabelsVisible(0, true);
//			     //cir.add
//			     stackedChart.getCategoryPlot().setRenderer(cir);
			     
			     BarRenderer bsr = (BarRenderer) stackedChart.getCategoryPlot().getRenderer();
			     bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			     bsr.setBaseItemLabelsVisible(true);
			     bsr.setItemLabelsVisible(true);
			     
			     stackedChart.getCategoryPlot().setRenderer(bsr);
			     
//temp			        

//			        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
//			        KeyToGroupMap map = new KeyToGroupMap("")
			        
				//document.add(chart);
				 BufferedImage bufferedImage = chart.createBufferedImage(300, 3);
		            Image image = null;
					try {
						image = Image.getInstance(writer, bufferedImage, 1.0f);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            document.add(image);	
				
		            
//stacked chart for serial and parallel jobs (job count)		            
		         bufferedImage = stackedChart.createBufferedImage(500, 300);
		            image = null;
					try {
						image = Image.getInstance(writer, bufferedImage, 1.0f);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            document.add(image);
		            
		            
//core hours
		        stackedChart = ChartFactory.createStackedBarChart("", "Month", "Number of core hours", dataSet3, PlotOrientation.HORIZONTAL, true, true, false);
		        stackedChart.setBackgroundPaint(Color.WHITE);

			     bsr = (BarRenderer) stackedChart.getCategoryPlot().getRenderer();
			     bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			     bsr.setBaseItemLabelsVisible(true);
			     bsr.setItemLabelsVisible(true);
			     
			     stackedChart.getCategoryPlot().setRenderer(bsr);		            
		         
		         bufferedImage = stackedChart.createBufferedImage(500, 300);
		            image = null;
					try {
						image = Image.getInstance(writer, bufferedImage, 1.0f);
					} catch (IOException e) {
						e.printStackTrace();
					}
		            document.add(image);
		            

//average waiting hours
			        stackedChart = ChartFactory.createStackedBarChart("", "Month", "Average waiting time", dataSet4, PlotOrientation.HORIZONTAL, true, true, false);
			        stackedChart.setBackgroundPaint(Color.WHITE);

				     bsr = (BarRenderer) stackedChart.getCategoryPlot().getRenderer();
				     bsr.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
				     bsr.setBaseItemLabelsVisible(true);
				     bsr.setItemLabelsVisible(true);
				     
				     stackedChart.getCategoryPlot().setRenderer(bsr);		            
			         
			         bufferedImage = stackedChart.createBufferedImage(500, 300);
			            image = null;
						try {
							image = Image.getInstance(writer, bufferedImage, 1.0f);
						} catch (IOException e) {
							e.printStackTrace();
						}
			            document.add(image);		            
		            
			        
			            document.newPage();
			            
			            table.setSpacingBefore(20);
			            
			            
			            Paragraph p2 = new Paragraph("User Statistics:");
			            p2.setSpacingBefore(50);
			            document.add(p2);
			    document.add(table);			            
				document.close();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		      
		      long end = System.currentTimeMillis();
		      System.out.println(end-start);
	}
	
	
}

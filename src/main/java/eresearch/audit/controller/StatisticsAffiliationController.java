package eresearch.audit.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class StatisticsAffiliationController extends StatisticsUserController {

	static Logger log = Logger.getLogger(Thread.currentThread().getClass());
	
	protected List<String> createUserList(HttpServletRequest req) throws Exception {
		Map params = req.getParameterMap();
		List<String> users = new LinkedList<String>();
		Future<List<String>> fuserlist = null;
		
		Calendar from = Calendar.getInstance();
		Calendar to= Calendar.getInstance();
		
		from.set(super.historyStartYear, super.historyStartMonth, 1,0,0,0);
		to.set(super.historyEndYear, super.historyEndMonth+1, 1,0,0,0);
		
		if (params.containsKey("affiliation")) {
			String affil = ((String[]) params.get("affiliation"))[0];
			setSelectedAffiliation(affil);

			users = reportUtils.createUserList(affil, from, to);	
		}
		
		log.info("Returning from createUserList. list size="+users.size());
		return users;
	}


}

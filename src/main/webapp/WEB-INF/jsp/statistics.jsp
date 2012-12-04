<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.min.js"></script>
  <!--[if lte IE 8]><script type="text/javascript" type="text/javascript" src="<%=request.getContextPath()%>/js/excanvas.min.js"></script><![endif]-->
  <script type="text/javascript" type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.flot.min.js"></script>
  <script type="text/javascript" type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.flot.stack.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.tablesorter.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.blockUI.2.39.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/util.js"></script>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/tablesorter/blue/style.css" type="text/css"/>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/common.css" type="text/css"/>
  
  <script type="text/javascript">
    // Arrays for the data to be plotted 
    var monthly_serial_jobs_data = new Array();
    var monthly_serial_core_hours_data = new Array();
    var monthly_parallel_jobs_data = new Array();
    var monthly_parallel_core_hours_data = new Array();
    var monthly_avg_waiting_hours_data = new Array();
    
    // query string params
	var qs_user = getParameterByName("user");
	var qs_affil = getParameterByName("affiliation");
	var qs_project = getParameterByName("project");

	var users = new Array();
	var usermap = new Array();
	var affils = new Array();
	var projects = new Array();
    <c:forEach items="${users}" var="tmp">
	  users.push(new Array("${tmp.id}", "${tmp.name}"));
	  usermap["${tmp.id}"] = "${tmp.name}";
	</c:forEach>
    <c:forEach items="${affiliations}" var="tmp">
      affils.push("${tmp}");
    </c:forEach>
    <c:forEach items="${projects}" var="tmp">
      projects.push("${tmp}");
    </c:forEach>

    function get_data() {
		var val_select1 = $("#select1").val();
		var val_select2 = $("#select2").val();
        var all_until_last_element = window.location.pathname.lastIndexOf("/");
        var url = window.location.pathname.substr(0,all_until_last_element) + "/statistics_" + val_select1;
        if (val_select2 != "") {
        	url += "?" + val_select1 + "=" + val_select2;
        }
        window.location.href = url;
    }
    
    function getParameterByName(name) {
    	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    	var regexS = "[\\?&]" + name + "=([^&#]*)";
    	var regex = new RegExp(regexS);
    	var results = regex.exec(window.location.search);
    	if(results == null) {
    		return "";
    	} else {
            return decodeURIComponent(results[1].replace(/\+/g, " "));    		
    	}
    }

    function build_initial_selects() {
		var options = "";
    	if (qs_user != "") {
    		options = "<option value='affiliation'>Affiliation</option>";
    		options += "<option value='project'>Project</option>";
    		options += "<option selected=\"selected\" value='user'>User</option>";
    	} else if (qs_project != "") {
    		options = "<option value='affiliation'>Affiliation</option>";
    		options += "<option value='project' selected=\"selected\">Project</option>";
    		options += "<option value='user'>User</option>";    		
    	} else {
    		options = "<option value='affiliation' selected=\"selected\">Affiliation</option>";
    		options += "<option value='project'>Project</option>";
    		options += "<option value='user'>User</option>";
    	}
		$("#select1").html(options);

		options = "<option value='all'>All</option>";
		if (qs_user != "") {
			for (var i=0; i<users.length; i++) {
				var sel = "";
				if (qs_user == users[i][0]) {
					sel = "selected=\"selected\"";
				}
				options += "<option " + sel + " value='" + users[i][0] + "'>" + users[i][1] + "</option>";
			}
		} else if (qs_project != "") {
			for (var i=0; i<projects.length; i++) {
				var sel = "";
				if (qs_project == projects[i]) {
					sel = "selected=\"selected\"";
				}
				options += "<option " + sel + " value='" + projects[i] + "'>" + projects[i] + "</option>";
			}
		} else {
			for (var i=0; i<affils.length; i++) {
				var sel = "";
				if (qs_affil == affils[i]) {
					sel = "selected=\"selected\"";
				}
				options += "<option " + sel + " value='" + affils[i] + "'>" + affils[i] + "</option>";
			}
		}
		$("#select2").html(options);    	
    }
    
	function adjust_select2() {
		var val_select1 = $("#select1").val();
		var options = "<option value='all'>All</option>";
		if (val_select1 == 'user') {
			for (var i=0; i<users.length; i++) {
				options += "<option value='" + users[i][0] + "'>" + users[i][1] + "</option>";
			} 
		} else if (val_select1 == 'affiliation') {
			for (var i=0; i<affils.length; i++) {
				options += "<option value='" + affils[i] + "'>" + affils[i] + "</option>";
			} 
		} else if (val_select1 == 'project') {
			for (var i=0; i<projects.length; i++) {
				options += "<option value='" + projects[i] + "'>" + projects[i] + "</option>";
			} 			
		}
		$("#select2").html(options);
	}
	
    $(document).ready(function() {
    	build_initial_selects();
        $("#statistics").tablesorter({widgets:['zebra'], sortList:[[5,1]]});
    });
  </script>
</head>

<body>
  <div id="header">
    <a class="white" href="<%=request.getContextPath()%>/html/users">Users</a> &nbsp;&nbsp;&nbsp;
    <a class="white" href="<%=request.getContextPath()%>/html/userrecords">User Records</a> &nbsp;&nbsp;&nbsp;
    <a class="white" href="<%=request.getContextPath()%>/html/statistics">Statistics</a> &nbsp;&nbsp;&nbsp;
  </div>
  <div id="body">
  
  <h3>Statistics</h3>

  Get statistics for 
  <select id="select1" onChange="adjust_select2()">
    <option value="affiliation" selected="selected">Affiliation</option>
    <option value="user">User</option>
    <option value="project">Project</option>
  </select>

  <select id="select2">
    <option value='' selected="selected">All</option>
    <c:forEach items="${affiliations}" var="tmp">
      <option value="${tmp}">${tmp}</option>
    </c:forEach>  
  </select>
  
  <button type="button" onClick="get_data()">Go!</button> 
   
  <c:if test="${not empty user_statistics}">
  <br>  
  <c:set var="totalNumberJobs" value="0" />
  <c:set var="totalCoreHours" value="0" />
  <c:set var="totalNumberGridJobs" value="0" />
  <c:set var="totalGridCoreHours" value="0" />
  <c:forEach items="${user_statistics}" var="statistics">
    <c:set var="totalNumberJobs" value="${totalNumberJobs + statistics.jobs}" />
    <c:set var="totalCoreHours" value="${totalCoreHours + statistics.total_core_hours}" />
    <c:set var="totalNumberGridJobs" value="${totalNumberGridJobs + statistics.grid_jobs}" />
    <c:set var="totalGridCoreHours" value="${totalGridCoreHours + statistics.total_grid_core_hours}" />
  </c:forEach>
  
  <c:set var="gridJobsPercentage" value="${(100*totalNumberGridJobs)/totalNumberJobs}" />
  <c:set var="gridTotalHoursPercentage" value="${(100*totalGridCoreHours)/totalCoreHours}" />

  <h4>Total number of jobs</h4>
  <table id="bordered">
  	<tr>
  	  <td>&nbsp;</td>
  	  <td align="right">Total</td>
  	  <td align="right">Via Grid</td>
  	</tr>
    <tr>
      <td>Number of jobs</td>
      <td align="right"><b>${totalNumberJobs}</b></td>
      <td align="right"><b>${totalNumberGridJobs}</b> (<script type="text/javascript">document.write(${gridJobsPercentage}.toFixed(2))</script> %)</td>
    </tr>
    <tr>
      <td>Core hours</td>
      <td align="right"><b><script type="text/javascript">document.write(${totalCoreHours}.toFixed(2))</script></b></td>
      <td align="right"><b><script type="text/javascript">document.write(${totalGridCoreHours}.toFixed(2))</script></b> (<script type="text/javascript">document.write(${gridTotalHoursPercentage}.toFixed(2))</script> %)</td>
    </tr>
  </table>

  <!-- Areas where the diagrams are plotted -->
  <table border="0" cellpadding="10">
    <tr>
      <th>Number Jobs (that actually started)</th>
      <th>Core Hours</th>
      <th>Average Waiting Hours</th>
    </tr>
    <tr>
      <td><div id="monthly_jobs_plot" style="width:380px;height:200px;"></div></td>
      <td><div id="monthly_core_hours_plot" style="width:380px;height:200px;"></div></p></td>
      <td><div id="monthly_avg_waiting_hours_plot" style="width:380px;height:200px;"></div></td>
    </tr>
  </table>
  <br>
  
  <!-- Prepare the data to be plotted -->
  <c:forEach items="${job_statistics}" var="statistics">
    <c:if test="${not empty statistics.serial_jobs}">
      <script type="text/javascript">
        var arr = new Array(${statistics.serial_jobs}, ${statistics.bottom}*1000);
        monthly_serial_jobs_data.push(arr);
      </script>      
    </c:if>
    <c:if test="${not empty statistics.parallel_jobs}">
      <script type="text/javascript">
        var arr = new Array(${statistics.parallel_jobs}, ${statistics.bottom}*1000);
        monthly_parallel_jobs_data.push(arr);
      </script>      
    </c:if>
    <c:if test="${not empty statistics.serial_core_hours}">
      <script type="text/javascript">
        var arr = new Array(${statistics.serial_core_hours}, ${statistics.bottom}*1000);
        monthly_serial_core_hours_data.push(arr);
      </script>
    </c:if>
    <c:if test="${not empty statistics.parallel_core_hours}">
      <script type="text/javascript">
        var arr = new Array(${statistics.parallel_core_hours}, ${statistics.bottom}*1000);
        monthly_parallel_core_hours_data.push(arr);
      </script>
    </c:if>
    <c:if test="${not empty statistics.avg_waiting_hours}">
      <script type="text/javascript">
        var arr = new Array(${statistics.avg_waiting_hours}, ${statistics.bottom}*1000);
        monthly_avg_waiting_hours_data.push(arr);
      </script>
    </c:if>
  </c:forEach>

  <!-- Plot the bar diagrams -->
  <script type="text/javascript">
      draw_stacked_bar_diagram(monthly_jobs_plot, [ 
          { "label": "Serial Jobs", "data": monthly_serial_jobs_data, "color": "#06c" },
          { "label": "Parallel Jobs", "data": monthly_parallel_jobs_data, "color": "#d70" }
      ]);
      draw_stacked_bar_diagram(monthly_core_hours_plot, [
          { "label": "Serial Core Hours", "data": monthly_serial_core_hours_data, "color": "#060" },
          { "label": "Parallel Core Hours", "data": monthly_parallel_core_hours_data, "color": "#d70" }
      ]);
      draw_stacked_bar_diagram(monthly_avg_waiting_hours_plot, [
          { "label": "Avg. Waiting Hours", "data": monthly_avg_waiting_hours_data, "color": "#b00" }
      ]);
  </script>
  <br>
  
  <!-- User overview -->
  <b>Per user:</b>
  <table id="statistics" class="tablesorter"><thead>
    <tr>
      <th>User Name</th>
      <th>User ID</th>
      <th>Jobs</th>
      <th>Jobs (Grid)</th>
      <th>Total Cores</th>
      <th>Total Core Hours</th>
      <th>Total Core Hours (Grid)</th>
      <th>Total Waiting Time</th>
      <th>Average Waiting Time</th>
    </tr>
    </thead><tbody>
  
  <c:forEach items="${user_statistics}" var="statistics">
    <tr>
      <td><a href="<%=request.getContextPath()%>/html/userrecords?upi=${statistics.user}">
            <script type="text/javascript">document.write(usermap["${statistics.user}"]);</script></a></td> 
      <td><a href="<%=request.getContextPath()%>/html/userrecords?upi=${statistics.user}">${statistics.user}</a></td> 
      <td>${statistics.jobs}</td> 
      <td>${statistics.grid_jobs}</td> 
      <td>${statistics.total_cores}</td> 
      <td>${statistics.total_core_hours}</td> 
      <td>${statistics.total_grid_core_hours}</td> 
      <td>${statistics.total_waiting_time}</td> 
      <td>${statistics.average_waiting_time}</td>
    </tr>
  </c:forEach>
  </tbody>
  </table>
  
  </c:if>
  </div>
</body>

</html>

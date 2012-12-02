<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>

<head>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.tablesorter.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.blockUI.2.39.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.paging.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/TableRenderer.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/Paginator.js"></script>
    
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/common.css" type="text/css"/>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/recordtable.css" type="text/css"/>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/overlay.css" type="text/css"/>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/pagination.css" type="text/css"/>
    
  <script type="text/javascript">
    var orderBy = "jobid";
    var order = "desc";
    var offset = 0;
    var name = '<c:out value="${user.name}"/>';
    var upi = '<c:out value="${user.id}"/>';
    var contextPath = '<c:out value=""/>';
    var maxJobRecordsPerPage = '<c:out value="${maxJobRecordsPerPage}"/>';
    var totalNumberRecords = '<c:out value="${totalNumberRecords}"/>';
    var pageCount = Math.ceil(totalNumberRecords / maxJobRecordsPerPage);
    var columnNames = new Array("jobid", "queue", "jobgroup", "jobtype", "account", "appstatus", "status",
    	"cores", "nodes", "memrequested", "walltime", "mem", "vmem", "qtime", "start", "done", "coretime",
    	"efficacy", "shared", "workingdir", "executable", "jobname");

    function reloadWithUser(user) {
        location.href='?upi=' + user;
    }
    
    // Display the modal to pick another user 
    function displayUserModal(users, textStatus, xhr) {
      var string = '<b>Pick a user</b>: ';
      string += '<select onchange="reloadWithUser(this.value)">';
      string += '<option value=""></option>';
      for (var i=0; i<users.length; i++) {
          string += '<option value="' + users[i].id + '">' + users[i].name + '</option>';
      }
      // string+='<input type="button" id="changeuser_cancel" value="Cancel" onclick="$.unblockUI()"/>';
      $.blockUI({ message: string }); 
    }

    if (name=="") {
      $.ajax({ 
          url: '<%=request.getContextPath()%>/rest/user', 
          cache: false, 
          success: displayUserModal
      });
    }

    $(document).ready(function() {      
      var tableR = new TableRenderer("#records", columnNames);

      // Load records from via REST call to database service 
      function loadRecords() {
        var tmpOrderBy = (orderBy == "jobid") ? "id" : orderBy;
        $.ajax({
          url: '<%=request.getContextPath()%>/rest/records/' + upi + '/' + tmpOrderBy + '/' + order + '/' + offset,
          cache: false,
          success: renderTable
        });
      }
            
      // Render the loaded data in a table 
      function renderTable(data, textStatus, xhr) {
        var obj = jQuery.parseJSON(data);
        tableR.render(data, orderBy);
      }

      // Called when the page changes 
      function onPageChange(newPage) {
        offset = (newPage-1) * maxJobRecordsPerPage;
        loadRecords();
      }

      // Records are loaded in the paginator 
      new Paginator(".pagination", pageCount, onPageChange).render();
      
      // Load user names and display the modal to pick another user 
      $('#changeuser').click(function() {
        $.ajax({ 
          url: '<%=request.getPathTranslated()%>', 
          cache: false, 
          success: displayUserModal
        }); 
      }); 

      // Change sorting order from ascending to descending or vice versa 
      $("#recordOrder").change(function() {
        order = $('#recordOrder').val();
        new Paginator(".pagination", pageCount, onPageChange).render();
      });
            
      // Sort the table using a different parameter 
      $("#recordOrderBy").change(function() {
        orderBy = $('#recordOrderBy').val();
        new Paginator(".pagination", pageCount, onPageChange).render();
      });
            
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
  <div id="summary">
    <h3>Audit records of ${user.name} (${user.id})</h3>
    <table id="bordered">
      <tr>
        <td>Total number of job records</td>
        <td><b>${totalNumberRecords}</b></td>
      </tr>
      <tr>
        <td>Max job records per page</td>
        <td><b>${maxJobRecordsPerPage}</b></td>
      </tr>
      <tr>
        <td>Sorting field and order</td>
        <td>
          <select id="recordOrderBy">
          <script type="text/javascript">
            entries = columnNames.slice().sort();
            for (var i=0; i<entries.length; ++i) {
              document.write('<option value="' + entries[i] + '"');
              if (entries[i] == orderBy) {
                document.write(" selected");
              }
              document.write('>' + entries[i] + '</option>');
            }
          </script> 
          </select>
          <select id="recordOrder">
            <option value='desc'>descending</option>
            <option value='asc'>ascending</option>
          </select>
        </td>
      </tr>
    </table>
  </div>
  <div class="pagination"></div>
  <div id="records"></div>
  <div class="pagination"></div>
  </div>
</body>

</html>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>

<head>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-1.7.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.tablesorter.min.js"></script>
  <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.blockUI.2.39.js"></script>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/tablesorter/blue/style.css" type="text/css"/>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/style/common.css" type="text/css"/>
  <script type="text/javascript">
    var users = new Array();

    function draw_table() {
      var table = '<table id="usertable" class="tablesorter"><thead>\
        <tr>\
          <th>User Name</th>\
          <th>User ID</th>\
          <th>Affiliation</th>\
          <th>Total number of jobs</th>\
        </tr></thead><tbody>';
        for (var key in users) {
          user = users[key];
          table += '<tr>\
            <td><a href="<%=request.getContextPath()%>/html/userrecords?upi=' + user['id'] + '">' + user['name'] + '</a></td>\
            <td><a href="<%=request.getContextPath()%>/html/userrecords?upi=' + user['id'] + '">' + user['id'] + '</a></td>\
            <td>' + user['affiliation'] + '</td>\
            <td>' + user['numjobs'] + '</td></tr>';
        }
        table += '</tbody></table>';
        document.getElementById('user_table').innerHTML = table;
        $("#usertable").tablesorter({widgets:['zebra'], sortList:[[0,0]]});
    }

    // Get number of jobs for user
    function getNumberJobs(upi) {
      $.ajax({
        url: '<%=request.getContextPath()%>/rest/user/' + upi + '/numjobs',
        cache: false,
        success: function(data, textStatus, xhr) { users[upi]['numjobs'] = data; draw_table(); }
      });
    }
      
    $(document).ready(function() {
      draw_table();
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
  <h3>Users</h3>
  <c:forEach items="${users}" var="user">
    <script type="text/javascript">
      var user = new Object();
      user['name'] = '<c:out value="${user.name}"/>'; 
      user['id'] = '<c:out value="${user.id}"/>'; 
      user['affiliation'] = '<c:out value="${user.affiliation}"/>'; 
      user['numjobs'] = '';
      users[user['id']] = user;
      getNumberJobs(user['id']);
    </script>
  </c:forEach>
  <div id="user_table"></div>
  </div>
</body>

</html>

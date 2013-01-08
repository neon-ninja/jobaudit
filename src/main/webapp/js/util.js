// pad a number
function pad2(number) {
  return (number < 10 ? '0' : '') + number;
} 

function create_text_from_hours(number) {
  number = Math.round(number);
  var years = Math.floor(number/(24*365));
  number = number % (24*365);
  var days = Math.floor(number/24);
  var hours = number % 24;
  return years + "y " + days + "d " + hours + "h";
}

//Plot a clustered bar diagram 
function draw_2clustered_bar_diagram(divid, data) {
  
  var barwidth = 8 * 24 * 60 * 60 * 1000;
  
  // Adjust y-position of the datasets 
  for (var i=0; i<data[0]['data'].length; i++) {
    data[0]['data'][i][1] = data[0]['data'][i][1] - barwidth/1.4;
    data[1]['data'][i][1] = data[1]['data'][i][1] + barwidth/1.4;
  }
  
  var options = {
    series: {
      stack: true,
      bars: {
        show: true,
        barWidth: barwidth,
        align: 'center',
        horizontal: true,
      },
    },
    
    xaxis: {
      tickFormatter: function(x) {
    	if (x>=1000) {
    		return (x/1000).toFixed(1) + "K";
    	} else {
    		return x.toFixed(2);
    	}
      },
      autoscaleMargin: 0.1,
    },
    
    yaxis: {
      mode: 'time',
      timeformat: "%b&nbsp;%y",
      minTickSize: [1, "month"],
      tickSize: [1, "month"],
      autoscaleMargin: 0.01,
    },
    
    grid: { hoverable: true, clickable: true },
    legend: { position: 'se', }
  };

  $(function() {
   
	$.plot($(divid), data, options);
      
    function showTooltip(x, y, contents) {
      $('<div id="tooltip">' + contents + '</div>').css( {
        position: 'absolute',
        display: 'none',
        top: y + 5,
        left: x + 5,
        border: '1px solid #fdd',
        padding: '2px',
        'background-color': '#111',
        'color': '#fee',
        opacity: 0.80
      }).appendTo("body").fadeIn(200);
    }

    var previousPoint = null;
    $(divid).bind("plothover", function (event, pos, item) {
      
      $("#x").text(pos.x.toFixed(2));
      $("#y").text(pos.y.toFixed(2));

      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;
          $("#tooltip").remove();
          var x = item.datapoint[0];
          var y = item.datapoint[1];

          // construct tooltip text
          var text = x;
          showTooltip(item.pageX, item.pageY, text);
        }    
      } else { 
        $("#tooltip").remove();
        previousPoint = null;
      }
    });
  });
}


// Plot a stacked bar diagram 
function draw_stacked_bar_diagram(divid, data) {
   
  var options = {
    series: {
      stack: true,
      bars: {
        show: true,
        barWidth: 15 * 24 * 60 * 60 * 1000,
        align: 'center',
        horizontal: true,
      },
    },
    
    xaxis: {
      tickFormatter: function(x) {
          if (x>=1000) {
        	  return (x/1000).toFixed(1) + "K";
          } else {
        	  return x.toFixed(2);
          }
      },
      autoscaleMargin: 0.1,
    },
    
    yaxis: {
      mode: 'time',
      timeformat: "%b&nbsp;%y",
      minTickSize: [1, "month"],
      tickSize: [1, "month"],
      autoscaleMargin: 0.01,
    },
    
    grid: { hoverable: true, clickable: true },
    legend: { position: 'se', }
  };

  $(function() {
   
	$.plot($(divid), data, options);
      
    function showTooltip(x, y, contents) {
      $('<div id="tooltip">' + contents + '</div>').css( {
        position: 'absolute',
        display: 'none',
        top: y + 5,
        left: x + 5,
        border: '1px solid #fdd',
        padding: '2px',
        'background-color': '#111',
        'color': '#fee',
        opacity: 0.80
      }).appendTo("body").fadeIn(200);
    }

    var previousPoint = null;
    $(divid).bind("plothover", function (event, pos, item) {
      
      $("#x").text(pos.x.toFixed(2));
      $("#y").text(pos.y.toFixed(2));

      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;
          $("#tooltip").remove();
          var x = item.datapoint[0];
          var y = item.datapoint[1];

          // construct tooltip text
          var text = x;
          if (data[0]["label"].indexOf("Serial Jobs") == 0) { // Tooltips for jobs 
            var count = 0;
            text = "";
            for (var i=0; i<data.length; i++) {
              var num = 0;
              if (data[i]["data"][item.dataIndex] != undefined) {
                num = data[i]["data"][item.dataIndex][0];
              }
              var label = data[i]["label"];
              count += num;
              if (text == "") {
                text += num + " " + label;
              } else {
                text += " + " + num + " " + data[i]["label"];
              }
            }
            text = count + " (" + text + ")";
          } else if (data[0]["label"].indexOf("Serial Core Hours") == 0) { // Tooltips for core hours 
            var count = 0;
            text = "";
            for (var i=0; i<data.length; i++) {
              var num = 0;
              if (data[i]["data"][item.dataIndex] != undefined) {
                  num = data[i]["data"][item.dataIndex][0];
              }
              var numHuman = create_text_from_hours(num);
              var label = data[i]["label"].split(' ')[0];
              count += num;
              if (text == "") {
                text += numHuman + " " + label;
              } else {
                text += " + " + numHuman + " " + label;
              }
            }
            var countHuman = create_text_from_hours(count);
            text = countHuman + " (" + text + ")";
          }
          showTooltip(item.pageX, item.pageY, text);
        }    
      } else { 
        $("#tooltip").remove();
        previousPoint = null;
      }
    });
  });
}

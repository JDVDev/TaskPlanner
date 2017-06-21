$(document).ready(function() {
    var socket = io();
    var evenID;
    var oddID;
    var toggle131 = false;
    var toggle171 = false;
    var timerValue = 0;
    var timerCounter = 0;
    var timer;
    var isAutomatic = false;
    socket.emit('advertisedata',"fffffffbbf00000003000000000000000000000000000000");
    socket.on('receiveinfo', function(msg){
      console.log(msg);
      var splitData = msg.split(',');
      $('#messagestable tr').first().next().append("<td>" + splitData[0] + "</td><td>" +
                                                            splitData[1] + "</td><td>" + 
                                                            splitData[2] + "</td><td>" + 
                                                            splitData[3] + "</td><td>" + 
                                                            splitData[4] + "</td>");
    });
    socket.on('sendinfo', function(msg){
      var splitData = msg.split(',');
      $('#messagestable tr').first().after("<tr><td>" + splitData[0] + "</td><td>" + splitData[1] + "</td></tr>");
      timerCounter = timerValue;
      clearInterval(timer);
      if(isAutomatic){
        setTimerText();        
        timer = setInterval(setTimerText, 1000);
      }      
    });
    socket.on('counterdemocounter', function(msg){
      if(msg.length === 4){
        if(msg.substring(0,2) === evenID){
          $('#countereven').text(msg.substring(0,2) + ": " + parseInt(msg.substring(2,4), 16));
        }
        if(msg.substring(0,2) === oddID){
          $('#counterodd').text(msg.substring(0,2) + ": " + parseInt(msg.substring(2,4), 16));
        }
      }
    });
    $("#btnCountStart").click(function(){
      evenID = $('#didevn').val();
      oddID = $('#didodd').val();
      timerValue = parseInt($('#timervalue').val());
      timerCounter = timerValue;
      socket.emit('startcount', $('#didevn').val() + "," + $('#didodd').val() + "," + $('#timervalue').val());
    });
    $("#btnCountStep").click(function(){
      socket.emit('democounterstep', "step");
    });
    $("#btnCountAuto").click(function(){
      setTimerText();
      timer = setInterval(setTimerText, 1000);
      socket.emit('democounterautomatic', "auto");
      isAutomatic = true;
    });
    $("#btnCountStop").click(function(){
      clearInterval(timer);
      socket.emit('stopcount', "plsstop");
      isAutomatic = false;
    });
    $("#btnSwitch131").click(function(){
      socket.emit('toggle131', toggle131);
      toggle131 = !toggle131;
      if(toggle131){
        $('#btnSwitch131').text("Enable 131");
        $('#btnSwitch131').css("background-color", "red");        
      }
      else{
        $('#btnSwitch131').text("Disable 131");
        $('#btnSwitch131').css("background-color", "lightgreen"); 
      }
    });
    $("#btnSwitch171").click(function(){
      socket.emit('toggle171', toggle171);
      toggle171 = !toggle171;
      if(toggle171){
        $('#btnSwitch171').text("Enable 171");
        $('#btnSwitch171').css("background-color", "red");      
      }
      else{
        $('#btnSwitch171').text("Disable 171");
        $('#btnSwitch171').css("background-color", "lightgreen");       
      }
    });
    $('form').submit(function(){
      socket.emit('advertisedata', $('#m').val());
      $('#m').val('');
      return false;
    });
    function setTimerText(){
      $('#timer').text("Timer: " + timerCounter);
      timerCounter--;
      if(timerCounter <= 0){
        timerCounter = timerValue;
      }
    }
    window.onbeforeunload = function() {
      socket.emit('stopcount',"plsstop");
      socket.emit('advertisedata',"fffffffbbf0000ff03ff0000000000000000000000000000");
    };
});
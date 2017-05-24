$(document).ready(function() {
    var socket = io();
    var toggle131 = false;
    var toggle171 = false;
    var jsonString = '{ "notification": [ { "id": "0001", "content": "What do you want to eat?", "action": [ { "id": "01", "content": "Meat" }, { "id": "02", "content": "Vegetables" }, { "id": "03", "content": "Chicken" }, { "id": "04", "content": "Fish" }, { "id": "05", "content": "Car" }, { "id": "06", "content": "Macawoni" }, { "id": "07", "content": "Mustache" }, { "id": "08", "content": "Surprise skittels instead of m&ms" } ] }, { "id": "0002", "content": "When do you want to go home?", "action": [ { "id": "01", "content": "When my work day ends" }, { "id": "02", "content": "Over a hour" }, { "id": "03", "content": "At 19:00" }, { "id": "04", "content": "Now" }, { "id": "05", "content": "Didnt even want to come" }, { "id": "06", "content": "Already gone" }, { "id": "07", "content": "At home sick" }, { "id": "08", "content": "Fanta" } ] }, { "id": "0003", "content": "What are you doing this weekend?", "action": [ { "id": "01", "content": "Chill" }, { "id": "02", "content": "Skipping work" }, { "id": "03", "content": "Work extra because I love my boss" }, { "id": "04", "content": "Gym life" }, { "id": "05", "content": "Bungee jump" }, { "id": "06", "content": "Party with lots of drugs" }, { "id": "07", "content": "Mustache" }, { "id": "08", "content": "Getting fired for giving the options above" } ] }, { "id": "0004", "content": "Give me a cookie!!", "action": [ { "id": "01", "content": "Yes" }, { "id": "02", "content": "Ill give you 2" }, { "id": "03", "content": "Yes" }, { "id": "04", "content": "YEAH" } ] } ] }';
    var jsonObj = JSON.parse(jsonString);
    socket.on('notificationaction', function(msg){
      var notificationID = msg.substr(8, 4);
      var actionID = msg.substr(12, 2);
      var deviceID = msg.substr(2, 2);
      var content = "";
      for(var i = 0; i < jsonObj.notification.length; i++){
        if(jsonObj.notification[i].id === notificationID){
          for(var j = 0; j < jsonObj.notification[i].action.length; j++){
            if(jsonObj.notification[i].action[j].id === actionID){
              content = jsonObj.notification[i].action[j].content;
            }
          }
        }
      }
      console.log("NotiID: " + notificationID + " ActionID: " + actionID);
      console.log("For gooed hoper: " + content);
      if(notificationID === "0001"){
        $('#vraag1').append(deviceID + ": " + content + "<br />");
      }
      if(notificationID === "0002"){
        $('#vraag2').append(deviceID + ": " + content + "<br />");        
      }
      if(notificationID === "0003"){
        $('#vraag3').append(deviceID + ": " + content + "<br />");        
      }
      if(notificationID === "0004"){
        $('#vraag4').append(deviceID + ": " + content + "<br />");        
      }
    });
    $("#btnVraag1").click(function(){
      answerCounter = 0;
      socket.emit('advertisedata',"01fb000400010000000000000000000000000000");
    });
    $("#btnVraag2").click(function(){
      answerCounter = 0;
      socket.emit('advertisedata',"02fb000400020000000000000000000000000000");
    });
    $("#btnVraag3").click(function(){
      answerCounter = 0;
      socket.emit('advertisedata',"03fb000400030000000000000000000000000000");
    });
    $("#btnVraag4").click(function(){
      answerCounter = 0;
      socket.emit('advertisedata',"04fb000400040000000000000000000000000000");
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
    window.onbeforeunload = function() {
      socket.emit('advertisedata',"fffb0003ff000000000000000000000000000000");
    };
});
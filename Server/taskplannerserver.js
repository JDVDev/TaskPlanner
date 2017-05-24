var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var cron = require('node-cron');
const os = require('os');
var Stopwatch = require('timer-stopwatch');
var stopwatch = new Stopwatch();

const MESSAGE_LEN = 40;
var canAdvertise = false;
var lastRecivedMessageIDBufferSize = 5;
var lastRecivedMessageIDBuffer = new Array(lastRecivedMessageIDBufferSize);
var connectedClients = new Array();
var counterDemoReceviedCounter = 0;

var demoCounterCounter = 0;
var byteCounter = Buffer.alloc(1, 0x01);

var identicalPacketCounter = 0; //For demo
var sendIdenticalPacketCounter = 0; //For demo
var jitterEven = 0;
var jitterOdd = 0;

function puts(error, stdout, stderr){ 
  console.log(stdout)
}

app.use(express.static('public'));
app.get('/', function (req, res) {
   res.sendFile( __dirname + "/" + "index.html" );
});

io.on('connection', function(socket){
  console.log('a user connected');
  connectedClients.push(socket);
  socket.on('disconnect', function(){
    var index = connectedClients.indexOf(socket);
    if (index > -1) {
      connectedClients.splice(index, 1);
    }
    console.log('user disconnected');
  });
});

io.on('connection', function(socket){
  socket.on('discoverdata', function(msg){
    //console.log('message: ' + msg);
    //console.log("msg.lenght: " + msg.length);
    if(msg.length === MESSAGE_LEN){
      var discoveryData = Buffer.from(msg, 'hex'); //Remove manufacture id
      //console.log("discoveryData: " + discoveryData.toString('hex'));
      //console.log("discoveryData.lenght: " + discoveryData.length);
      if(discoveryData[2] === 0xFB){ //thats me
        if(lastRecivedMessageIDBuffer.indexOf(discoveryData[0]) === -1){

          console.log("Total packets: " + identicalPacketCounter);
          sendIdenticalPacketCounter = identicalPacketCounter + 1;
          identicalPacketCounter = 0;

          lastRecivedMessageIDBuffer.push(discoveryData[0]);
          console.log("Buffer: " + lastRecivedMessageIDBuffer.toString());
          if(lastRecivedMessageIDBuffer.length >= lastRecivedMessageIDBufferSize){
              lastRecivedMessageIDBuffer = lastRecivedMessageIDBuffer.slice(1);
          }

          var fromBeacon = socket.request.connection.remoteAddress;
          console.log("From beacon: " + fromBeacon);
          
          if(discoveryData[3] === 0x00){// Type ping
            //console.log("Message ID: " + discoveryData.toString('hex'));
						//console.log("Last message ID: " + lastRecivedMessageIDBuffer.toString('hex'));
						//console.log("Recived ping sending pong to ID: " + discoveryData.toString('hex'));
            io.emit('receiveinfo', "Received ping from 0x" + discoveryData[1].toString(16));//Send data to browser clients
					}
          if(discoveryData[3] === 0x01){//Type data
            console.log("Received data: " + discoveryData.toString('hex'));
            //io.emit('receiveinfo', "Received data from 0x" + discoveryData[1].toString(16));//Send data to browser clients
            if(discoveryData[4] === 0x03){ //Demo counter
              var demoCounter = Buffer.alloc(2);
              demoCounter[0] = discoveryData[1]; //Sender
              demoCounter[1] = discoveryData[5]; //Counter value
              //console.log("Received demoCounter: " + demoCounter.toString('hex'));
              console.log("Received demoCounter: " + ++counterDemoReceviedCounter);
              var epochBytes = Buffer.from(discoveryData.slice(6,14));
              console.log("Epoch string in bytes: " + epochBytes.toString('hex'));
              var epochString = epochBytes.toString();
              console.log("Epoch string: " + epochString);
              var epochServer = parseInt((new Date).getTime().toString().substr(-8));
              console.log("epoch server: " + epochServer + " epochClient: " + epochString);
              var timeTaken = epochServer - parseInt(epochString);
              var jitter = 0;
              if(discoveryData.slice(5, 6).readUInt8(0) %2 === 0){// recv even so send is odd
                jitter = timeTaken - jitterOdd;
                jitterOdd = timeTaken;
              }
              else{// recv odd so send is even
                jitter = timeTaken - jitterEven;
                jitterEven = timeTaken;
              }
              var raspiID = "";
              if(discoveryData[discoveryData.length - 1] === 0x01){
                raspiID = "171";
              }
              else if(discoveryData[discoveryData.length - 1] === 0x02){
                raspiID = "131";
              }
              var watchReceivedPacketAmountByWatch = 0;
              watchReceivedPacketAmountByWatch = discoveryData.readIntLE(14,4);
              var ommitedSendString = discoveryData.slice(5, 6).readUInt8(0).toString() + 
                                    "," + timeTaken + " ms" + 
                                    "," + jitter + " ms" + 
                                    "," + watchReceivedPacketAmountByWatch.toString() + 
                                    "," + stopwatch.ms / 1000 + " s" + 
                                    "," + sendIdenticalPacketCounter.toString() +
                                    "," + raspiID + " -> Watch -> " + fromBeacon.substr(-3);
              io.emit('receiveinfo', ommitedSendString);
              stopwatch.stop();
              stopwatch.reset();
              //io.emit('counterdemocounter', demoCounter.toString('hex'));//Send data to browser clients
            }
            if(discoveryData[4] === 0x04){ //Notification Action
              console.log("Received noti action");
              if(discoveryData[5] === 0x01){ //Accepted
                io.emit('actionaccept', discoveryData.slice(6,8).toString('hex') + discoveryData.slice(1).toString('hex'));
                console.log("Send action: " + discoveryData.slice(6,8).toString('hex'));
              }
              if(discoveryData[5] === 0x02){ //Dismissed
                io.emit('actiondismiss', discoveryData.slice(6,8).toString('hex') + discoveryData.slice(1).toString('hex'));
              }
            }
            if(discoveryData[4] === 0x05){ //Send tap
              
            }
            if(discoveryData[4] === 0x06){ //Send snake control
              console.log("Received control send: " + discoveryData.slice(5).toString('hex'));
              io.emit('snakedirection', discoveryData.slice(5).toString('hex'));
            }
            if(discoveryData[4] === 0x07){ //Join team for tapper demo
              console.log("Received tapper demo: " + discoveryData.toString('hex'));
              if(discoveryData[5] === 0x01){
                console.log("Send tap");
                io.emit('tapper', discoveryData.toString('hex'));
              }
              if(discoveryData[5] === 0x10 || discoveryData[5] === 0x11 || discoveryData[5] === 0x12){
                io.emit('setplayer', discoveryData.toString('hex'));
              }
              if(discoveryData[5] === 0x50){
                io.emit('playerready', discoveryData.toString('hex'));
              }
            }
					}
					if(discoveryData[3] === 0x02){//Type poll
            //console.log("Received poll request");
            io.emit('receiveinfo', "Received poll request from 0x" + discoveryData[1].toString(16)); //Send data to browser clients
					}
          if(discoveryData[3] === 0x04){//Type notification
            console.log("Received notification");
            console.log("Sending: " + discoveryData.slice(4).toString('hex'));
            io.emit('notificationaction', discoveryData.toString('hex'));
          }
        }
        else{
          identicalPacketCounter++
        }
      }
    }
  });
});

io.on('connection', function(socket){
  var cronJob;
  socket.on('startcount', function(msg){ //Start counter demo
    console.log("Start count");
    var splitData = msg.split(',');
    if(splitData.length != 3){
      console.log("Mallformed data: " + splitData.length);
      return;
    }
    var deviceIDs = Buffer.alloc(2);
    var stringIDS = splitData[0] + splitData[1];
    var timerValue = splitData[2];
    if(timerValue < 5){
      timerValue = 5;
    }
    console.log("ID msg lenght: " + stringIDS.length);
    if(stringIDS.length === 4){
      deviceIDs = Buffer.from(stringIDS, 'hex');
    }
    socket.on('democounterstep', function(msg){ //Step counter demo
      counterDemoStep(deviceIDs);
    });
    socket.on('democounterautomatic', function(msg){ //Step counter demo
      if(cronJob != null){
        console.log("Stop on start");
        cronJob.destroy();
      }
      cronJob = cron.schedule("*/" + timerValue + " * * * * *", function(){counterDemoStep(deviceIDs)}, true); 
    });
  });
  socket.on('stopcount', function(msg){
    console.log("plzzz just stop");
    if(cronJob != null){
      console.log("STOP");
      cronJob.destroy();
      demoCounterCounter = 0;
      byteCounter = Buffer.alloc(1, 0x01);
    }
  });
});

function counterDemoStep(deviceIDs){
  var sendData = Buffer.alloc(MESSAGE_LEN / 2);
  // Set initail data
  sendData[0] = 0x00; //MSG ID
  sendData[1] = 0xFB; //This deviceID
  sendData[2] = 0x00; //DeviceID to send to (0x00 is preciefed as broadcast by reciving devices)
  sendData[3] = 0x01; //MSG Type (0x01 is preciefed as a msg containing data)
  sendData[4] = 0x03; //Data to speceficy running demo counter

  //Manipulate data
  sendData[0] = byteCounter[0]; //MSG ID
  console.log("Device IDs " + deviceIDs.toString('hex'));
  if(demoCounterCounter %2 === 0){// Even
    console.log("Even");
    sendData[2] = deviceIDs[0];
  }
  else{ //odd
    console.log("Odd");
    sendData[2] = deviceIDs[1];
  }
  demoCounterCounter++;
  sendData[5] = byteCounter[0];
  //console.log("Send " + counter + " messages, sending msg with ID: 0x" + sendData[0].toString(16) + " and byteCounter: 0X" + byteCounter.toString('hex'));
  stopwatch.start();
  io.emit('advertisedata', sendData.toString('hex'));
  io.emit('sendinfo', sendData.readUInt8(5) + "," + sendData.slice(2, 3).toString('hex'));
  byteCounter[0] += 0x01;
  if(byteCounter[0] === 0xFF){
    byteCounter[0] = 0x01;
  }
  //console.log("Send " + sendData.toString('hex'));
  console.log("Send counter: " + demoCounterCounter + " times");
  //console.info('cron job completed');
}

io.on('connection', function(socket){
  socket.on('advertisedata', function(msg){ //Recieved data to advertise from browser client
    console.log("msg: " + msg.toString('hex'));
    console.log("msg.lenght: " + msg.length);
    if(msg.length === MESSAGE_LEN){
      io.emit('advertisedata', msg); //Broadcast advertise data to raspis
    }
  });
});

io.on('connection', function(socket){
  socket.on('toggle131', function(msg){
    console.log("Toggle 131: " + msg);
    console.log("connectedClients: " + connectedClients.length);
    if(connectedClients.length > 0){
      for(var i = 0; i < connectedClients.length; i++){
        console.log("Searching: " + connectedClients[i].request.connection.remoteAddress);
        if(connectedClients[i].request.connection.remoteAddress === "::ffff:192.168.10.131"){
          console.log("Found " + connectedClients[i].request.connection.remoteAddress);
          connectedClients[i].emit('changeEnableState', msg);
          break;
        }
      } 
    }
  });
  socket.on('toggle171', function(msg){ 
    if(connectedClients.length > 0){
      for(var i = 0; i < connectedClients.length; i++){
        if(connectedClients[i].request.connection.remoteAddress === "::ffff:192.168.10.171"){
          connectedClients[i].emit('changeEnableState', msg);
          break;
        }
      } 
    }
  });
});

http.listen(8081, function(){ //Listen for connenctions
  console.log('listening on *:8081');
});
var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var cron = require('node-cron');
const os = require('os');
var Stopwatch = require('timer-stopwatch');
var stopwatch = new Stopwatch();
var crypto = require('crypto');

const password = "067835110a97e82dd18f0a39e426a5ef";
var algorithm = 'aes-256-ctr';

const MESSAGE_LEN = 48;
var canAdvertise = false;
var lastRecivedMessageIDBufferSize = 20;
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
      var discoveryData = Buffer.from(msg, 'hex');
      //console.log("before decrypt discoveryData: " + discoveryData.toString('hex'));      
      discoveryData = decrypt(discoveryData);
      //console.log("discoveryData: " + discoveryData.toString('hex'));
      //console.log("discoveryData.lenght: " + discoveryData.length);
      if(discoveryData[5] === 0xFB && discoveryData[6] === 0xBF){ //thats me
        //console.log("recieved hash: " + discoveryData.slice(15, 15 + 8).toString('hex'));
        //console.log("created hash: " + createHash(msg).toString('hex'));
        if(createHash(discoveryData).equals(discoveryData.slice(15, 15 + 8))){//For this network
          //console.log("recieved message: " + msg);
          //console.log("Correct hash");
          if(lastRecivedMessageIDBuffer.indexOf(discoveryData.slice(0, discoveryData.length - 1).toString('hex')) === -1){
            console.log("Total packets: " + identicalPacketCounter);
            sendIdenticalPacketCounter = identicalPacketCounter + 1;
            identicalPacketCounter = 0;
            lastRecivedMessageIDBuffer.push(discoveryData.slice(0, discoveryData.length - 1).toString('hex'));
            console.log("Buffer: " + lastRecivedMessageIDBuffer.toString());
            if(lastRecivedMessageIDBuffer.length >= lastRecivedMessageIDBufferSize){
                lastRecivedMessageIDBuffer = lastRecivedMessageIDBuffer.slice(1);
            }
            var fromBeacon = socket.request.connection.remoteAddress;
            console.log("From beacon: " + fromBeacon);
            
            if(discoveryData[8] === 0x00){// Type ping
              //console.log("Message ID: " + discoveryData.toString('hex'));
              //console.log("Last message ID: " + lastRecivedMessageIDBuffer.toString('hex'));
              //console.log("Recived ping sending pong to ID: " + discoveryData.toString('hex'));
              var sendData = Buffer.from(discoveryData);
              sendData[0] = 0xFF;
              sendData[1] = 0xFF;
              sendData[2] = 0xFF;
              sendData[3] = 0xFB;
              sendData[4] = 0xBF;
              sendData[5] = discoveryData[3];
              sendData[6] = discoveryData[4];
              advertise(sendData.toString('hex'));
            }
            if(discoveryData[8] === 0x01){//Type data
              console.log("Received data: " + discoveryData.toString('hex'));
              //io.emit('receiveinfo', "Received data from 0x" + discoveryData[1].toString(16));//Send data to browser clients
              if(discoveryData[9] === 0x03){ //Demo counter
                var demoCounter = Buffer.alloc(3);
                demoCounter[0] = discoveryData[0]; //Sender
                demoCounter[1] = discoveryData[1]; //Sender
                demoCounter[2] = discoveryData[5]; //Counter value
                //console.log("Received demoCounter: " + demoCounter.toString('hex'));
                console.log("Received demoCounter: " + ++counterDemoReceviedCounter);
                var raspiID = "";
                if(discoveryData[14] === 0x01){
                  raspiID = "171";
                }
                if(discoveryData[14] === 0x02){
                  raspiID = "131";
                }
                var watchReceivedPacketAmountByWatch = 0;
                watchReceivedPacketAmountByWatch = discoveryData.readUInt16LE(11,2);
                var ommitedSendString = discoveryData.slice(10, 11).readUInt8(0).toString() + 
                                      "," + watchReceivedPacketAmountByWatch.toString() + 
                                      "," + stopwatch.ms / 1000 + " s" + 
                                      "," + sendIdenticalPacketCounter.toString() +
                                      "," + raspiID + " -> Watch -> " + fromBeacon.substr(-3);
                io.emit('receiveinfo', ommitedSendString);
                stopwatch.stop();
                stopwatch.reset();
                //io.emit('counterdemocounter', demoCounter.toString('hex'));//Send data to browser clients
              }
              if(discoveryData[9] === 0x05){ //Send tap
                
              }
              if(discoveryData[9] === 0x06){ //Send snake control
                console.log("Received control send: " + discoveryData.slice(5).toString('hex'));
                io.emit('snakedirection', discoveryData.slice(5).toString('hex'));
              }
              if(discoveryData[9] === 0x07){ //Join team for tapper demo
                console.log("Received tapper demo: " + discoveryData.toString('hex'));
                if(discoveryData[10] === 0x01){
                  console.log("Send tap");
                  io.emit('tapper', discoveryData.toString('hex'));
                }
                if(discoveryData[10] === 0x10 || discoveryData[10] === 0x11 || discoveryData[10] === 0x12){
                  io.emit('setplayer', discoveryData.toString('hex'));
                }
                if(discoveryData[10] === 0x50){
                  io.emit('playerready', discoveryData.toString('hex'));
                }
              }
            }
            if(discoveryData[8] === 0x02){//Type poll
              //console.log("Received poll request");
              io.emit('receiveinfo', "Received poll request from 0x" + discoveryData[1].toString(16)); //Send data to browser clients
            }
            if(discoveryData[8] === 0x04){//Type notification
              console.log("Received notification");
              console.log("Sending: " + discoveryData.toString('hex'));
              io.emit('notificationaction', discoveryData.toString('hex'));
            }
          }
          else{
            identicalPacketCounter++
          }
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
    var deviceIDs = Buffer.alloc(4);
    var stringIDS = splitData[0] + splitData[1];
    var timerValue = splitData[2];
    if(timerValue < 5){
      timerValue = 5;
    }
    console.log("ID msg lenght: " + stringIDS.length);
    if(stringIDS.length === 8){
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
  sendData[0] = 0xFF; //Sequence
  sendData[1] = 0xFF; //Sequence
  sendData[2] = 0xFF; //Sequence
  sendData[3] = 0xFB; //This deviceID
  sendData[4] = 0xBF; //This deviceID
  sendData[5] = 0x00; //DeviceID to send to (0x0000 is preciefed as broadcast by reciving devices)
  sendData[6] = 0x00; //DeviceID to send to (0x0000 is preciefed as broadcast by reciving devices)
  sendData[7] = 0x00; //MSG ID
  sendData[8] = 0x01; //MSG Type
  sendData[9] = 0x03; //Data to speceficy running demo counter

  //Manipulate data
  sendData[7] = byteCounter[0]; //MSG ID
  console.log("Device IDs " + deviceIDs.toString('hex'));
  if(demoCounterCounter %2 === 0){// Even
    console.log("Even");
    sendData[5] = deviceIDs[0];
    sendData[6] = deviceIDs[1];
  }
  else{ //odd
    console.log("Odd");
    sendData[5] = deviceIDs[2];
    sendData[6] = deviceIDs[3];
  }
  demoCounterCounter++;
  sendData[10] = byteCounter[0];
  //console.log("Send " + counter + " messages, sending msg with ID: 0x" + sendData[0].toString(16) + " and byteCounter: 0X" + byteCounter.toString('hex'));
  stopwatch.start();
  advertise(sendData.toString('hex'));
  io.emit('sendinfo', sendData.readUInt8(10) + "," + sendData.slice(5, 7).toString('hex'));
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
    advertise(msg); //Broadcast advertise data to raspis
  });
});

io.on('connection', function(socket){
  socket.on('toggle131', function(msg){
    console.log("Toggle 131: " + msg);
    console.log("connectedClients: " + connectedClients.length);
    if(connectedClients.length > 0){
      for(var i = 0; i < connectedClients.length; i++){
        console.log("Searching: " + connectedClients[i].request.connection.remoteAddress);
        if(connectedClients[i].request.connection.remoteAddress.substr(-3) === "131"){
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
        if(connectedClients[i].request.connection.remoteAddress.substr(-3) === "171"){
          connectedClients[i].emit('changeEnableState', msg);
          break;
        }
      } 
    }
  });
});

function advertise(stringData){
  if(stringData.length === MESSAGE_LEN){
    var data = Buffer.from(stringData, 'hex');
    hash = createHash(stringData);
    for(index = 0; index < 8 ; index++){
      data[15 + index] = hash[index];
    }
    data[data.length - 1] = 0x0F;
    data = encrypt(data);
    console.log("Advertise: " + data.toString('hex')); 
    io.emit('advertisedata', data.toString('hex')); //Broadcast advertise data to raspis
  }
}

function createHash(stringData){
    var data = Buffer.from(stringData, 'hex');
    var offsetData = Buffer.alloc(23);
    for(var i = 0; i < 15; i++){
      offsetData[i+8] = data[i];
    }
    var hash = crypto.createHmac('sha256', password)
                   .update(offsetData)
                   .digest('hex');
    var asie = Buffer.from(hash, 'hex');
    //console.log("Data: " + offsetData.toString('hex')); 
    //console.log("Asie: " + asie.toString('hex')); 
    for(index=0; index<8; index++){
      data[15 + index ] = asie[ (asie.length - 1) - index ];
    }
    //console.log("return hash: " + data.slice(15, 15 + 8).toString('hex'));
    return data.slice(15, 15 + 8);
}

function encrypt(buffer){
  var iv = Buffer.alloc(16);
  iv[0] = buffer[0];
  iv[1] = buffer[1];
  iv[2] = buffer[2];

  iv[4] = buffer[3];
  iv[5] = buffer[4];

  toEncBuffer = buffer.slice(5, 15);
  console.log(toEncBuffer.toString('hex'));
  var cipher = crypto.createCipheriv(algorithm,password,iv);
  cipher.setAutoPadding(false);
  var crypted = Buffer.concat([cipher.update(toEncBuffer),cipher.final()]);
  buffer.fill(crypted, 5, 15);
  return buffer;
}
 
function decrypt(buffer){
  var iv = Buffer.alloc(16);
  iv[0] = buffer[0];
  iv[1] = buffer[1];
  iv[2] = buffer[2];

  iv[4] = buffer[3];
  iv[5] = buffer[4];
  toDencBuffer = buffer.slice(5, 15);
  var decipher = crypto.createDecipheriv(algorithm,password,iv);
  decipher.setAutoPadding(false);
  var dec = Buffer.concat([decipher.update(toDencBuffer) , decipher.final()]);
  buffer.fill(dec, 5, 15);
  return buffer;
}

http.listen(8081, function(){ //Listen for connenctions
  console.log('listening on *:8081');
});
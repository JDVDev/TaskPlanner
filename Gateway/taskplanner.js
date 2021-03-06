var noble = require('noble');
var bleno = require('bleno');
var io = require('socket.io-client');
var execFile = require('child_process').execFile;
var sys = require('util')
var exec = require('child_process').exec;
var crypto = require('crypto');

const password = "067835110a97e82dd18f0a39e426a5ef";
var algorithm = 'aes-256-ctr';

const MESSAGE_LEN = 48;
const MESSAGE_OFFSET = 7;
const SERVICE_UUID_16 = "FEF1";
const SERVICE_UUID_128 = "0000" + SERVICE_UUID_16 + "-0000-1000-8000-00805F9B34FB";
const ADVERTISE_TIME = 5000;
var advertiseBuffer = []; //NOT CHECKED FOR OVERFLOW
var advertiseTimeout;
var canAdvertise = false;
var canDiscover = false;
var isConnected = false;
var isEnabled = true;

function puts(error, stdout, stderr){ 
  console.log(stdout)
}
exec("sudo hciconfig hci0 down", puts);
exec("sudo hciconfig hci0 up", puts);
exec("sudo hcitool -i hci0 cmd 0x08 0x000A 00", puts); //Disable advertising
exec("sudo hcitool -i hci0 cmd 0x08 0x0006 A0 00 A0 00 03 00 00 00 00 00 00 00 00 07 00", puts); //Set Advertising prarameters: interval min/max 100ms(0xA000), Advertising type: ADV_NONCONN_IND(0x03), Use all adv channels(0x07)
exec("sudo hcitool -i hci0 cmd 0x08 0x000B 00 00 04 00 04 00 00", puts); //Set Scanning prarameters: 

var socket = io("http://192.168.10.157:8081"); //Setup connection to server

socket.on('advertisedata', function(msg){ //Start advertising received data
    console.log('message: ' + msg);
    var advertisementData = Buffer.alloc(31); // maximum 31 bytes
    /*advertisementData[0] = 0x02; // Upcomming data length. //Old implementation
	  advertisementData[1] = 0x01; // Indicate next byte is type flag.
	  advertisementData[2] = 0x02; // Flag data (General Discoverable).
	  advertisementData[3] = 0x1B; // Upcomming data length.
	  advertisementData[4] = 0xFF; // Manufacture field.
	  advertisementData[5] = 0xFF; // Manufacture ID byte 1
	  advertisementData[6] = 0xFF; // Manufacture ID byte 2*/
    advertisementData[0] = 0x02; // Upcomming data length.
	  advertisementData[1] = 0x01; // Indicate next byte is type flag.
	  advertisementData[2] = 0x02; // Flag data (General Discoverable).
	  advertisementData[3] = 0x1B; // Upcomming data length.
	  advertisementData[4] = 0x16; // Indicate upcomming data is service data
	  advertisementData[5] = 0xF1; // Last 8 bit uuid.
    advertisementData[6] = 0xFE; // Last 8 bit uuid.
    console.log("msg.lenght: " + msg.length);
    if(msg.length === MESSAGE_LEN && canAdvertise){
      console.log("Advertisement: " + advertisementData.toString('hex'));
      var recivedData = Buffer.from(msg, 'hex');

      //FOR DEMO
      recivedData = decrypt(recivedData);
      recivedData[14] = 0x01; //Raspi ID for counter demo, 0x01=171, 0x02=131
      recivedData = encrypt(recivedData);

      console.log("recivedData: " + recivedData.toString('hex'));
      for(var i = 0; i < recivedData.length; i++){
          advertisementData[i + MESSAGE_OFFSET] = recivedData[i];
      }
      console.log("Advertisement: " + advertisementData.toString('hex'));
      if(isEnabled){
        //bleno.startAdvertisingWithEIRData(advertisementData);
        var bufferSize = advertiseBuffer.length; //Save size of og buffer
        console.log("Buffer size: " + bufferSize);
        advertiseBuffer.push(advertisementData); // Add advertisment to buffer
        console.log("Buffer push: " + advertisementData.toString('hex'));
        if(bufferSize <= 0){ // If og buffer size empty start advertising from buffer
          console.log("Buffer size <= 0");
          advertiseFromBuffer(false); //False to indicatie its starting from scratch
        }
      }
    }
});

function advertiseFromBuffer(decrementBuffer){
  if(decrementBuffer){ //If from timeout so from innerfunction
    console.log("Stop advertising");
    bleno.stopAdvertising(); //From innerfuntion so is advertising, stop advertising
    advertiseBuffer = advertiseBuffer.slice(1); //Remove first advertsiment from buffer if comming from innnerfunction
  }
  if(advertiseBuffer.length >= 1){ //Advertise if buffer is greater than 1
    console.log("Buffer size >= 1");

    bleno.startAdvertisingWithEIRData(advertiseBuffer[0]);
    console.log("Advertise: " + advertiseBuffer[0]);

    advertiseTimeout = setTimeout(function(){advertiseFromBuffer(true);}, ADVERTISE_TIME); //setTimeout to advertise next from buffer, check after timeout for buffersize
    console.log("setTimeout: " + advertiseTimeout);
  }
}

socket.on('changeEnableState', function(msg){
  console.log("changeEnableState: " + msg);
  isEnabled = msg;
  if(isEnabled && canDiscover){
    noble.startScanning([SERVICE_UUID_16.toString().toLocaleLowerCase()], true);
  }
  else{
    noble.stopScanning();
    bleno.stopAdvertising();
    clearTimeout(advertiseTimeout);
    advertiseBuffer.length = 0;
  }
  console.log("isEnabled: " + isEnabled);
});

socket.on('connect', function(msg){ //This client connected to server
  console.log("Connect");
  isConnected = true;
  if(canDiscover){
    console.log("Connected -> Discovering");    
    noble.startScanning([SERVICE_UUID_16.toString().toLocaleLowerCase()], true);
  }
  else{
    console.log("Can't discover yet");
  }
});

socket.on('disconnect', function(msg){ //This client lost connection to server
  console.log("Disconnect");
  isConnected = false;
  bleno.stopAdvertising();
  noble.stopScanning();
  clearTimeout(advertiseTimeout);
  advertiseBuffer.length = 0;
});

noble.on('stateChange', function(state) { //Start listening when bluetooth adapter in noble is ready
  if (state === 'poweredOn') {
    canDiscover = true;
    console.log("Can discover");
    if(isConnected){
      console.log("Powered on -> Discovering");
      noble.startScanning([SERVICE_UUID_16.toString().toLocaleLowerCase()], true);
    }
    else{
      console.log("Not connected yet");
    }
  }
  if (state === 'poweredOff') {
    console.log("State poweredOff stop scanning");
    canDiscover = false;
    noble.stopScanning();
  }
});

bleno.on('stateChange', function(state) { //Set can advertise when bluetooth adapter in bleno is ready
  if (state === 'poweredOn') {
    canAdvertise = true;
  }
  if (state === 'poweredOff') {
    console.log("State poweredOff stop advertising");
    bleno.stopAdvertising();
    canAdvertise = false;
  }
});

noble.on('discover', function(peripheral) {
  if(peripheral.advertisement.serviceData.length === 1){
    if(peripheral.advertisement.serviceData[0].data) { //Filter packets that have service data
      if(isEnabled){
        //console.log("Sending: " + peripheral.advertisement.serviceData[0].data.toString('hex'));
        socket.emit('discoverdata', peripheral.advertisement.serviceData[0].data.toString('hex')); //Send data to the server
      }
    }
  }
});

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
$(document).ready(function() {
    $("#gameboard").height($(document).height());
    $("#playerblue").height($("#gameboard").height() / 3);
    $("#playerpurple").height($("#gameboard").height() / 3);
    $("#playerred").height($("#gameboard").height() / 3);
    $('#playerblue').css('line-height', $('#playerblue').height() + "px");
    $('#playerpurple').css('line-height', $('#playerpurple').height() + "px");
    $('#playerred').css('line-height', $('#playerred').height() + "px");

    overlayText('Please join a team');

    var socket = io();
    var toggle131 = false;
    var toggle171 = false;
    var tapcounterBlue = 0;
    var tapcounterPurple = 0;
    var tapcounterRed = 0;
    var IDBlue = new Uint8Array(2);
    var IDPurple = new Uint8Array(2);
    var IDRed = new Uint8Array(2);
    var tapImpact = $('#gameboard').height() / 20;
    var readyToPlay = false;
    var playerAmountInGame = 0;
    var playersJoined = 0;
    var audioElement = $("#audioEffects").get(0);
    var audioElementBackground = $("#audioBackground").get(0);

    socket.emit('advertisedata',"fbbf000000030100000000000000000000000000");

    socket.on('tapper', function(msg){
      var tapData = hexToBytes(msg);
      if(tapData[7] === 0x01){
        if((tapData[0] === IDBlue[0] && tapData[1] === IDBlue[1]) && readyToPlay){
          playSound("furp.wav");          
          tapcounterBlue++;
          $('#playerblue').height($('#playerblue').height() + tapImpact);
          $('#playerpurple').height($('#playerpurple').height() - tapImpact / (playerAmountInGame - 1));
          $('#playerred').height($('#playerred').height() - tapImpact / (playerAmountInGame - 1));

          $('#playerblue').css('line-height', $('#playerblue').height() + "px");
          $('#playerpurple').css('line-height', $('#playerpurple').height() + "px");          
          $('#playerred').css('line-height', $('#playerred').height() + "px");

          if($('#playerblue').height() >= $("#gameboard").height()){
            playBackgroundMusic("backgroundresult.wav");                   
            playSound("snd_se_menu_Narration_WinnerIs.wav");
            setTimeout(function(){playSound("snd_se_menu_Narration_TeamBlue.wav");}, 2000); 
            $('#gameboard').html(IDBlue[0].toString(16).toUpperCase() + IDBlue[1].toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "blue");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");

            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = 0xFB;
            sendData[1] = 0xBF;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x00; ///set broadcast
            sendData[4] = tapData[4] + 0x10; //copy ID
            sendData[5] = 0x01; //Type data
            sendData[6] = 0x07; //Tapper demo
            sendData[7] = 0x20; //Won game
            sendData[8] = IDBlue[0]; //Blue id won
            sendData[9] = IDBlue[1]; //Blue id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
        if((tapData[0] === IDRed[0] && tapData[1] === IDRed[1]) && readyToPlay){
          playSound("plop.wav");          
          tapcounterRed++;
          $('#playerred').height($('#playerred').height() + tapImpact);
          $('#playerpurple').height($('#playerpurple').height() - tapImpact / (playerAmountInGame - 1));
          $('#playerblue').height($('#playerblue').height() - tapImpact / (playerAmountInGame - 1));

          $('#playerred').css('line-height', $('#playerred').height() + "px");
          $('#playerpurple').css('line-height', $('#playerpurple').height() + "px");          
          $('#playerblue').css('line-height', $('#playerblue').height() + "px");

          if($('#playerred').height() >= $("#gameboard").height()){
            playBackgroundMusic("backgroundresult.wav");                   
            playSound("snd_se_menu_Narration_WinnerIs.wav");
            setTimeout(function(){playSound("snd_se_menu_Narration_TeamRed.wav");}, 2000);     
            $('#gameboard').html(IDRed[0].toString(16).toUpperCase() + IDRed[1].toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "red");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");
            
            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = 0xFB;
            sendData[1] = 0xBF;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x00; ///set broadcast
            sendData[4] = tapData[4] + 0x10; //copy ID
            sendData[5] = 0x01; //Type data
            sendData[6] = 0x07; //Tapper demo
            sendData[7] = 0x20; //Won game
            sendData[8] = IDRed[0]; //Red id won
            sendData[9] = IDRed[1]; //Red id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
        if((tapData[0] === IDPurple[0] && tapData[1] === IDPurple[1]) && readyToPlay){
          playSound("fart.wav");                    
          tapcounterPurple++;
          $('#playerpurple').height($('#playerpurple').height() + tapImpact);          
          $('#playerred').height($('#playerred').height() - tapImpact / (playerAmountInGame - 1));
          $('#playerblue').height($('#playerblue').height() - tapImpact / (playerAmountInGame - 1));

          $('#playerpurple').css('line-height', $('#playerpurple').height() + "px");
          $('#playerred').css('line-height', $('#playerred').height() + "px");
          $('#playerblue').css('line-height', $('#playerblue').height() + "px");

          if($('#playerpurple').height() >= $("#gameboard").height()){
            playBackgroundMusic("backgroundresult.wav");       
            playSound("snd_se_menu_Narration_WinnerIs.wav");
            setTimeout(function(){playSound("snd_se_menu_Narration_TeamGreen.wav");}, 2000); 
            $('#gameboard').html(IDPurple[0].toString(16).toUpperCase() + IDPurple[1].toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "lightgreen");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");
            
            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = 0xFB;
            sendData[1] = 0xBF;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x00; ///set broadcast
            sendData[4] = tapData[4] + 0x10; //copy ID
            sendData[5] = 0x01; //Type data
            sendData[6] = 0x07; //Tapper demo
            sendData[7] = 0x20; //Won game
            sendData[8] = IDPurple[0]; //Purple id won
            sendData[9] = IDPurple[1]; //Purple id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
      }
      if(readyToPlay){
        if($('#playerblue').height() <= 0){
            IDBlue[0] = 0x00;
            IDBlue[1] = 0x00;
            $('#playerblue').remove();
            playerAmountInGame--;
        }
        if($('#playerpurple').height() <= 0){ 
            IDPurple[0] = 0x00;
            IDPurple[1] = 0x00;
            $('#playerpurple').remove();   
            console.log("Gameover purple");         
            playerAmountInGame--;
        }
        if($('#playerred').height() <= 0){
             IDRed[0] = 0x00;
             IDRed[1] = 0x00;
            $('#playerred').remove();             
             playerAmountInGame--;
        }
      }
    });
    socket.on('setplayer', function(msg){
      console.log("Setplayer");
      var data = hexToBytes(msg);
      console.log("Data: " + data);
      var sendData = new Uint8Array(20);
      sendData[0] = 0xFB;
      sendData[1] = 0xBF;
      sendData[2] = data[0]; //set senderID as reciever
      sendData[3] = data[1]; //set senderID as reciever
      sendData[4] = data[4] + 0x10; //copy ID
      sendData[5] = 0x01; //Type data
      sendData[6] = 0x07; //Tapper demo
      
      if(data[7] === 0x10){
        if(IDBlue[0] === 0x00 && IDBlue[1] === 0x00){
          playSound("furp.wav");          
          IDBlue[0] = data[0];
          IDBlue[1] = data[1];
          sendData[7] = data[7]; //Joined team blue
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          console.log("Joined blue sending joined blue");
          $('#playerblue').text(IDBlue[0].toString(16).toUpperCase() + IDBlue[1].toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[7] = 0x40;
            sendData[8] = data[7]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
      if(data[7] === 0x11){
        if(IDRed[0] === 0x00 && IDRed[1] === 0x00){
          playSound("plop.wav");          
          IDRed[0] = data[0];
          IDRed[1] = data[1];
          sendData[7] = data[7]; //Joined team red
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          $('#playerred').text(IDRed[0].toString(16).toUpperCase() + IDRed[1].toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[7] = 0x40;
            sendData[8] = data[7]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
    if(data[7] === 0x12){
        if(IDPurple[0] === 0x00 && IDPurple[1] === 0x00){
          playSound("fart.wav");
          IDPurple[0] = data[0];                    
          IDPurple[1] = data[1];
          sendData[7] = data[7]; //Joined team pruple
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          $('#playerpurple').text(IDPurple[0].toString(16).toUpperCase() + IDPurple[1].toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[7] = 0x40;
            sendData[8] = data[7]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
    });
    socket.on('playerready', function(msg){
      var data = hexToBytes(msg);
      console.log("Player ready: " + msg.toString('hex'));
      if(data[7] === 0x50){
        playersJoined++
        if(playersJoined === 3 && readyToPlay === false){   
            playerAmountInGame = 3;
            console.log("Player amount " + playerAmountInGame.toString());
            playSound("snd_se_narration_Ready.wav");
            overlayText('Get ready!');
            setTimeout(function(){
                playSound("snd_se_narration_three.wav");
                overlayText('3');
                setTimeout(function(){
                    playSound("snd_se_narration_two.wav");                  
                    overlayText('2');
                    setTimeout(function(){
                        playSound("snd_se_narration_one.wav");                        
                        overlayText('1');
                        setTimeout(function(){
                            playBackgroundMusic("backgroundbattlemusic.wav");                          
                            playSound("snd_se_narration_Go.wav");                            
                            overlayText('GO!!');
                            readyToPlay = true;
                            setTimeout($.unblockUI, 300);
                        }, 1000);
                    }, 1000);
                }, 1000);
            }, 1000);
        }
        console.log("Player joined action " + playersJoined.toString());
      }
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
      socket.emit('advertisedata',"fbbf0000ff03ff00000000000000000000000000");
    };
    function hexToBytes(hex) {
      for (var bytes = [], c = 0; c < hex.length; c += 2)
      bytes.push(parseInt(hex.substr(c, 2), 16));
      return bytes;
    }
    function bytesToHex(bytes) {
        for (var hex = [], i = 0; i < bytes.length; i++) {
            hex.push((bytes[i] >>> 4).toString(16));
            hex.push((bytes[i] & 0xF).toString(16));
        }
        return hex.join("");
    }
    function overlayText(message) {
        $.blockUI({ 
            message: message,
            css: { 
                border: 'none',
                backgroundColor: 'rgba(0,0,0,0)', 
                color: '#fff',
                'font-size': '10vh'
            } 
        });
    }
    function playSound(filename){
      audioElement.setAttribute('src', 'sounds/' + filename);
      audioElement.addEventListener("canplay",function(){
        audioElement.play();
      });
    }
    function playBackgroundMusic(filename){
      audioElementBackground.setAttribute('src', 'sounds/' + filename);
      $("#audioBackground").prop('volume', 0.33);
      audioElementBackground.addEventListener('ended', function() {
          this.play();
      }, false);
      audioElement.addEventListener("canplay",function(){
        audioElementBackground.play();
      });
    }
    playSound("snd_se_menu_Narration_TeamBattle.wav");
    playBackgroundMusic("backgroundteamselect.wav");
});
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
    var IDBlue = 0x00;
    var IDPurple = 0x00;
    var IDRed = 0x00;
    var tapImpact = $('#gameboard').height() / 20;
    var readyToPlay = false;
    var playerAmountInGame = 0;
    var playersJoined = 0;
    var audioElement = $("#audioEffects").get(0);
    var audioElementBackground = $("#audioBackground").get(0);

    socket.emit('advertisedata',"00fb000301000000000000000000000000000000");

    socket.on('tapper', function(msg){
      var tapData = hexToBytes(msg);
      if(tapData[5] === 0x01){
        if(tapData[1] === IDBlue && readyToPlay){
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
            $('#gameboard').html(IDBlue.toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "blue");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");

            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = tapData[0] + 0x10; //copy ID
            sendData[1] = 0xFB;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x01; //Type data
            sendData[4] = 0x07; //Tapper demo
            sendData[5] = 0x20; //Won game
            sendData[6] = IDBlue; //Blue id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
        if(tapData[1] === IDRed && readyToPlay){
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
            $('#gameboard').html(IDRed.toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "red");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");
            
            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = tapData[0] + 0x10; //copy ID
            sendData[1] = 0xFB;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x01; //Type data
            sendData[4] = 0x07; //Tapper demo
            sendData[5] = 0x20; //Won game
            sendData[6] = IDRed; //Red id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
        if(tapData[1] === IDPurple && readyToPlay){
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
            $('#gameboard').html(IDPurple.toString(16).toUpperCase() + " Won!");
            $('#gameboard').css("background", "lightgreen");
            $('#gameboard').css('line-height', $('#gameboard').height() + "px");
            
            readyToPlay = false;
            var sendData = new Uint8Array(20);
            sendData[0] = tapData[0] + 0x10; //copy ID
            sendData[1] = 0xFB;
            sendData[2] = 0x00; //set broadcast
            sendData[3] = 0x01; //Type data
            sendData[4] = 0x07; //Tapper demo
            sendData[5] = 0x20; //Won game
            sendData[6] = IDPurple; //Red id won
            console.log("Sending advertisment: " + bytesToHex(sendData));
            socket.emit('advertisedata', bytesToHex(sendData));
            console.log("Sending red won");
          }
        }
      }
      if(readyToPlay){
        if($('#playerblue').height() <= 0){
            IDBlue = 0x00;
            $('#playerblue').remove();
            playerAmountInGame--;
        }
        if($('#playerpurple').height() <= 0){ 
            IDPurple = 0x00;
            $('#playerpurple').remove();   
            console.log("Gameover purple");         
            playerAmountInGame--;
        }
        if($('#playerred').height() <= 0){
             IDRed = 0x00;
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
      sendData[0] = data[0] + 0x10; //copy ID
      sendData[1] = 0xFB;
      sendData[2] = data[1]; //set senderID as reciever
      sendData[3] = 0x01; //Type data
      sendData[4] = 0x07; //Tapper demo
      if(data[5] === 0x10){
        if(IDBlue === 0x00){
          playSound("furp.wav");          
          IDBlue = data[1];
          sendData[5] = data[5]; //Joined team blue
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          console.log("Joined blue sending joined blue");
          $('#playerblue').text(IDBlue.toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[5] = 0x40;
            sendData[6] = data[5]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
      if(data[5] === 0x11){
        if(IDRed === 0x00){
          playSound("plop.wav");          
          IDRed = data[1];
          sendData[5] = data[5]; //Joined team red
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          $('#playerred').text(IDRed.toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[5] = 0x40;
            sendData[6] = data[5]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
    if(data[5] === 0x12){
        if(IDPurple === 0x00){
          playSound("fart.wav");          
          IDPurple = data[1];
          sendData[5] = data[5]; //Joined team pruple
          console.log("Sending advertisment: " + bytesToHex(sendData));
          socket.emit('advertisedata', bytesToHex(sendData));
          $('#playerpurple').text(IDPurple.toString(16).toUpperCase());
        }
        else{
            console.log("Send team full");
            sendData[5] = 0x40;
            sendData[6] = data[5]; 
            socket.emit('advertisedata', bytesToHex(sendData));
        }
      }
    });
    socket.on('playerready', function(msg){
      var data = hexToBytes(msg);
      console.log("Player ready: " + msg.toString('hex'));
      if(data[5] === 0x50){
        playersJoined++
        playerAmountInGame++;
        console.log("Player amout " + playerAmountInGame.toString());
        if(playersJoined === 3 && readyToPlay === false){
            playerAmountInGame = 3;
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
      socket.emit('advertisedata',"fffb0003ff000000000000000000000000000000");
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
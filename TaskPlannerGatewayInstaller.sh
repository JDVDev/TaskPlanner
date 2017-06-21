#apt install vsftpd
#bash -c 'echo "write_enable=YES" >> /etc/vsftpd.conf'
#systemctl restart vsftpd
apt install -y nodejs
apt install -y nodejs-legacy
apt install -y build-essential
apt install -y npm
npm install socket.io
apt install -y bluetooth bluez libbluetooth-dev libudev-dev
systemctl stop bluetooth
systemctl disable bluetooth
npm install noble
npm install bleno
mkdir ~/Development
cd ~/Development
wget https://raw.githubusercontent.com/JDVDev/TaskPlanner/master/Gateway/taskplanner.service
wget https://raw.githubusercontent.com/JDVDev/TaskPlanner/master/Gateway/taskplanner.js
mv taskplanner.service /etc/systemd/system/
chmod 664 /etc/systemd/system/taskplanner.service
systemctl enable taskplanner
systemctl start taskplanner

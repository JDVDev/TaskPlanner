apt install -y bluetooth bluez libbluetooth-dev libudev-dev
systemctl stop bluetooth
systemctl disable bluetooth
npm install socket.io
npm install noble
npm install bleno
mv taskplanner.service /etc/systemd/system/
chmod 664 /etc/systemd/system/taskplanner.service
systemctl enable taskplanner
systemctl start taskplanner

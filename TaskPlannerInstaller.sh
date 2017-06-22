#!/bin/bash
if [[ $# -eq 0 ]]; then
	echo "Need one product as argument. gateway or server"
	exit 1
fi
if [[ $1 != 'gateway' ]] && [[ $1 != 'server' ]]; then
	echo "Need one product as argument. gateway or server"
	exit 1
fi
apt install vsftpd
bash -c 'echo "write_enable=YES" >> /etc/vsftpd.conf'
systemctl restart vsftpd
curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
apt install -y nodejs
apt install -y nodejs-legacy
apt install -y build-essential
apt install -y npm
rm -rf ~/TaskPlanner
mkdir ~/TaskPlanner
cd ~/TaskPlanner
releaseURL=$(curl -L https://raw.githubusercontent.com/JDVDev/TaskPlanner/master/releasecontenturl)
wget $releaseURL
unzip TaskPlanner* -d unpack
rm -f *.zip
cd unpack/TaskPlanner*
mv * ../../
cd ../../
rm -rf unpack
rm -rf Android
if [[ $1 == 'gateway' ]]; then
	cd Gateway
	chmod a+x TaskPlannerGatewayInstaller.sh
	bash TaskPlannerGatewayInstaller.sh
	cd ..
	rm -rf Server
fi
if [[ $1 == 'server' ]]; then
	cd Server
	chmod a+x TaskPlannerServerInstaller.sh
	bash TaskPlannerServerInstaller.sh
	cd ..
	rm -rf Gateway
fi
rm -f *

FROM centos:latest

RUN yum install -y firefox java which xorg-x11-server-Xvfb \
	&& dbus-uuidgen > /etc/machine-id \
#	&& mkdir /root/selena \
	&& rm /etc/localtime \
	&& ln -s /usr/share/zoneinfo/America/Chicago /etc/localtime

#COPY 100.sh /root/selena/
#COPY selena.jar /root/selena/


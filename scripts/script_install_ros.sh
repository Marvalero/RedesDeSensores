#!/bin/bash

#Install ROS Indigo

echo "Instalando ROS..."
sudo sh -c 'echo "deb http://packages.ros.org/ros/ubuntu $(lsb_release -sc) main" > /etc/apt/sources.list.d/ros-latest.list'

wget https://raw.githubusercontent.com/ros/rosdistro/master/ros.key -O - | sudo apt-key add -

sudo apt-get update

sudo apt-get -y install ros-indigo-desktop-full

apt-cache search ros-indigo

sudo rosdep init
rosdep update

echo "source /opt/ros/indigo/setup.bash" >> ~/.bashrc
source ~/.bashrc

source /opt/ros/indigo/setup.bash

sudo apt-get -y install python-rosinstall




#Install Freenect

echo "Instalando Freenect..."
sudo apt-get -y install libfreenect-dev
sudo apt-get -y install ros-indigo-freenect-launch
sudo apt-get -y install libsqlite3-dev libpcl-1.7-all libfreenect-dev libopencv-dev 

sudo apt-get -y install ros-indigo-rtabmap-ros


#Install Octomap

echo "Instalando Octomap..."
sudo apt-get -y install ros-indigo-octomap


#Install Rviz

echo "Instalando Rviz..."
sudo apt-get -y install ros-irosmake rvizndigo-rviz

rosdep -y install rviz

rosmake rviz

echo "INSTALACIÓN COMPLETADA"






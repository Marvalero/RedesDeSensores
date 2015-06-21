#!/bin/bash

#Launch ros

echo "Ejecutando roscore..."
roscore &

#Launch kinect

echo "Lanzando kinect..."
roslaunch freenect_launch freenect.launch depth_registration:=true &

#Launch rviz

echo "Lanzando rviz..."

#roslaunch rtabmap_ros demo_robot_mapping.launch rviz:=true
#rosbag play --clock demo_mapping.bag & rtabmapviz:=false &
#./script_serial_to_topic.sh &


rosbag play --clock demo_mapping.bag &
roslaunch rtabmap_ros demo_robot_mapping.launch rviz:=true rtabmapviz:=false 




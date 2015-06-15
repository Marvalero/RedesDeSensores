cmake_minimum_required(VERSION 2.8.3)
project(redes_sensores)

## Find catkin macros and libraries
## if COMPONENTS list like find_package(catkin REQUIRED COMPONENTS xyz)
## is used, also find other catkin packages
find_package(catkin REQUIRED COMPONENTS
  roscpp
  rospy
  std_msgs
  genmsg 
  visualization_msgs
)


## Declare ROS messages and services
add_message_files( FILES Num.msg)

## Generate added messages and services
generate_messages(DEPENDENCIES std_msgs)
## System dependencies are found with CMake's conventions
# find_package(Boost REQUIRED COMPONENTS system)


catkin_package(
#  INCLUDE_DIRS include
#  LIBRARIES serial_to_topic
#  CATKIN_DEPENDS roscpp rospy std_msgs
#  DEPENDS system_lib
)

###########
## Build ##
###########

## Specify additional locations of header files
## Your package locations should be listed before other locations
# include_directories(include)
include_directories(
  ${catkin_INCLUDE_DIRS}
)



add_executable(temperature_markers_publisher src/temperature_markers_publisher.cpp)
target_link_libraries(temperature_markers_publisher ${catkin_LIBRARIES})

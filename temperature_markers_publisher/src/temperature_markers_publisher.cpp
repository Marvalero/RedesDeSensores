// temperature_markers_publisher
#include <ros/ros.h>
#include <visualization_msgs/Marker.h>
#include <nav_msgs/Odometry.h>
#include "std_msgs/Int16.h"
#include <math.h>
#include <string.h>


double globalx=0;
double globaly=0;

double globalxAnterior = 50;
double globalyAnterior = 50;

int count = 0;
int temperature = 0;
int temperatureRef = 0;
int distanciaMin = 50;
int distanciaEntreMedidas;

void odomCallBack(const nav_msgs::Odometry::ConstPtr& msg)
{
    globalx=msg->pose.pose.position.x;
    globaly=msg->pose.pose.position.y;
    ROS_INFO("----  Se ha leido posicion: %g %g ", globalx, globaly);
}

void tempCallBack(const std_msgs::Int16::ConstPtr& msg){

    ROS_INFO("----  Se ha leido temperatura: %d ", msg->data);

    temperature = msg->data;

  ros::NodeHandle n;
  ros::Publisher marker_pub = n.advertise<visualization_msgs::Marker>("visualization_marker", 1);

  uint32_t shape = visualization_msgs::Marker::CUBE;

    visualization_msgs::Marker marker;
  
    marker.header.frame_id = "/my_frame";
    marker.header.stamp = ros::Time::now();

    marker.ns = "";
    marker.id = count;
    count++;

    if (count == 1){
         temperatureRef = temperature;
    }

    marker.type = shape;

    marker.action = visualization_msgs::Marker::ADD;

    marker.pose.position.x = globalx;
    marker.pose.position.y = globaly;
    marker.pose.position.z = 0;
    marker.pose.orientation.x = 0.0;
    marker.pose.orientation.y = 0.0;
    marker.pose.orientation.z = 0.0;
    marker.pose.orientation.w = 1.0;

    marker.scale.x = 1;
    marker.scale.y = 1;
    marker.scale.z = 1;

    if (temperature > (1.01*temperatureRef)){
        marker.color.r = (temperatureRef/temperature);
        marker.color.g = (1-(temperatureRef/temperature));
        marker.color.b = 0;
        marker.color.a = 1.0;
    }
    else if (temperature < (0.99*temperatureRef)){
        marker.color.r = 0;
        marker.color.g = ((temperature/temperatureRef)-0.2);
        marker.color.b = (temperature/temperatureRef);
        marker.color.a = 1.0;
    }
    else {
        marker.color.r = 1;
        marker.color.g = 1;
        marker.color.b = 0;
        marker.color.a = 1.0;
    }

    marker.lifetime = ros::Duration();

    distanciaEntreMedidas = sqrt(((globalx-globalxAnterior)*(globalx-globalxAnterior))+((globaly-globalyAnterior)*(globaly-globalyAnterior))) ;

  if(distanciaEntreMedidas > distanciaMin ||  distanciaEntreMedidas < -distanciaMin){
    marker_pub.publish(marker);
    ROS_INFO("Se acaba de escribir en el topic: %g %g", marker.pose.position.x,marker.pose.position.y);


    globalxAnterior=globalx;
    globalyAnterior=globaly;
  }else{
    ROS_INFO("Medidas demasiado cercanas");
  }


}


int main( int argc, char** argv )
{
  ros::init(argc, argv, "temperature_markers_publisher");
 
  ros::NodeHandle n;
  ros::Publisher marker_pub = n.advertise<visualization_msgs::Marker>("visualization_marker", 1);
 
  ros::NodeHandle nh_("~");
  ros::Subscriber sub_image_1 = nh_.subscribe<nav_msgs::Odometry>("/az3/base_controller/odom", 1, &odomCallBack);

  ros::Subscriber sub_image_2 = nh_.subscribe<std_msgs::Int16>("/temp_monitor", 10, &tempCallBack);

  globalx=0;
  globaly=0;


  ros::spin();
}

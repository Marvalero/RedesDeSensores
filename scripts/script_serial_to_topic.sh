#!/bin/sh

sudo chown `whoami` /dev/ttyUSB0;

rosrun serial_to_topic serial1; 



#!/bin/bash

echo "Eliminando archivos .class";

rm *.class

echo "Eliminando mensajes de protocolo";
rm InfoMsg.*

echo "Compilando..."
make telosb

java TestSerial -comm serial@`ls /dev/ttyUSB*`:115200 ;



#!/bin/bash

#######################################################
# (Des)comentar si no tienes instalado TinyOS 2.x en tu sistema
#######################################################
#echo "Eliminando archivos .class";
#rm *.class

#echo "Eliminando mensajes de protocolo";
#rm InfoMsg.*

#echo "Compilando..."
#make telosb

# Comando utilizado para ejecutar el programa java
#java TestSerial -comm serial@`ls /dev/ttyUSB*`:115200 ;
#######################################################

# (Des)comenta la siguiente línea para la ejecución del programa
java -cp tinyos.jar: TestSerial -comm serial@`ls /dev/ttyUSB*`:115200;




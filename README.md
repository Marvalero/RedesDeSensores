# RedesDeSensores
Trabajo realizado para la asignatura Redes de Sensores para el Sistema Operativo ROS. Tiene tres herramientas principales: 
 - cmd_vel_publisher
 - serial_to_topic
 - temperature_markers_publisher

# cmd_vel_publisher
Programa usado para mover al robot de forma manual. Recibe por teclado un comando y en función de este escribe una orden en el topic /RosAria/cmd_vel. Opciones:
       - a: gira a la izquierda
       - d: gira a la derecha
       - s: se para
       - w: va para adelante
       - r : va marcha atrás
       - q: cierra la aplicación

Ejecución:  rosrun cmd_vel_publisher cmd_vel_publisher_node

# serial_to_topic
Programa que lee del puerto serie y escribe en el topic "/temp_monitor"

Ejecución: ./temp_publisher com.github.rosjava.serial_to_topic.temp_publisher.Talker

# temperature_markers_publisher
Este nodo está programado en c++, se subscribe tanto a /temp_monitor para leer las medidas de temperatura que publica serial_to_topic como a /RosAria/pose para leer la posición. 

Usa el callback de la lectura de /temp_monitor para publicar en /visualization_marker un cubo en la posición actual del robot y cuyo color depende de la temperatura leída. Se puede configurar para que no publique de nuevo hasta que se haya recorrido una distancia mínima.

Ejecución: rosrun redes_sensores temperature_markers_publisher

/*
 * Copyright (C) 2014 SLAM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.rosjava.serial_to_topic.temp_publisher;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.io.*;
import java.net.*;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
public class Talker extends AbstractNodeMain {

  private ServerSocket ss;
  private Socket so;
  private DataOutputStream salida;
  private DataInputStream entrada;
  private String mensajeRecibido;

  final int PUERTO = 5000;

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/talker");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    final Publisher<std_msgs.Int16> publisher =
        connectedNode.newPublisher("temp_monitor", std_msgs.Int16._TYPE);
    // This CancellableLoop will be canceled automatically when the node shuts
    // down.
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private short sequenceNumber;

      @Override
      protected void setup() {
        sequenceNumber = 1000;

        mensajeRecibido = "";

        try{
          ss = new ServerSocket(PUERTO);
          so = new Socket();

          System.out.println("Esperando conexión");
          so = ss.accept();
          System.out.println("Se ha conectado un cliente");

          entrada = new DataInputStream(so.getInputStream());
          salida = new DataOutputStream(so.getOutputStream());

        }catch(Exception e){
            System.out.println("Error!");
        }
      }

      @Override
      protected void loop() throws InterruptedException {
        short dato = 0;

        try{
          dato = entrada.readShort();
        }catch(Exception e){
          System.out.println("Error al recibir el mensaje");
        }

        System.out.println("Mensaje recibido : " + dato);
	if(dato > 0){
	        std_msgs.Int16 temp = publisher.newMessage();
	         temp.setData(dato);
        	publisher.publish(temp);
	}
        //sequenceNumber++;
        //Thread.sleep(5000);
      }
    });
  }
}

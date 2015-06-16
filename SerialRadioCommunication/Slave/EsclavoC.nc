#include <Timer.h>
#include <UserButton.h>
#include "EsclavoApp.h"

// Estos son los módulos que usaremos (interfaces)
module EsclavoC {

	// Arranque del sistema
  uses interface Boot;

	// Interfaz para los leds
  uses interface Leds;

	// Interfaz para envio y recepción de paquetes radio
  uses interface Packet;
  uses interface AMPacket;
  uses interface AMSend;
  uses interface Receive;
  uses interface SplitControl as AMControl;

	// Interfaz de sensores
  uses interface Read<uint16_t> as ReadVisible;
  uses interface Read<uint16_t> as ReadNotVisible;
  uses interface Read<uint16_t> as Temperature;
  uses interface Read<uint16_t> as Humidity;
  uses interface CC2420Packet;
	
	// Interfaz de temporizador
  uses interface Timer<TMilli> as Timer0;
  
	// Interfaz para el botón de usuario
  uses interface Get<button_state_t>;
  uses interface Notify<button_state_t>;
  

}

implementation {

  // Variables globales
  message_t pkt;
  
  // Variable que indica cuando el dispositivo está ocupado
  bool busy = FALSE;
  
  // Variable para tomar el valor de la humedad
  uint16_t H;
 
  // Variable para tomar el valor de la temperatura
  uint16_t T;
 
  // Variable para tomar el valor de la luminosidad
  uint16_t S;
  
  // Variable para tomar como referencia el valor del nodo maestro
  uint16_t nodoMaestro;
  
  // Indica que se encuentra en sincronización
  bool SYNC_STATE = FALSE;
  
  // Indica que se encuentra preparado
  bool READY_STATE = FALSE;
  
  // Indica que se encuentra en espera
  bool WAIT_STATE = FALSE;
  
  // Indica que se encuentra en el estado inicial
  bool INIT_STATE = FALSE;
  
  // Indica que se encuentra en el estado de disparo
  bool TRIGGER_STATE = FALSE;
  
  // Variable que indica el intervalo TDMA (ms)
  uint16_t intervalo_TDMA = 0;
  
  // Variable que indica la medida RSSI
  uint16_t measureRssi = 0;
  
  // Variable que indica la medida
  uint16_t medida;
  
  // Variable que indica el tipo de medida
  uint16_t tipoMedida;

  // Evento que se dispara cuando termina de leer la humedad
  event void Humidity.readDone(error_t result, uint16_t val){
	// Asignamos el valor para la humedad
	H = val;
  }

  // Evento que se dispara cuando termina de leer la temperatura
  event void Temperature.readDone(error_t result, uint16_t val){
	// Asignamos el valor para la temperatura
	T = val;
  }

  // Evento que se dispara cuando termina de leer la intensidad lumínica
  event void ReadNotVisible.readDone(error_t result, uint16_t val){
	// Asignamos el valor para la luminosidad
	S = val;
  }

  // Evento que se dispara cuando termina de leer la intensudad lumínica visible
  event void ReadVisible.readDone(error_t result, uint16_t val){
	// Asignamos el valor para la luminosidad
	S = val;
  }

	void getMeasureMean(uint16_t tipoMedidaSolicitada){
	
		if(tipoMedidaSolicitada == 0){	// Humedad
			call Humidity.read();				
			medida = H;
		}else if(tipoMedidaSolicitada == 1){	// Temperatura
			call Temperature.read();
			medida = T;
		}else if(tipoMedidaSolicitada == 2){	// Luminosidad
			call ReadVisible.read();
			medida = S;
		}
		
	}

  // Función que obtiene el RSSI
  uint16_t getRssi(message_t *msg){
	return (uint16_t) call CC2420Packet.getRssi(msg);
  }
	// Evento cada vez que el Timer0 es disparado
	event void Timer0.fired(){
		// Comprobamos que no esté ocupado el nodo
		if(!busy){
			// Creamos un nuevo mensaje
			InfoMsg *respmsg = (InfoMsg *)(call Packet.getPayload(&pkt, sizeof(InfoMsg)));	
			// Comprobamos que no es NULL en caso de serlo salimos de la función
			if(respmsg == NULL){
				return;
			}
			
		// Asignamos el nodo origen
		respmsg->SourceNode = TOS_NODE_ID;
		
		// Asignamos el nodo destino
		respmsg->DestinationNode = nodoMaestro;
				
		if(SYNC_STATE){
			// Enviamos un mensaje del tipo Hello
			respmsg->TOM = HELLO_TYPE;
			
			// Encendemos el led de SYNC_STATE
			call Leds.led0Toggle();
			call Leds.led1Off();
			call Leds.led2Off();
			
			// Enviamos el mensaje
			if(call AMSend.send(nodoMaestro, &pkt, sizeof(InfoMsg)) == SUCCESS){
				busy = TRUE;
			}
		
		}else if(WAIT_STATE){
		
			// Mensaje del tipo ACK
			respmsg->TOM = ACK_TYPE;
		
			call Leds.led2Toggle();
			call Leds.led0Off();
			call Leds.led1Off();
		
			// Enviamos el mensaje
			if(call AMSend.send(nodoMaestro, &pkt, sizeof(InfoMsg)) == SUCCESS){
				busy = TRUE;
			}
			
		}else if(READY_STATE){
			respmsg->TOM = RESPONSE_TYPE;

			respmsg->datos[0] = tipoMedida;
			respmsg->datos[1] = medida;
			respmsg->datos[2] = measureRssi;
						
			call Leds.led1Toggle();
			call Leds.led0Off();
			call Leds.led2Off();
			
			// Enviamos el mensaje
			if(call AMSend.send(nodoMaestro, &pkt, sizeof(InfoMsg)) == SUCCESS){
				busy = TRUE;
			}
		}
		}// Fin de comprobación de ocupado
		
	}// Fin evento fired
	
	// Evento, cuando a finalizado el arranque
  event void Boot.booted(){
  
	// Habilita las notificaciones
	call Notify.enable();
	
	// Hace una llamada a la radio para encenderla
    call AMControl.start();	
    
    // Paramos el temporizador
    call Timer0.stop();

    // Iniciamos los leds para saber que está en la fase inicial
    call Leds.led0On();
    call Leds.led1On();
    call Leds.led2On();

    // Al iniciar el nodo nos encontramos en el estado inicial
    INIT_STATE = TRUE;
    
  }
  
  
  event void Notify.notify(button_state_t state){
	// Si el botón del usuario es pulsado
	if(state == BUTTON_PRESSED){
		// Encendemos todos los leds
		call Leds.led0On();
		call Leds.led1On();
		call Leds.led2On();
	// Si dejamos de pulsar el botón
	}else if(state == BUTTON_RELEASED){
		// Enviamos un mensaje Hello a todos los nodos para buscar un maestro
		InfoMsg *helloMsg = (InfoMsg *)(call Packet.getPayload(&pkt, sizeof(InfoMsg)));	
		
		// Si el mensaje es nulo retornamos de la función
		if(helloMsg == NULL){
			return;
		}
		
		// Asignamos el nodo origen
		helloMsg->SourceNode = TOS_NODE_ID;
		
		// Asignamos el nodo destino (Todos)
		helloMsg->DestinationNode = AM_BROADCAST_ADDR;
				
		// Enviamos un mensaje del tipo Hello
		helloMsg->TOM = HELLO_TYPE;	
		
		// Enviamos el mensaje
		if(call AMSend.send(nodoMaestro, &pkt, sizeof(InfoMsg)) == SUCCESS){
			busy = TRUE;
		}
		
	}
	
  }

	// Evento, cuando la radio se ha encendido (bien)
  event void AMControl.startDone(error_t err) {
    if (err == SUCCESS) {    
    }
    else {
      call AMControl.start();				// Si no se ha iniciado bien, volvemos a intentar el arranque
    }
  }

	// Evento, se apaga la radio
  event void AMControl.stopDone(error_t err) {
	// Nada que hacer en este caso
  }

	// Evento, cada vez que se ha mandado un mensaje
  event void AMSend.sendDone(message_t* msg, error_t err) {
    if (&pkt == msg) {
      busy = FALSE;

    }
  }

	// Evento, cuando se recibe un mensaje
  event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len){
		
		call Leds.led0Off();
		call Leds.led1Off();
		call Leds.led2Off();
		
		// Si recibimos un mensaje de información
		if(len == sizeof(InfoMsg)){
		
			// Obtenemos el mensaje recibido
			InfoMsg *infomsg = (InfoMsg *)payload;
		
			switch(infomsg->TOM){
				case(SYNC_TYPE):
	
					if(INIT_STATE){
							if(infomsg->DestinationNode == TOS_NODE_ID || infomsg->DestinationNode == AM_BROADCAST_ADDR){
								// Obtenemos el nodo maestro
								nodoMaestro = infomsg->SourceNode;
								
								// Salimos del estado de inicio
								INIT_STATE = FALSE;
								// Entramos en el estado de sincronización
								SYNC_STATE = TRUE;
								
								// Comprobamos si el Timer está en ejecución, en caso de que no
								if(call Timer0.isRunning() == FALSE){
									call Timer0.startPeriodic(TIMER_PERIOD_SYNC);
								}
								
								
							}
					}
				break;
				case(ACK_TYPE):
					// El Ack se envía en el modo sync o espera
					if(WAIT_STATE){
						if(infomsg->DestinationNode == TOS_NODE_ID && infomsg->SourceNode == nodoMaestro){
							WAIT_STATE = FALSE;
							READY_STATE = TRUE;
							
							if(call Timer0.isRunning()){
								call Timer0.stop();
								
								call Leds.led0On();
								call Leds.led1Off();
								call Leds.led2On();
							}
						}
					}
				break;
				case(HANDSHAKE_TYPE):
					// Nos encontramos en el modo sincronización
					if(SYNC_STATE){
						if(infomsg->DestinationNode == TOS_NODE_ID && infomsg->SourceNode == nodoMaestro){
							if(call Timer0.isRunning() == TRUE){
								call Timer0.stop();
							}
							
							SYNC_STATE = FALSE;
							WAIT_STATE = TRUE;
							
								intervalo_TDMA = infomsg->datos[0]*infomsg->datos[1];
							
							call Timer0.startPeriodic(intervalo_TDMA);
						}
					}
				break;
				case(REQUEST_TYPE):
					if(READY_STATE){
						if(infomsg->DestinationNode == AM_BROADCAST_ADDR && infomsg->SourceNode == nodoMaestro){	
							WAIT_STATE = TRUE;
							READY_STATE = FALSE;
							tipoMedida = infomsg->datos[0];
							getMeasureMean(tipoMedida);
							measureRssi = getRssi(msg);
							call Timer0.startPeriodic(intervalo_TDMA);	
						}
					}
				break;
				case(TRIGGER_TYPE):
					if(READY_STATE){
						if(infomsg->DestinationNode == AM_BROADCAST_ADDR && infomsg->SourceNode == nodoMaestro){
							call Timer0.startOneShot(intervalo_TDMA);						
						}
					}
				break;
				case(REJECT_TYPE):
					
					// Detenemos el temporizador
					call Timer0.stop();
					
					// Restablecemos los estados
					INIT_STATE = TRUE;
					WAIT_STATE = FALSE;
					READY_STATE = FALSE;
					SYNC_STATE = FALSE;
					
					// Encendemos todos los leds
					call Leds.led0On();
					call Leds.led1On();
					call Leds.led2On();
					
				break;
			}
			
		}
			
    return msg;
  }
}

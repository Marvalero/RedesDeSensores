#include <Timer.h>
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
	// Interfaz de temporizador
  uses interface Timer<TMilli> as Timer0;
  uses interface CC2420Packet;

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
  
  bool SYNC_STATE = FALSE;
  bool READY_STATE = FALSE;
  bool WAIT_STATE = FALSE;
  bool INIT_STATE = FALSE;
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

	uint16_t getMeasureMean(uint16_t numMuestras, uint16_t tipoMedida){
	
		uint16_t media = 0;
	
		if(tipoMedida == 0){	// Humedad
			call Humidity.read();				
			media = H;
		}else if(tipoMedida == 1){	// Temperatura
			call Temperature.read();
			media = T;
		}else if(tipoMedida == 2){	// Luminosidad
			call ReadVisible.read();
			media = S;
		}
		
		return media;
		
	}

  // Función que obtiene el RSSI
  uint16_t getRssi(message_t *msg){
	return (uint16_t) call CC2420Packet.getRssi(msg);
  }

	event void Timer0.fired(){
		if(!busy){
		// Creamos un nuevo mensaje
		InfoMsg *respmsg = (InfoMsg *)(call Packet.getPayload(&pkt, sizeof(InfoMsg)));	
		
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
  
    call AMControl.start();	// Hace una llamada a la radio para encenderla
    
    // Paramos el temporizador
    call Timer0.stop();

    // Iniciamos los leds para saber que está en la fase inicial
    call Leds.led0On();
    call Leds.led1On();
    call Leds.led2On();

    // Al iniciar el nodo nos encontramos en el estado inicial
    INIT_STATE = TRUE;
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
							if(infomsg->DestinationNode == AM_BROADCAST_ADDR){
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
						WAIT_STATE = FALSE;
						READY_STATE = TRUE;
						
						if(call Timer0.isRunning()){
							call Timer0.stop();
							
							call Leds.led0On();
							call Leds.led1Off();
							call Leds.led2On();
						}
					}
				break;
				case(HELLO_TYPE):
					// No implementado
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
							
							WAIT_STATE = TRUE;
							READY_STATE = FALSE;
							tipoMedida = infomsg->datos[0];
							medida = getMeasureMean(infomsg->datos[0], infomsg->datos[1]);
							measureRssi = getRssi(msg);
							call Timer0.startPeriodic(intervalo_TDMA);
							
					}
				break;
				case(TRIGGER_TYPE):
					if(READY_STATE){
							//WAIT_STATE = TRUE;
							//READY_STATE = FALSE;
							
							call Timer0.startOneShot(intervalo_TDMA);						
					}
				break;
			
			}
			
		}
			/*	
	
		// Si hemos recibido un mensaje Handshake y además estamos en el 
		// estado de sincronización
		if(len == sizeof(Handshake) && SYNC_STATE == TRUE){
			// Obtenemos el mensaje Handshake
			Handshake *config = (Handshake *)payload;
				
			if(config->idNodoOrigen == nodoMaestro && config->idNodoDestino == TOS_NODE_ID){
				// Detenemos los mensajes Hello anteriores
				call Timer0.stop();
				// Asignamos el slot de la TDMA
				intervalo_TDMA = config->intervalo*config->periodo;
				call Timer0.startPeriodic(intervalo_TDMA);
				
				// Con esto hemos salido del estado de sincronización
				SYNC_STATE = FALSE;
				// Y entramos en el estado de "preparado"
				READY_STATE = TRUE;
			}
			
		}// Fin de Handshake
		
		// Si hemos recibido un Ack
		if(len == sizeof(Ack) && READY_STATE == TRUE){
			// Obtenemos el mensaje Ack
			Ack *asentimiento = (Ack *)payload;
			
			// Comprobamos que el origen y el destino sean los correctos
			if(asentimiento->idNodoOrigen == nodoMaestro && asentimiento->idNodoDestino == TOS_NODE_ID){
				// Detenemos los mensajes Ack
				call Timer0.stop();
				// Y salimos del estado de preparado
				READY_STATE = FALSE;
				WAITING_STATE = FALSE;
				
				call Leds.led0On();
				call Leds.led1Off();
				call Leds.led2On();
			}
		}// Fin de Ack
		
		if(len == sizeof(Request) && WAITING_STATE == FALSE){
			// Obtenemos el mensaje Request
			Request *peticion = (Request *)payload;
			// Comprobamos que el origen y el destino sean los correctos
			if(peticion->idNodoOrigen == nodoMaestro && peticion->idNodoDestino == TOS_NODE_ID){
				call Timer0.stop();
				medida = getMeasureMean(peticion->numMuestras, peticion->tipoMedida);
				measureRssi = getRssi(msg);
				// Entramos en el estado de espera
				WAITING_STATE = TRUE;
				// Comenzamos a transmitir el mensaje con las medidas
				call Timer0.startPeriodic(intervalo_TDMA);
			}
			
		}// Fin de Request*/
    
			/*if(len == sizeof(Ack) && WAITING_STATE == TRUE){
			Ack *asentimiento = (Ack *)payload;
			
			// Comprobamos que el origen y el destino sean los correctos
			if(asentimiento->idNodoOrigen == nodoMaestro && asentimiento->idNodoDestino == TOS_NODE_ID){
				// Detenemos los mensajes Ack
				call Timer0.stop();
				// Y salimos del estado de preparado
				WAITING_STATE = FALSE;
				
				call Leds.led0On();
				call Leds.led1Off();
				call Leds.led2On();
			}
		}// Fin de Ack 
    */
    return msg;
  }
}

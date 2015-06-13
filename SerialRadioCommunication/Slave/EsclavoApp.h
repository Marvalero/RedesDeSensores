// $Id: BlinkToRadio.h,v 1.4 2006/12/12 18:22:52 vlahan Exp $

#ifndef ESCLAVOAPP_H
#define ESCLAVOAPP_H

// Constantes definidas que se utilizarán luego

enum {
	
  AM_ESCLAVOAPP = 2, 

  AM_INFOMSG = 2,
  
  SYNC_TYPE = 1,							// Sincronización
  ACK_TYPE = 2,								// Asentimiento
  HELLO_TYPE = 3,							// Hola
  HANDSHAKE_TYPE = 4,						// Establecimiento de parámetros
  REQUEST_TYPE = 5,							// Petición
  RESPONSE_TYPE = 6,						// Respuesta
  TRIGGER_TYPE = 7,
  
  TIMER_PERIOD_SYNC = 1000      			// Periodo de Timer (por defecto 250)
};

/**
 * Mensaje que responde el esclavo al nodo maestro, de manera que contiene
 * el ID del nodo origen (Esclavo) y el ID del nodo destino (Maestro). Tras 
 * recibir este mensaje el esclavo permanece de forma latente y envía un mensaje 
 * cada segundo.
 */
typedef nx_struct InfoMsg {
	nx_uint16_t 	SourceNode;				// ID nodo origen
	nx_uint16_t 	DestinationNode;		// ID nodo destino
	nx_uint16_t		TOM;					// Tipo de mensaje
	nx_uint16_t		TTL;					// Tiempo de vida del mensaje (evita bucles)
	nx_uint16_t		datos[10];				// Array de datos
	
} InfoMsg;



#endif        

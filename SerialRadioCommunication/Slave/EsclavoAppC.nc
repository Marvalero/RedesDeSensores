#include "EsclavoApp.h"

configuration EsclavoAppC {
}

implementation {

	// Componente principal
  	components MainC;

	// Componente led
  	components LedsC;
	
	// Componente de aplicación
  	components EsclavoC as App;
        
	// Componente de mensaje activo              
  	components ActiveMessageC;
	
	// Componente para radio
  	components new AMSenderC(AM_ESCLAVOAPP);            
  	components new AMReceiverC(AM_ESCLAVOAPP);          
	
	// Componentes para los sensores
  	components CC2420ActiveMessageC;
  	components new SensirionSht11C() as Sht11;
  	components new HamamatsuS10871TsrC() as PhotoActiveC;
  	components new HamamatsuS1087ParC() as TotalSolarC;

	components new TimerMilliC() as Timer0; 
  
	// Conexiones
	App -> CC2420ActiveMessageC.CC2420Packet;

	App.Boot -> MainC;
  	App.Leds -> LedsC;
  	
  	App.Packet -> AMSenderC;
  	App.AMPacket -> AMSenderC;
  	App.AMControl -> ActiveMessageC;
  	App.AMSend -> AMSenderC;
  	App.Receive -> AMReceiverC;
  	App.ReadNotVisible -> PhotoActiveC;
  	App.ReadVisible -> TotalSolarC;
  	App.Temperature -> Sht11.Temperature;
	App.Humidity -> Sht11.Humidity;
	
	App.Timer0 -> Timer0;
}


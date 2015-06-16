#include "TestSerial.h"

configuration TestSerialAppC {}

implementation {

  components MainC, TestSerialC, LedsC;

  components SerialActiveMessageC as Serial;
  components ActiveMessageC as Radio;

	MainC.Boot <- TestSerialC;

	TestSerialC.RadioControl -> Radio;
	TestSerialC.SerialControl -> Serial;

	TestSerialC.UartSend -> Serial;
	TestSerialC.UartReceive -> Serial.Receive;
	TestSerialC.UartPacket -> Serial;
	TestSerialC.UartAMPacket -> Serial;
	
	TestSerialC.RadioSend -> Radio;
	TestSerialC.RadioReceive -> Radio.Receive;
	TestSerialC.RadioSnoop -> Radio.Snoop;
	TestSerialC.RadioPacket -> Radio;
	TestSerialC.RadioAMPacket -> Radio;

	TestSerialC.Leds -> LedsC;

}



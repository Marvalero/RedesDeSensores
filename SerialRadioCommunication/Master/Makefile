COMPONENT=TestSerialAppC
BUILD_EXTRA_DEPS += TestSerial.class
CLEAN_EXTRA = *.class TestSerialMsg.java

CFLAGS += -DCC2420_NO_ACKNOWLEDGEMENTS
CFLAGS += -DCC2420_NO_ADDRESS_RECOGNITION
CFLAGS += -DTASKLET_IS_TASK

CFLAGS += -I$(TOSDIR)/lib/T2Hack

TestSerial.class: $(wildcard *.java) InfoMsg.java
	javac -target 1.4 -source 1.4 *.java

#TestSerial.class: $(wildcard *.java) SLAMRequestMsg.java SLAMResponseMsg.java SLAMHelloMsg.java SLAMAckMsg.java\
 Mote.java SLAMHandshakeMsg.java SLAMSyncMsg.java
#	javac -target 1.4 -source 1.4 *.java

InfoMsg.java:
	mig java -target=null $(CFLAGS) -java-classname=InfoMsg Slave/EsclavoApp.h InfoMsg -o $@
	
#SLAMRequestMsg.java:
#	mig java -target=null $(CFLAGS) -java-classname=SLAMRequestMsg Slave/EsclavoApp.h Request -o $@
	
#SLAMHelloMsg.java:
#	mig java -target=null $(CFLAGS) -java-classname=SLAMHelloMsg Slave/EsclavoApp.h Hello -o $@
	
#SLAMAckMsg.java:
#	mig java -target=null $(CFLAGS) -java-classname=SLAMAckMsg Slave/EsclavoApp.h Ack -o $@

#SLAMHandshakeMsg.java:
#	mig java -target=null $(CFLAGS) -java-classname=SLAMHandshakeMsg Slave/EsclavoApp.h Handshake -o $@
	
#SLAMSyncMsg.java:
#	mig java -target=null $(CFLAGS) -java-classname=SLAMSyncMsg Slave/EsclavoApp.h Sync -o $@
		
include $(MAKERULES)


import java.io.IOException;
import java.io.PrintStream;

import java.util.*;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;
import net.tinyos.util.*;
/**
*	Clase principal que implementa el escuchador de mensajes recibidos
*/
public class TestSerial implements MessageListener{

	/**
	 * 	Trama de 5 segundos
	 */
	private int TRAMA_LENGTH = 5000;
	private int SUB_TRAMAS = 10;
	private int POLLING = TRAMA_LENGTH;
	private boolean POLLING_ENABLE = false;
	
	public static final int SYNC_TYPE = 1;
	public static final int ACK_TYPE = 2;							
	public static final int HELLO_TYPE = 3;							
	public static final int HANDSHAKE_TYPE = 4;						
	public static final int REQUEST_TYPE = 5;					
	public static final int RESPONSE_TYPE = 6;					
	public static final int TRIGGER_TYPE = 7;
	
	public static final double D1_TEMP = -40.1;
	public static final double D2_TEMP = 0.04;
	
	
	private LinkedList motas = null;
	private int interval = 1;
	
	/** 
	*	Objeto que nos proporciona una interfaz a nivel de aplicación java
	*	para enviar y recibir mensajes a un mote a través del puerto serie,
	*	conexión TCP u otro medio de conectividad. Es necesario registrar un
	*	objeto de la clase MessageListener.
	*/	
	private MoteIF moteIF;
  	
	
	/**
	*	Constructor principal
	*/
	public TestSerial(MoteIF moteIF) {
		
			motas = new LinkedList();
			
			// Asignamos el objeto correspondiente
    		this.moteIF = moteIF;
			// Registramos el nuevo listener como el de esta clase
			this.moteIF.registerListener(new InfoMsg(), this);

  	}
  	
  	public void imprimirLista(){
		
		System.out.print("Listado de motas : [");
		
		for(int i = 0; i < motas.size(); i++)
			System.out.print(((Mote)motas.get(i)).getID() + ",");
		
		System.out.println("]");
		System.out.println("Tenemos un total de " + motas.size() + " mota(s).");
			
	}

	public void imprimirMenu(){
		
		imprimirLista();
		
		System.out.println("Menu:");
		System.out.println("\t0. Sincronizar nodos");
		System.out.println("\t1. Ajustar trama TDMA");
		System.out.println("\t2. Ajustar polling");
		System.out.println("\t3. Tomar medida temperatura");
		System.out.println("\t4. Recopilar datos");
		System.out.println("\t5. Iniciar/Detener polling");
		System.out.println("\n\tq.Salir");
		System.out.print("\nOpción :  ");

	}

	/**
	*	Método que envía un paquete al mote que está conectado
	*/
	public void sendPackets() {
		// Con esta clase podemos leer de la entrada estándar
		
		PrintStream o = System.out;
		Scanner sc = new Scanner(System.in);   	 
		System.out.println("Registramos el escuchador");
	
		// Con esta variable obtendremos el valor introducido por teclado (numérico)
		short cmd = 0;

    		try {
			// En un bucle infinito
      			while (true) {
				
				// Mostramos el menú
				imprimirMenu();	
				// Obtenemos el valor introducido
				String opcion = sc.next();
				char c = opcion.charAt(0);
				
				switch(c){
					
					case '0':	// Sincronización de nodos
						
						InfoMsg syncMsg = new InfoMsg();
						
						syncMsg.set_SourceNode(0x7);
						syncMsg.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);
						syncMsg.set_TOM(0x01);
						
						moteIF.send(MoteIF.TOS_BCAST_ADDR, syncMsg);
						//moteIF.send(1, syncMsg);
						
						System.out.println("Se ha enviado un mensaje de sincronización a todos los nodos");
						
						break;

					case '1':	// Luminosidad
					
						System.out.println("Ajuste de trata TDMA\nPor favor introduce la longitud de la trama (segundos = " + TRAMA_LENGTH/1000 +"):");
						
						
						try{
						TRAMA_LENGTH = sc.nextInt()*1000;
					}catch(Exception e){
						TRAMA_LENGTH = 5000;
					}
						
						System.out.println("Insertar número de intervalos (nodos = " + SUB_TRAMAS + ") : ");
						try{
							
						SUB_TRAMAS = sc.nextInt();
					}catch(Exception e){
						SUB_TRAMAS = 10;
						}
		/*				System.out.println("Solicitando medida de luminosidad...");
					
						for(int i = 0; i < motas.size(); i++){
			
							InfoMsg tempRequest = new InfoMsg();
						
							Mote m = (Mote)motas.get(i);
							tempRequest.setElement_datos(0, 2);
							tempRequest.set_SourceNode(7);
							tempRequest.set_TOM(REQUEST_TYPE);
							tempRequest.set_DestinationNode(m.getID());
						
							System.out.println("Enviando peticion al nodo " + m.getID());
							
							moteIF.send(m.getID(), tempRequest);
						
						}*/
					break;
					case '2': 	// Humedad
					
					/*System.out.println("Solicitando medida de humedad...");
			
						for(int i = 0; i < motas.size(); i++){
			
						InfoMsg tempRequest = new InfoMsg();
						
							Mote m = (Mote)motas.get(i);
							tempRequest.setElement_datos(0, 0);
							tempRequest.set_SourceNode(7);
							tempRequest.set_TOM(REQUEST_TYPE);
							tempRequest.set_DestinationNode(m.getID());
						
							System.out.println("Enviando peticion al nodo " + m.getID());
							
							moteIF.send(m.getID(), tempRequest);
						
						}*/
						
						System.out.println("Por favor, introduce el tiempo de polling (segundos = " + TRAMA_LENGTH/1000+ ")");
						try{
						POLLING = sc.nextInt();
					}catch(Exception e){
						POLLING = TRAMA_LENGTH;
					}
					break;
					case '3': 	// Temperatura
						System.out.println("Solicitando medida de temperatura...");
			
						for(int i = 0; i < motas.size(); i++){
			
							InfoMsg tempRequest = new InfoMsg();
						
							Mote m = (Mote)motas.get(i);
							tempRequest.setElement_datos(0, 1);
							tempRequest.set_SourceNode(7);
							tempRequest.set_TOM(REQUEST_TYPE);
							tempRequest.set_DestinationNode(m.getID());
						
							System.out.println("Enviando peticion al nodo " + m.getID());
							
							moteIF.send(m.getID(), tempRequest);
						
						}
					break;
					case '4':
						System.out.println("Recopilando datos");
			
						//for(int i = 0; i < motas.size(); i++){
			
							InfoMsg getResultsManually = new InfoMsg();
						
							//Mote m = (Mote)motas.get(i);
							getResultsManually.setElement_datos(0, 1);
							getResultsManually.set_SourceNode(7);
							getResultsManually.set_TOM(TRIGGER_TYPE);
							getResultsManually.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);
						
							//System.out.println("Enviando peticion al nodo " + m.getID());
							
							moteIF.send(MoteIF.TOS_BCAST_ADDR, getResultsManually);
						
						//}
					break;
					case '5':
						if(POLLING_ENABLE == false){
							POLLING_ENABLE = true;
							Polling hilo = new Polling();
							hilo.start();
							System.out.println("Polling activado!");
						}else{
							POLLING_ENABLE = false;
							System.out.println("Polling desactivado!");
						}
					break;
					case 'q':
						System.exit(2);
					break;
					default:
						System.out.println("Opción no encontrada o no implementada aún");

				}


      			} // Fin bucle while
    		}
		catch (IOException exception) {
      			System.err.println(exception);
    		}// Fin try/catch
  }
  
   /**
	*	Método que se encargará de atender los paquetes recibidos
	*/
  	public void messageReceived(int to, Message message) {
		
		System.out.println("He recibido un mensaje nuevo.");
		SerialPacket sp = message.getSerialPacket();
		int SourceNode = sp.get_header_src();
		int DestinationNode = sp.get_header_dest();
	
		
		InfoMsg msgRcv = (InfoMsg)message;
		InfoMsg msgRsp = new InfoMsg();

		switch(msgRcv.get_TOM()){
			case HELLO_TYPE:
				System.out.println("Recibido mensaje Hello");
				
				Mote mota = new Mote(SourceNode);
	
				msgRsp.set_DestinationNode(SourceNode);
				msgRsp.set_SourceNode(0x7);
				msgRsp.set_TOM(HANDSHAKE_TYPE);
				
				if(motas.size()>0){
				
					for(int i = 0; i < motas.size(); i++){
						Mote m = (Mote)motas.get(i);
						System.out.println("ID Obtenido : " + SourceNode);
						System.out.println("ID Almacenado : " + m.getID());
						if(m.getID() == SourceNode && m.getStatus() == Mote.SYNC_STATUS){
								msgRsp.setElement_datos(0, m.getInterval());
								msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);
								try{
									moteIF.send(m.getID(), msgRsp);
								}catch(Exception e){
									System.out.println("Hubo un problema al transmitir el mensaje");
								}
								
								return;
						}else if(m.getID() == SourceNode && m.getStatus() != Mote.SYNC_STATUS){
								m.setStatus(Mote.SYNC_STATUS);
								msgRsp.setElement_datos(0, m.getInterval());
								msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);
								
								try{
									moteIF.send(m.getID(), msgRsp);
								}catch(Exception e){
									System.out.println("Hubo un problema al transmitir el mensaje");
								}
								
								return;
						}		
				
					}
				
				mota.setInterval(interval++);
				mota.setStatus(Mote.SYNC_STATUS);
				motas.add(mota);
				
				msgRsp.setElement_datos(0, mota.getInterval());
				msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);
				
				try{
					moteIF.send(mota.getID(), msgRsp);
				}catch(Exception e){
					System.out.println("Hubo un problema al transmitir el mensaje");
				}
				
				
			}else{
				mota.setInterval(interval++);
				mota.setStatus(Mote.SYNC_STATUS);
				motas.add(mota);
			}	

			break;
			case ACK_TYPE:
				System.out.println("Recibido mensaje Ack");
				
				for(int i = 0; i < motas.size(); i++){
						Mote m = (Mote)motas.get(i);
						
						if(m.getID() == SourceNode){
							m.setStatus(Mote.WAIT_STATUS);
							msgRsp.set_DestinationNode(m.getID());
							msgRsp.set_SourceNode(0x7);
							msgRsp.set_TOM(ACK_TYPE);
				
							try{
								moteIF.send(m.getID(), msgRsp);
								System.out.println("Enviado Ack a " + m.getID());
							}catch(Exception e){
								System.out.println("Hubo un problema al transmitir el mensaje");
							}
						}
				}
				
			break;
			case SYNC_TYPE:
				System.out.println("Recibido mensaje Sync");
			break;
			case HANDSHAKE_TYPE:
				System.out.println("Recibido mensaje Handshake");
			break;
			case REQUEST_TYPE:
				System.out.println("Recibido mensaje Request");
			break;
			case RESPONSE_TYPE:
				System.out.println("Recibido mensaje Response");
				
				System.out.println("Tipo de mensaje : " + msgRcv.get_TOM());
				System.out.println("Tipo medida : " + msgRcv.get_datos()[0]);
				System.out.println("medida : " + msgRcv.get_datos()[1]);
				System.out.println("Temperatura : " + (D1_TEMP + msgRcv.get_datos()[1]*D2_TEMP));
				System.out.println("distancia : " + msgRcv.get_datos()[2]);
			break;
		}
		
		if(DestinationNode == 0x7 && msgRcv.get_TOM() == HELLO_TYPE){
//				System.out.println("Se ha recibido un mensaje del tipo \"Hello\" con ID Origen " + message.getSeialPacket().get_header_src());
			
			
		}
		
	}
  
	/**
	*	Método para mostrar por pantalla el uso del programa
	*/
  	private static void usage() {
    		System.err.println("usage: TestSerial [-comm <source>]");
  	}
  	
	/**
	*	Método principal
	*/
  	public static void main(String[] args) throws Exception {
    		// En un principio la fuente es desconocida
		String source = null;
		// Comprobamos el número de argumentos
    		if (args.length == 2) {
			// Si el primer argumento no es "-comm"
      			if (!args[0].equals("-comm")) {
				usage();	// Mostramos el uso del programa
				System.exit(1);	// Salimos del programa
      			}
      			// Si es correcto el primer argumento asignamos la fuente
			source = args[1];	
    		}
		// Si el número de argumentos es incorrecto
    		else if (args.length != 0) {
			// Mostramos el uso del programa
      			usage();
			// Salimos del programa
      			System.exit(1);
	    	} // Fin condiciones de argumentos
    
   	 	PhoenixSource phoenix;
    		// Si la fuente es desconocida
    		if (source == null) {
			// Aplicamos la fuente por defecto
      			phoenix = BuildSource.makePhoenix(PrintStreamMessenger.err);
    		}
    		else {
			// Aplicamos la fuente pasada como argumento
      			phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
    		}
		
		// Creamos la interfaz de comunicación con nuestra fuente asignada
    		MoteIF mif = new MoteIF(phoenix);
		// Creamos una nueva comunicación en Serie
    		TestSerial serial = new TestSerial(mif);
		// Enviamos los paquetes
    		serial.sendPackets();
  	}

	class Polling extends Thread{
		
		public void run(){
			while(POLLING_ENABLE){
						
						try{

							
					
							InfoMsg tempRequest = new InfoMsg();
						
							tempRequest.setElement_datos(0, 1);
							tempRequest.set_SourceNode(7);
							tempRequest.set_TOM(REQUEST_TYPE);
							tempRequest.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);
						
							moteIF.send(MoteIF.TOS_BCAST_ADDR, tempRequest);

							Thread.sleep(1000);
						
							InfoMsg getResults = new InfoMsg();
						
							getResults.setElement_datos(0, 1);
							getResults.set_SourceNode(7);
							getResults.set_TOM(TRIGGER_TYPE);
							getResults.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);
						
							moteIF.send(MoteIF.TOS_BCAST_ADDR, getResults);
							
							Thread.sleep(POLLING);
							
						}catch(Exception e){
							e.printStackTrace();
							System.err.println("Fallo en Polling");
						}
							
			}
			
		}
		
		
	}


}

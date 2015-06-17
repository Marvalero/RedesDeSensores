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
	public static final int REJECT_TYPE = 8;

	public static final double D1_TEMP = -39.6;
	public static final double D2_TEMP = 0.01;


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
							// Creamos un mensaje de sincronización
						InfoMsg syncMsg = new InfoMsg();

						// Establecemos que el nodo origen es el maestro
						syncMsg.set_SourceNode(0x7);
						// Establecemos que le nodo destino es todo el mundo
						syncMsg.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);
						// El tipo de mensaje es de sincronización
						syncMsg.set_TOM(SYNC_TYPE);

						// Enviamos el mensaje
						moteIF.send(MoteIF.TOS_BCAST_ADDR, syncMsg);

						System.out.println("Se ha enviado un mensaje de sincronización a todos los nodos");

						break;

					case '1':	// Ajuste TDMA

						System.out.println("Ajuste de trata TDMA\nPor favor introduce la longitud de la trama (segundos = " + TRAMA_LENGTH/1000 +"):");

						try{
							// Número de segundos asignados
							TRAMA_LENGTH = sc.nextInt()*1000;
						}catch(Exception e){
							System.err.println("Se ha producido un error asignación por defecto");
							// En caso de error dejamos la trama por defecto
							TRAMA_LENGTH = 5000;
						}

						System.out.println("Insertar número de intervalos (slots = " + SUB_TRAMAS + ") : ");

						try{
							// Número de slots asignados
							SUB_TRAMAS = sc.nextInt();
						}catch(Exception e){
							System.err.println("Se ha producido un error asignación por defecto");
							// En caso de error dejamos los slots por defecto
							SUB_TRAMAS = 10;
						}

						System.out.println("Resumen:\n"
										+ "\tTiempo TDMA : " + TRAMA_LENGTH + " ms"
										+ "\n\tSlots TDMA : " + SUB_TRAMAS
										+ "\n\tIntervalo TDMA : " + TRAMA_LENGTH/SUB_TRAMAS +" ms ");

					break;
					case '2': 	// Polling

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

							InfoMsg getResultsManually = new InfoMsg();

							getResultsManually.setElement_datos(0, 1);
							getResultsManually.set_SourceNode(7);
							getResultsManually.set_TOM(TRIGGER_TYPE);
							getResultsManually.set_DestinationNode(MoteIF.TOS_BCAST_ADDR);

							moteIF.send(MoteIF.TOS_BCAST_ADDR, getResultsManually);

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
		// Obtenemos el nodo origen
		int SourceNode = sp.get_header_src();
		// Obtenemos el nodo destino
		int DestinationNode = sp.get_header_dest();

		// Obtenemos el mensaje recibido
		InfoMsg msgRcv = (InfoMsg)message;
		// Creamos un mensaje de respuesta
		InfoMsg msgRsp = new InfoMsg();

		// Comprobamos el tipo de mensaje ({T}ype {O}f {M}essage)
		switch(msgRcv.get_TOM()){
			// En el caso de un mensaje Hello
			case HELLO_TYPE:

				System.out.println("Recibido mensaje Hello");
				if(DestinationNode == 0){

					// Creamos un mensaje de sincronización
					InfoMsg syncMsg = new InfoMsg();

					// Establecemos que el nodo origen es el maestro
					syncMsg.set_SourceNode(0x7);
					// Establecemos que le nodo destino es todo el mundo
					syncMsg.set_DestinationNode(SourceNode);
					// El tipo de mensaje es de sincronización
					syncMsg.set_TOM(SYNC_TYPE);

					try{
						// Enviamos el mensaje
						moteIF.send(MoteIF.TOS_BCAST_ADDR, syncMsg);
					}catch(Exception e){
						System.err.println("Error al enviar el mensaje de sincronización");
					}

					System.out.println("Se ha enviado un mensaje de sincronización al nodo : " + SourceNode);

				}else if(interval <= SUB_TRAMAS){
					// Creamos una nueva mota con ID del nodo origen
					Mote mota = new Mote(SourceNode);

					// Establecemos el destino que será el del nodo origen
					msgRsp.set_DestinationNode(mota.getID());
					// El origen será el maestro
					msgRsp.set_SourceNode(0x7);
					// El tipo de mensaje que se enviará es del tipo Handshake
					msgRsp.set_TOM(HANDSHAKE_TYPE);

					// Comprobamos si tenemos un número de motas disponibles en caso de que sea cierto
					if(motas.size()>0){
						// Recorremos el vector de motas
						for(int i = 0; i < motas.size(); i++){
							// Obtenemos la mota
							Mote m = (Mote)motas.get(i);

							//System.out.println("ID Obtenido : " + SourceNode);

							//System.out.println("ID Almacenado : " + m.getID());

							// Si obtenido el ID del nodo coincide con el origen obtenido en el mensaje y el estado es sincronizando
							if(m.getID() == SourceNode && m.getStatus() == Mote.SYNC_STATUS){
									// En el mensaje de respuesta el primer dato es el intervalo de la TDMA
									msgRsp.setElement_datos(0, m.getInterval());
									// En el mensaje de respuesta el segundo dato es el tiempo del slot de trama TDMA
									msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);

									// Intentamos transmitir el mensaje
									try{
										moteIF.send(m.getID(), msgRsp);
									}catch(Exception e){
										// En caso de error lo notificamos al usuario
										System.err.println("Hubo un problema al transmitir el mensaje");
									}
									return;
							// Si obtenido el ID del nodo coincide con el origen obtenido en el mensaje y el estado es distinto de sincronizando
							}else if(m.getID() == SourceNode && m.getStatus() != Mote.SYNC_STATUS){
									// El estado del nodo lo cambiamos a sincronizando
									m.setStatus(Mote.SYNC_STATUS);

									// En el mensaje de respuesta el primer dato es el intervalo de la TDMA
									msgRsp.setElement_datos(0, m.getInterval());
									// En el mensaje de respuesta el segundo dato es el tiempo del slot de trama TDMA
									msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);

									// Intentamos transmitir el mensaje
									try{
										moteIF.send(m.getID(), msgRsp);
									}catch(Exception e){
										System.err.println("Hubo un problema al transmitir el mensaje");
									}

									return;
							}

						}// Fin de bucle for
					// En caso de que no exista en el vector le agregamos un nuevo intervalo
					mota.setInterval(interval++);
					// Le asignamos el estado de sincronizando
					mota.setStatus(Mote.SYNC_STATUS);
					// Y la añadimos al vector de motas
					motas.add(mota);

					// En el mensaje de respuesta damos el intervalo para dicha mota
					msgRsp.setElement_datos(0, mota.getInterval());
					// Y le asignamos el slot de trama TDMA
					msgRsp.setElement_datos(1, TRAMA_LENGTH/SUB_TRAMAS);

					// Intentamos enviar el mensaje
					try{
						moteIF.send(mota.getID(), msgRsp);
					}catch(Exception e){	// En caso de error
						System.err.println("Hubo un problema al transmitir el mensaje");
					}

				// Si no tenemos motas dentro del vector es evidente que debemos agregarla
				}else{
						// Asignamos un nuevo intervalo
						mota.setInterval(interval++);
						// Establecemos el estado a sincronizando
						mota.setStatus(Mote.SYNC_STATUS);
						// Añadimos la mota al vector
						motas.add(mota);
						System.out.println("Se ha añadido la mota con ID : " + mota.getID());
				}// Fin comprobación número de motas en el vector
			}else{
				// En caso de que tengamos todos los slots ocupados enviamos un mensaje de rechazo
				msgRsp.set_TOM(REJECT_TYPE);

				// Intentamos enviar el mensaje
				try{
					moteIF.send(SourceNode, msgRsp);
				}catch(Exception e){
					System.err.println("Hubo un problema al enviar el mensaje");
				}
			}// Fin de comprobación de motas en SUBT_RAMAS
			break;
			case ACK_TYPE:	// Mensaje del tipo ACK

				System.out.println("Recibido mensaje Ack");

				// Recorremos el listado de nodos que tenenmos disponibles
				for(int i = 0; i < motas.size(); i++){
						// Obtenemos el nodo
						Mote m = (Mote)motas.get(i);

						// Si el ID del nodo coincide con el recibido por el mensaje
						if(m.getID() == SourceNode){
							// Si el estado es de sincronización
							if(m.getStatus() == Mote.SYNC_STATUS){
								// Pasamos a un estado de espera
								m.setStatus(Mote.WAIT_STATUS);
								// Si por el contrario es un estado de espera
							}else if(m.getStatus() == Mote.WAIT_STATUS){
								// Pasamos a un estado de preparado
								m.setStatus(Mote.READY_STATUS);
							}
							// Establecemos el nodo destino como el ID de la mota obtenida
							msgRsp.set_DestinationNode(m.getID());
							// Establecemos el nodo origen como el asignado por el maestro
							msgRsp.set_SourceNode(0x7);
							// El tipo de mensaje es ACK
							msgRsp.set_TOM(ACK_TYPE);

							// Intentamos enviar el ACK
							try{
								moteIF.send(m.getID(), msgRsp);
								System.out.println("Enviado Ack a " + m.getID());
							}catch(Exception e){
								System.err.println("Hubo un problema al transmitir el mensaje");
							}
						}
				}

			break;
			case RESPONSE_TYPE:	// Mensaje Response
				System.out.println("Recibido mensaje Response");
				System.out.println("Nodo : " + SourceNode);
				// Tipo de mensaje
				System.out.println("Tipo de mensaje : " + msgRcv.get_TOM());

				// Tipo de medida solicitada
				switch(msgRcv.get_datos()[0]){
					case 1:
						System.out.println("Tipo medida : Temperatura");
					break;
				}

				// Medida
				System.out.println("medida : " + msgRcv.get_datos()[1]);
				// Temperatura
				System.out.println("Temperatura : " + (D1_TEMP + msgRcv.get_datos()[1]*D2_TEMP));
				// Distancia
				System.out.println("distancia : " + msgRcv.get_datos()[2]);

			break;
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

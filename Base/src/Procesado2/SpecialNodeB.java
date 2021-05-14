package Procesado2;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import Global.GlobalFunctions;
import protocol.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FilterApp.Filter;
import FilterApp.FilterHelper;

public class SpecialNodeB {
	
	public static void main(String[] args) {
		try {
			ServerSocket listen = new ServerSocket(GlobalFunctions.getPort("SPECIALNODEB"));
			while(true) {
				System.out.println("Special Node Filter B waiting...");
				Socket socket = listen.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
				new ConnectionSpecialB(socket,args);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}


class ConnectionSpecialB extends Thread{
	Socket socketServer, socketRingLeft,socketRingRight;
	ObjectOutputStream osServer,osRingLeft,osRingRight;
	ObjectInputStream isServer, isRingLeft, isRingRight;
	String [] args;
	static Filter filterImpl;
	public boolean done = false;
	public ServerSocket listen;
	
	public ConnectionSpecialB(Socket socket, String[] args) {
		try {
			this.socketServer = socket;
			this.update("Connection accepted from ... "+socket.getInetAddress());
			this.isServer = new ObjectInputStream(this.socketServer.getInputStream());
			this.osServer = new ObjectOutputStream(this.socketServer.getOutputStream());
			this.args = args;
			this.listen = new ServerSocket(GlobalFunctions.getPort("SPECIALNODEB1"));
			this.start();
		} catch (Exception e) {
			update(" -Error: "+e.getMessage());
		}
	}
	
	@Override
	public void run() {
			try {
				Request r = (Request)  this.isServer.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					ControlRequest cr = (ControlRequest) r;
					if(cr.getSubtype().equals("OP_FILTER")) {
						
						if(Integer.valueOf(cr.getArgs().get(0).toString())==0) {
							System.out.println("Soy el elegido para filtrar");
							ORB orb = ORB.init(this.args, null);

					        // get the root naming context
					        org.omg.CORBA.Object objRef = 
					            orb.resolve_initial_references("NameService");
					        // Use NamingContextExt instead of NamingContext. This is 
					        // part of the Interoperable naming Service.  
					        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					        
					        
					        
					        // resolve the Object Reference in Naming
					        String name = "Filter";
					        filterImpl = FilterHelper.narrow(ncRef.resolve_str(name));
					        System.out.println("Obtained a handle on server object: " + filterImpl);
					        update("Image filtered and sent to the multiserver");
							GlobalFunctions.updateToken("FILTERB.txt", 0);
							ControlResponse crs = new ControlResponse("OP_FILTER_OK");
							crs.getArgs().add("All right from SpecialNode and path is"+filterImpl.getFilterImage(cr.getArgs().get(1).toString(), 2));
							this.osServer.writeObject(crs);
						}else {
							System.out.println("No soy el elegido para filtrar");
							System.out.println(cr.getSubtype());
							this.doConnect();
							this.osRingRight.writeObject(cr);
							Thread m = new Thread(new nodeInactiveB(this));
							m.start();
							System.out.println("Esperando respuesta del resto de nodos del anillo");
							this.socketRingLeft = this.listen.accept();
							this.done = true;
							System.out.println("Respuesta recibida");
							this.osRingLeft = new ObjectOutputStream(this.socketRingLeft.getOutputStream());
							this.isRingLeft = new ObjectInputStream(this.socketRingLeft.getInputStream());
							ControlResponse crs = (ControlResponse) this.isRingLeft.readObject();
							update("Image filtered and sent to the multiserver");							
							this.osServer.writeObject(crs);
							System.out.println("Respuesta enviada");
							this.doDisconnect();
							listen.close();
							listen = null;
						}

					}
				}
				
			} catch (Exception e) {
				update(" -Error: "+e.getMessage());
			}
	
	}
	public void update(String text) {
		 SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		 Date date = new Date(System.currentTimeMillis());
		 GlobalFunctions.updateLog("Processed2.txt",(formatter.format(date)+" "+text));
	 }
	public void doConnect() {
		try {
			if(this.socketRingRight == null) {
				this.socketRingRight = new Socket(GlobalFunctions.getIP("NODEB1"),GlobalFunctions.getPort("NODEB1"));
				this.osRingRight = new ObjectOutputStream(this.socketRingRight.getOutputStream());
				this.isRingRight = new ObjectInputStream(this.socketRingRight.getInputStream());
			}
		} catch (Exception e) {
			update(" -Error: "+e.getMessage());
		}
	}
	
	public void doDisconnect() {
		try {
			if(this.socketRingRight != null) {
				this.osRingRight.close();
				this.osRingRight = null;
				this.isRingRight.close();
				this.isRingRight = null;
				this.socketRingRight.close();
				this.socketRingRight = null;
			}
			if(this.socketRingLeft != null) {
				this.osRingLeft.close();
				this.osRingLeft = null;
				this.isRingLeft.close();
				this.isRingLeft = null;
				this.socketRingLeft.close();
				this.socketRingLeft = null;
			}
		} catch (Exception e) {
			update(" -Error: "+e.getMessage());
		}
	}
}
class nodeInactiveB extends Thread{
	ConnectionSpecialB sp;
	
	public nodeInactiveB(ConnectionSpecialB sp) {
		this.sp = sp;
	}
	@Override
	public void run() {
		try {
			System.out.println("A dormir");
			Thread.sleep(1000);
			System.out.println("A despertar");
			if(!this.sp.done) {
				this.sp.listen.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
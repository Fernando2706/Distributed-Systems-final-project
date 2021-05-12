package Procesado1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Global.GlobalFunctions;
import protocol.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FilterApp.Filter;
import FilterApp.FilterHelper;

public class SpecialNodeA {
	
	public static void main(String[] args) {
		try {
			ServerSocket listen = new ServerSocket(GlobalFunctions.getPort("SPECIALNODEA"));
			while(true) {
				System.out.println("Special Node Filter A waiting..."+listen.getInetAddress()+":"+listen.getLocalPort());
				Socket socket = listen.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
				new ConnectionSpecial(socket,args);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}


class ConnectionSpecial extends Thread{
	Socket socketServer, socketRingLeft,socketRingRight;
	ObjectOutputStream osServer,osRingLeft,osRingRight;
	ObjectInputStream isServer, isRingLeft, isRingRight;
	String [] args;
	static Filter filterImpl;
	
	public ConnectionSpecial(Socket socket, String[] args) {
		try {
			System.out.println("Hi");
			this.socketServer = socket;
			this.isServer = new ObjectInputStream(this.socketServer.getInputStream());
			this.osServer = new ObjectOutputStream(this.socketServer.getOutputStream());
			this.args = args;
			System.out.println(this.args.length);
			this.start();
		} catch (Exception e) {
				e.printStackTrace();		
		}
	}
	
	@Override
	public void run() {
			try {
				System.out.println("Hi");
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
					        
							GlobalFunctions.updateToken("FILTERA.txt", 0);
							ControlResponse crs = new ControlResponse("OP_FILTER_OK");
							crs.getArgs().add("All right from SpecialNode and path is"+filterImpl.getFilterImage(cr.getArgs().get(1).toString(), 1));
							this.osServer.writeObject(crs);
						}else {
							System.out.println("No soy el elegido para filtrar");
							System.out.println(cr.getSubtype());
							this.doConnect();
							this.osRingRight.writeObject(cr);
							ServerSocket listen = new ServerSocket(GlobalFunctions.getPort("SPECIALNODEA1"));
							System.out.println("Esperando respuesta del resto de nodos del anillo");
							this.socketRingLeft = listen.accept();
							System.out.println("Respuesta recibida");
							this.osRingLeft = new ObjectOutputStream(this.socketRingLeft.getOutputStream());
							this.isRingLeft = new ObjectInputStream(this.socketRingLeft.getInputStream());
							ControlResponse crs = (ControlResponse) this.isRingLeft.readObject();
							this.osServer.writeObject(crs);
							this.doDisconnect();
							listen.close();
							listen = null;
						}

					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();		
			}
	
	}
	public void doConnect() {
		try {
			if(this.socketRingRight == null) {
				this.socketRingRight = new Socket(GlobalFunctions.getIP("NODEA1"),GlobalFunctions.getPort("NODEA1"));
				this.osRingRight = new ObjectOutputStream(this.socketRingRight.getOutputStream());
				this.isRingRight = new ObjectInputStream(this.socketRingRight.getInputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();		
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
			e.printStackTrace();		
		}
	}
}

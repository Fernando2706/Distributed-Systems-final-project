package Procesado1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

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
            Semaphore sem = new Semaphore(1);
			while(true) {
				System.out.println("Special Node Filter A waiting..."+listen.getInetAddress()+":"+listen.getLocalPort());
				Socket socket = listen.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
				new ConnectionSpecial(socket,args,sem);
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
	ServerSocket listen;
	String [] args;
	static Filter filterImpl;
	public boolean done = false;
	public Semaphore sem;
	
	public ConnectionSpecial(Socket socket, String[] args,Semaphore sem) {
		try {
			this.socketServer = socket;
			this.sem = sem;
			this.update("Connection accepted from ... "+socketServer.getInetAddress());
			this.isServer = new ObjectInputStream(this.socketServer.getInputStream());
			this.osServer = new ObjectOutputStream(this.socketServer.getOutputStream());
			this.listen =  new ServerSocket(GlobalFunctions.getPort("SPECIALNODEA1"));

			this.args = args;
			System.out.println(this.args.length);
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
							GlobalFunctions.updateToken("FILTERA.txt", 0);
							ControlResponse crs = new ControlResponse("OP_FILTER_OK");
							crs.getArgs().add("All right from SpecialNode and path is"+filterImpl.getFilterImage(cr.getArgs().get(1).toString(), 1));
							this.osServer.writeObject(crs);
						}else {
							System.out.println("No soy el elegido para filtrar");
							System.out.println(cr.getSubtype());
							this.doConnect();
							this.osRingRight.writeObject(cr);
							Thread m = new Thread(new nodeInactive(this));
							m.start();
							this.socketRingLeft = listen.accept();
							this.done = true;
							this.osRingLeft = new ObjectOutputStream(this.socketRingLeft.getOutputStream());
							this.isRingLeft = new ObjectInputStream(this.socketRingLeft.getInputStream());
							ControlResponse crs = (ControlResponse) this.isRingLeft.readObject();
							this.osServer.writeObject(crs);
							update("Image filtered and sent to the multiserver");							
							this.doDisconnect();
							this.listen.close();
							listen.close();
							listen = null;
						}

					}
				}
				
			} catch (Exception e) {
				update(" -Error: "+e.getMessage());
				ControlResponse cr = new ControlResponse("OP_FILTER_NOK");
				cr.getArgs().add("Failed... try it later");
				try {
					this.osServer.writeObject(cr);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				this.doDisconnect();
			}
	
	}
	public void update(String text) {
		try {
			this.sem.acquire();
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			GlobalFunctions.updateLog("Processed1.txt",(formatter.format(date)+" "+text));
			this.sem.release();
		} catch (Exception e) {
			// TODO: handle exception
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
class nodeInactive extends Thread{
	ConnectionSpecial sp;
	
	public nodeInactive(ConnectionSpecial sp) {
		this.sp = sp;
	}
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			if(!this.sp.done) {
				this.sp.listen.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}

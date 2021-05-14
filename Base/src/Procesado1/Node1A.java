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

public class Node1A {

	
	public static void main(String[] args) {
		try {
			ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("NODEA1"));
			
			while(true) {
				System.out.println("Waiting node 1...");
				Socket s = listenSocket.accept();
				new ConnectionNodeA(1, GlobalFunctions.getExternalVariables("MAXNODES"), s,args);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}

class ConnectionNodeA extends Thread{
	private Socket socketLeft,socketRight;
	private ObjectInputStream isLeft,isRight;
	private ObjectOutputStream osLeft,osRight;
	private int maxNodes,index;
	String [] args;
	static Filter filterImpl;
	
	public ConnectionNodeA(int index, int maxNodes, Socket socket,String[] args) {
		try {
			this.maxNodes = maxNodes;
			this.index = index;
			this.socketLeft = socket;
			this.isLeft = new ObjectInputStream(this.socketLeft.getInputStream());
			this.osLeft = new ObjectOutputStream(this.socketLeft.getOutputStream());
			this.args = args;
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	@Override
	public void run() {
		try {
			if(this.index == this.maxNodes) {
				Request r = (Request) this.isLeft.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					
					ControlRequest cr = (ControlRequest) r;
					if(cr.getSubtype().equals("OP_FILTER")) {
						if(Integer.valueOf(cr.getArgs().get(0).toString())==this.index) {
							System.out.println("Soy la elegida para filtrar");
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
					        
							GlobalFunctions.updateToken("FILTERA.txt", this.index);
							this.doConnect();
							ControlResponse crs = new ControlResponse("OP_FILTER_OK");
							crs.getArgs().add("Filter complete from node"+this.index+"and path is"+filterImpl.getFilterImage(cr.getArgs().get(1).toString(), 1));
							this.osRight.writeObject(crs);
							System.out.println("Done");
							this.doDisconnect();
							
						}else {
							this.doConnect();
							this.osRight.writeObject(cr);
							this.doDisconnect();
						}
						
					}else if(cr.getSubtype().equals("OP_FILTER_OK")) {
						this.doConnect();
						ControlResponse crs = new ControlResponse("OP_FILTER_OK");
						crs.getArgs().add(cr.getArgs().get(0));
						this.osRight.writeObject(crs);
						this.doDisconnect();
					}
				}
			}else {
				Request r = (Request) this.isLeft.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					
					ControlRequest cr = (ControlRequest) r;
					if(cr.getSubtype().equals("OP_FILTER")) {
						if(Integer.valueOf(cr.getArgs().get(0).toString())==this.index) {
							System.out.println("Soy la elegida para filtrar");
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
					        
							GlobalFunctions.updateToken("FILTERA.txt", this.index);
							this.doConnect();
							ControlRequest crs = new ControlRequest("OP_FILTER_OK");
							crs.getArgs().add("Filter complete from node"+this.index+"and path is"+filterImpl.getFilterImage(cr.getArgs().get(1).toString(), 1));
							this.osRight.writeObject(crs);
							this.doDisconnect();
						}else {
							this.doConnect();
							this.osRight.writeObject(cr);
							this.doDisconnect();
						}
						
					}else if(cr.getSubtype().equals("OP_FILTER_OK")) {
						this.doConnect();
						this.osRight.writeObject(cr);
						this.doDisconnect();
					}
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void doConnect() {
		try {
			if(this.socketRight == null) {
				if(this.index == this.maxNodes) {
					this.socketRight = new Socket(GlobalFunctions.getIP("SPECIALNODEA"),GlobalFunctions.getPort("SPECIALNODEA1"));
					this.osRight = new ObjectOutputStream(this.socketRight.getOutputStream());
					this.isRight = new ObjectInputStream(this.socketRight.getInputStream());
				}else {
					this.socketRight = new Socket(GlobalFunctions.getIP("NODEA"+(this.index+1)),GlobalFunctions.getPort("NODEA"+(this.index+1)));
					this.osRight = new ObjectOutputStream(this.socketRight.getOutputStream());
					this.isRight = new ObjectInputStream(this.socketRight.getInputStream());
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());	

		}
	}
	public void doDisconnect() {
		try {
			if(this.socketRight != null) {
				this.osRight.close();
				this.osRight = null;
				this.isRight.close();
				this.isRight = null;
				this.socketRight.close();
				this.socketRight = null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());	

		}
	}
	
}
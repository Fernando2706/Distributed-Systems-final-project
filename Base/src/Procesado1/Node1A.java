package Procesado1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Global.GlobalFunctions;
import protocol.*;

public class Node1A {

	
	public static void main(String[] args) {
		try {
			ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("NODEA1"));
			
			while(true) {
				System.out.println("Waiting node 1...");
				Socket s = listenSocket.accept();
				new ConnectionNodeA(1, GlobalFunctions.getExternalVariables("MAXNODES"), s);
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
	
	public ConnectionNodeA(int index, int maxNodes, Socket socket) {
		try {
			this.maxNodes = maxNodes;
			this.index = index;
			this.socketLeft = socket;
			this.isLeft = new ObjectInputStream(this.socketLeft.getInputStream());
			this.osLeft = new ObjectOutputStream(this.socketLeft.getOutputStream());
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
						this.doConnect();
						ControlResponse crs = new ControlResponse("OP_FILTER_OK");
						crs.getArgs().add("All Right from Node"+this.index);
						this.osRight.writeObject(crs);
						
					}
				}
			}else {
				Request r = (Request) this.isLeft.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					
					ControlRequest cr = (ControlRequest) r;
					if(cr.getSubtype().equals("OP_FILTER")) {
						this.doConnect();
						this.osRight.writeObject(cr);
						
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
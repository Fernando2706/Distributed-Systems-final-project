package MultiServer;

import protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import Global.GlobalFunctions;

public class MultiServer1 {

	
		public static void main(String[] args) {
			try {
				ServerSocket listen = new ServerSocket(GlobalFunctions.getPort("SERVER1"));
				while(true) {
					System.out.println("Waiting server 1...");
					Socket socket = listen.accept();
					new ConnectionServer1(socket);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}

class ConnectionServer1 extends Thread{
	Socket socketCentral, socketRing;
	ObjectOutputStream osCentral, osRing;
	ObjectInputStream isCentral,isRing;
	
	 public ConnectionServer1(Socket socketCentral) {
		 try {
			this.socketCentral = socketCentral;
			this.osCentral = new ObjectOutputStream(this.socketCentral.getOutputStream());
			this.isCentral = new ObjectInputStream(this.socketCentral.getInputStream());
			this.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	 
	 @Override
	public void run() {
		try {
			Request r = (Request) this.isCentral.readObject();
			System.out.println("Tipo de peticion: "+r.getType());
			if(r.getType().equals("CONTROL_REQUEST")) {
				ControlRequest cr = (ControlRequest) r;
				if(cr.getSubtype().equals("OP_FILTER")) {
					this.doFilter(cr.getArgs().get(0).toString(), cr.getArgs().get(1).toString());
				}
			}else if(r.getType().equals("DATA_REQUEST")) {
				DataRequest dr = (DataRequest) r;
				if(dr.getSubtype().equals("OP_CPU")) {
					ControlResponse crsCPU = new ControlResponse("OP_CPU_OK");
                    Random random = new Random();
                    int rmd = random.nextInt(101);
                    System.out.println(rmd);
                    crsCPU.getArgs().add(rmd);
                    this.osCentral.writeObject(crsCPU);
                    this.doDisconnect();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	 
	 public void doFilter(String path, String filter) {
		 try {
			 if(filter.equals("FILTERA")) {
				 this.doConnect("SPECIALNODEA");
				 ControlRequest cr = new ControlRequest("OP_FILTER");
				 cr.getArgs().add(GlobalFunctions.getLessToken("FILTERA.txt"));
				 cr.getArgs().add(path);
				 cr.getArgs().add(filter);
				 this.osRing.writeObject(cr);
			 }
			 ControlResponse crs = (ControlResponse) this.isRing.readObject();
			 this.doDisconnect();

			this.osCentral.writeObject(crs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
	 
	 public void doConnect(String filter) {
		 try {
			if(this.socketRing == null) {
				this.socketRing = new Socket(GlobalFunctions.getIP(filter),GlobalFunctions.getPort(filter));
				this.osRing = new ObjectOutputStream(this.socketRing.getOutputStream());
				this.isRing = new ObjectInputStream(this.socketRing.getInputStream());
			}
		} catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("doConnect Server1 :"+e.getMessage());
		}
	 }
	 public void doDisconnect() {
		 try {
			if(this.socketRing != null) {
				this.osRing.close();
				this.osRing = null;
				this.isRing.close();
				this.isRing = null;
				this.socketRing.close();
				this.socketRing = null;
			}
		} catch (Exception e) {
			System.out.println("doDisconnect Server1 :"+e.getMessage());
		}
	 }
}

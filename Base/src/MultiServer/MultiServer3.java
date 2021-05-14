package MultiServer;

import protocol.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;

import Global.GlobalFunctions;

public class MultiServer3 {

	
		public static void main(String[] args) {
			try {
				ServerSocket listen = new ServerSocket(GlobalFunctions.getPort("SERVER3"));
	            Semaphore sem = new Semaphore(1);

				while(true) {
					System.out.println("Waiting server 3...");
					Socket socket = listen.accept();
					new ConnectionServer3(socket,sem);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}

class ConnectionServer3 extends Thread{
	Socket socketCentral, socketRing;
	ObjectOutputStream osCentral, osRing;
	ObjectInputStream isCentral,isRing;
    Semaphore sem;

	 public ConnectionServer3(Socket socketCentral,Semaphore sem) {
		 try {
			 this.sem = sem;
			this.socketCentral = socketCentral;
			this.update("Connection accepted from ... "+socketCentral.getInetAddress());
			this.osCentral = new ObjectOutputStream(this.socketCentral.getOutputStream());
			this.isCentral = new ObjectInputStream(this.socketCentral.getInputStream());
			this.start();
		} catch (Exception e) {
			update(" -Error: "+e.getMessage());
		}
	}
	 
	 @Override
	 public void run() {
			try {
				Request r = (Request) this.isCentral.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					ControlRequest cr = (ControlRequest) r;
					if(cr.getSubtype().equals("OP_FILTER")) {
						update("Filter request sent to the processing node");
						this.doFilter(cr.getArgs().get(0).toString(), cr.getArgs().get(1).toString());
					}
				}else if(r.getType().equals("DATA_REQUEST")) {
					DataRequest dr = (DataRequest) r;
					if(dr.getSubtype().equals("OP_CPU")) {
						ControlResponse crsCPU = new ControlResponse("OP_CPU_OK");
	                    Random random = new Random();
	                    int rmd = random.nextInt(101);
	                    crsCPU.getArgs().add(rmd);
	                    this.osCentral.writeObject(crsCPU);
	                    update("DataCpu sent the CentralNode");
	                    this.doDisconnect();
					}
				}
			} catch (Exception e) {
				update(" -Error: "+e.getMessage());
			}
		}
		 
		 public void doFilter(String path, String filter) {
			 try {
				 if(filter.equals("FILTERA")) {
					 this.doConnect("SPECIALNODEA");
					 update("Filter A");
					 ControlRequest cr = new ControlRequest("OP_FILTER");
					 cr.getArgs().add(GlobalFunctions.getLessToken("FILTERA.txt"));
					 cr.getArgs().add(path);
					 cr.getArgs().add(filter);
					 this.osRing.writeObject(cr);
				 }else if(filter.equals("FILTERB")) {
					 this.doConnect("SPECIALNODEB");
					 update("Filter B");
					 System.out.println("Me conecto al SpecialNode del filtroB");
					 ControlRequest cr = new ControlRequest("OP_FILTER");
					 cr.getArgs().add(GlobalFunctions.getLessToken("FILTERB.txt"));
					 cr.getArgs().add(path);
					 cr.getArgs().add(filter);
					 this.osRing.writeObject(cr);
					 System.out.println("Envio la peticion");
				 }else if(filter.equals("FILTERC")) {
					 this.doConnect("SPECIALNODEC");
					 update("Filter C");
					 ControlRequest cr = new ControlRequest("OP_FILTER");
					 cr.getArgs().add(GlobalFunctions.getLessToken("FILTERC.txt"));
					 cr.getArgs().add(path);
					 cr.getArgs().add(filter);
					 this.osRing.writeObject(cr);
				 }
				 update("Processed node request received and sent to central Node");
				 ControlResponse crs = (ControlResponse) this.isRing.readObject();
				 System.out.println("Enviando respuesta");
				 this.doDisconnect();

				this.osCentral.writeObject(crs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				update(" -Error: "+e.getMessage());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				update(" -Error: "+e.getMessage());
			}
		 }
		 
		 public void update(String text) {
			 try {
				this.sem.acquire();
				SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		        Date date = new Date(System.currentTimeMillis());
		        GlobalFunctions.updateLog("MultiServer3.txt",(formatter.format(date)+" "+text));
				this.sem.release();
			} catch (Exception e) {
				// TODO: handle exception
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
				update(" -Error: "+e.getMessage());
	        }catch(IOException e) {
	            System.out.println(e.getMessage());
				update(" -Error: "+e.getMessage());
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("doConnect Server1 :"+e.getMessage());
				update(" -Error: "+e.getMessage());

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
				update(" -Error: "+e.getMessage());

			}
		 }
}

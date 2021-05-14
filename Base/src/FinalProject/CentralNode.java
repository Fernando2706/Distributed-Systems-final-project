package FinalProject;

import protocol.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

import Global.GlobalFunctions;
import java.io.*;

public class CentralNode {
    public static void main(String [] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("CENTRAL"));
            Semaphore sem = new Semaphore(1);

            while(true) {
                System.out.println("Waiting central node... "+listenSocket.getInetAddress()+":"+listenSocket.getLocalPort());
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+": Accepted connection from "+socket.getInetAddress().toString()));
            
                new ConnectionCentral(socket,sem);
            }
        }catch(Exception e){
            System.out.println("Main (Auth): "+e.getMessage());
        }
    }
}

class ConnectionCentral extends Thread{
    private Socket socketIn;
    public Socket [] socketservers;
    private ObjectOutputStream osIn;
    public ObjectOutputStream [] osServers;
    private ObjectInputStream isIn;
    public ObjectInputStream [] isServers;
    public int [] dataCpu;
    public Semaphore sem;

    public ConnectionCentral(Socket socket,Semaphore sem){
        try {
        	this.sem = sem;
            this.socketIn = socket;
            this.isIn = new ObjectInputStream(this.socketIn.getInputStream());
            this.osIn =  new ObjectOutputStream(this.socketIn.getOutputStream());  
            this.socketservers = new Socket[GlobalFunctions.getExternalVariables("MAXSERVERS")];
            this.osServers = new ObjectOutputStream[GlobalFunctions.getExternalVariables("MAXSERVERS")];
            this.isServers = new ObjectInputStream[GlobalFunctions.getExternalVariables("MAXSERVERS")];
            this.dataCpu = new int[GlobalFunctions.getExternalVariables("MAXSERVERS")];
            for(int i = 0; i < this.dataCpu.length; i++) dataCpu[i] = 0;
            this.start();
        } catch (Exception e) {
            System.out.println("ConnectionCentral: "+e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try{
            Request r = (Request) this.isIn.readObject();
            if(r.getType().equals("CONTROL_REQUEST")) {
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_LOGIN")) {
                    ControlResponse crs = new ControlResponse("OP_LOGIN_OK");
                    System.out.println(cr.getSubtype());
                    this.osIn.writeObject(crs);
                    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                    Date date = new Date(System.currentTimeMillis());
                    GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Login request accepted"));
                }else if(cr.getSubtype().equals("OP_FILTER")) {
                    this.doConnect();
                    this.doFilter(cr.getArgs().get(0).toString(),cr.getArgs().get(1).toString());
                    this.doDisconnect();
                }
            }
        }catch(ClassNotFoundException e){
            System.out.println("ClassNotFoundException run connectionCental: " + e.getMessage());
        } catch (IOException e) {
        	System.out.println(e.getMessage());
        	SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            try {
				this.sem.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Error:"+e.getMessage()));
            this.sem.release();
		}
    }

    private void doFilter(String path, String filter) {
        try{
        	SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            this.sem.acquire();
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Requesting cpu data from multiserver nodes"));
        	this.sem.release();
            System.out.println("Pido datos de CPU a los nodos MultiServidor");
        	DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 0; i < GlobalFunctions.getExternalVariables("MAXSERVERS"); i++){
                new DataCpu(this, "CPU", i, dr);
            }
        	
            while(true) {
            	int done = 0;
            	for(int data : this.dataCpu) if(data != 0) done++;
            	if(done == GlobalFunctions.getExternalVariables("MAXSERVERS")) break;
            }
        	System.out.println("Se ha terminado el recibo de datos y se procede a elegir al candidato");
            this.doDisconnect();
            this.doConnect();
            
            int minCpu = 100;
            int indexServer = -1;
            for(int i = 0; i < this.dataCpu.length; i++) {
            	System.out.println(this.dataCpu[i]);
                if(this.dataCpu[i] < minCpu) {
                    minCpu = this.dataCpu[i];
                    indexServer = i;
                }
            }
            if(indexServer !=-1) {
            	Date date1 = new Date(System.currentTimeMillis());
                GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date1)+" -Sending request to selected node"));
            	System.out.println("Se envia la peticion de filtrado al nodoserver"+indexServer);
            	ControlRequest cr = new ControlRequest("OP_FILTER");
            	cr.getArgs().add(path);
            	cr.getArgs().add(filter);
            	this.osServers[indexServer].writeObject(cr);
            	System.out.println("Se espera respuesta del Nodo");
            	ControlResponse crs = (ControlResponse) this.isServers[indexServer].readObject();
            	System.out.println("Se envia respuesta al cliente");
            	this.osIn.writeObject(crs);
            	Date date2 = new Date(System.currentTimeMillis());
            	this.sem.acquire();
                GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date2)+" -Sending reply to the client"));
                this.sem.release();
            }
            
        }catch(Exception e){
            System.out.println("Exception doFilter connectionCentral: " + e.getMessage());
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Error:"+e.getMessage()));
        	
        }
    }
    
    private void doConnect(){
        try {
            for(int i = 0; i< GlobalFunctions.getExternalVariables("MAXSERVERS");i++){
                if(this.socketservers[i] == null){
                    this.socketservers[i] = new Socket(GlobalFunctions.getIP("SERVER"+(i+1)),GlobalFunctions.getPort("SERVER"+(i+1)));
                    this.osServers[i] = new ObjectOutputStream(this.socketservers[i].getOutputStream());
                    this.isServers[i] = new ObjectInputStream(this.socketservers[i].getInputStream());
                }
            }
        } catch (Exception e) {
            System.out.println("ConnectionCentral (doConnect): "+e.getMessage());
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Error:"+e.getMessage()));
        }
    }

    private void doDisconnect(){
        try {
            for(int i = 0; i< GlobalFunctions.getExternalVariables("MAXSERVERS");i++){
                if(this.socketservers[i] != null){
                    this.osServers[i].close();
                    this.osServers[i] = null;
                    this.isServers[i].close();
                    this.isServers[i] = null;
                    this.socketservers[i].close();
                    this.socketservers[i] = null;
                }
            }
        } catch (Exception e) {
            System.out.println("ConnectionCentral (doDisconnect): "+e.getMessage());
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Error:"+e.getMessage()));
        }
    }
    
    public void doDisconnect(int index) {
    	try {
			if(this.socketservers[index]!=null) {
				this.osServers[index].close();
                this.osServers[index] = null;
                this.isServers[index].close();
                this.isServers[index] = null;
                this.socketservers[index].close();
                this.socketservers[index] = null;
			}
		} catch (Exception e) {
			System.out.println("ConnectionCentral (doDisconnect): "+e.getMessage());
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            GlobalFunctions.updateLog("CentralNode.txt",(formatter.format(date)+" -Error:"+e.getMessage()));
		}
    }
}

class DataCpu extends Thread {
    public ConnectionCentral connection;
    public int indexServer;
    private String type;
    private DataRequest dataRequest;
    boolean done;

    public DataCpu(ConnectionCentral connection, String type, int indexServer, DataRequest dataRequest) {
        System.out.println(indexServer);
    	this.connection = connection;
        this.type = type;
        this.indexServer = indexServer;
        this.dataRequest = dataRequest;
        this.done = false;
        this.start();
    }

	@Override
    public void run() {

        long start = System.currentTimeMillis();
        try{
        	Thread m = new Thread(new Masking(this));
            this.connection.osServers[this.indexServer].writeObject(this.dataRequest);
            m.start();
            ControlResponse crs = (ControlResponse) this.connection.isServers[this.indexServer].readObject();
            this.done = true;
            this.connection.dataCpu[this.indexServer] = Integer.valueOf(crs.getArgs().get(0).toString());
            
            long end = System.currentTimeMillis();
        }catch(IOException e) {
            System.out.println(e.getMessage());
            if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = 100;
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
            if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = 100;
        }
        
        try{
        }catch (Exception e) {
            System.out.println(e.getMessage());
            this.connection.dataCpu[this.indexServer] = 100;
        }
    }
}
class Masking extends Thread{
	private DataCpu data;
	
	public Masking(DataCpu data) {
		this.data = data;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			
			if(!this.data.done) {
				this.data.connection.doDisconnect(this.data.indexServer);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}


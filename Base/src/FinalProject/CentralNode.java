package FinalProject;

import protocol.*;
import java.net.*;
import Global.GlobalFunctions;
import java.io.*;

public class CentralNode {
    public static void main(String [] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("CENTRAL"));
            
            while(true) {
                System.out.println("Waiting central node... "+listenSocket.getInetAddress()+":"+listenSocket.getLocalPort());
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
            
                new ConnectionCentral(socket);
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

    public ConnectionCentral(Socket socket){
        try {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void doFilter(String path, String filter) {
        try{
        	
        	DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 0; i < GlobalFunctions.getExternalVariables("MAXSERVERS"); i++){
                new DataCpu(this, "CPU", i, dr);
            }
        	
            while(true) {
            	int done = 0;
            	for(int data : this.dataCpu) if(data != 0) done++;
            	if(done == GlobalFunctions.getExternalVariables("MAXSERVERS")) break;
            }
        	
            this.doDisconnect();
            this.doConnect();
            
            int minCpu = 100;
            int indexServer = -1;
            for(int i = 0; i < this.dataCpu.length; i++) {
                if(this.dataCpu[i] < minCpu) {
                    minCpu = this.dataCpu[i];
                    indexServer = i;
                }
            }
            if(indexServer !=-1) {
            	ControlRequest cr = new ControlRequest("OP_FILTER");
            	cr.getArgs().add(path);
            	cr.getArgs().add(filter);
            	this.osServers[indexServer].writeObject(cr);
            	ControlResponse crs = (ControlResponse) this.isServers[0].readObject();
            	this.osIn.writeObject(crs);
            }
            
        }catch(Exception e){
            System.out.println("Exception doFilter connectionCentral: " + e.getMessage());
        }
    }
    
    private void doConnect(){
        try {
            for(int i = 0; i< GlobalFunctions.getExternalVariables("MAXSERVERS");i++){
                if(this.socketservers[i] == null){
                	System.out.println("Trying to connect ..."+GlobalFunctions.getIP("SERVER"+(i+1))+":"+GlobalFunctions.getPort("SERVER"+(i+1)));
                    this.socketservers[i] = new Socket(GlobalFunctions.getIP("SERVER"+(i+1)),GlobalFunctions.getPort("SERVER"+(i+1)));
                    this.osServers[i] = new ObjectOutputStream(this.socketservers[i].getOutputStream());
                    this.isServers[i] = new ObjectInputStream(this.socketservers[i].getInputStream());
                }
            }
        } catch (Exception e) {
            System.out.println("ConnectionCentral (doConnect): "+e.getMessage());
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
        }
    }
}

class DataCpu extends Thread {
    private ConnectionCentral connection;
    private int indexServer;
    private String type;
    private DataRequest dataRequest;
    private boolean done;

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
            this.connection.osServers[this.indexServer].writeObject(this.dataRequest);
            ControlResponse crs = (ControlResponse) this.connection.isServers[this.indexServer].readObject();
            this.done = true;
            this.connection.dataCpu[this.indexServer] = Integer.valueOf(crs.getArgs().get(0).toString());
            
        }catch(IOException e) {
            System.out.println(e.getMessage());
            if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = 100;
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
            if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = 100;
        }
        long end = System.currentTimeMillis();
        
        try{
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}


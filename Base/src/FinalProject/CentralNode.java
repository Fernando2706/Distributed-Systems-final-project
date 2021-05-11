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
                System.out.println("Waiting auth node...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
            
                new Connection(socket);
            }
        }catch(Exception e){
            System.out.println("Main (Auth): "+e.getMessage());
        }catch(IOException e) {
            System.out.println("Listen socket: " + e.getMessage());
        }
    }
}

class ConnectionCentral extends Thread{
    private Socket socketIn;
    private Socket [] socketservers;
    private ObjectOutputStream osIn;
    private ObjectOutputStream [] osServers;
    private ObjectInputStream isIn;
    private ObjectInputStream [] isServers;
    private int [] dataCpu;

    public ConnectionCentral(Socket socket){
        try {
            this.socketIn = socket;
            this.isIn = new ObjectInputStream(this.socketIn.getInputStream());
            this.osIn =  new ObjectOutputStream(this.socketIn.getOutputStream());               
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
                ControlResquest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_LOGIN")) {
                    ControlResponse crs = new ControlResponse("OP_LOGIN_OK");
                    this.osIn.writeObject(crs);
                }else if(cr.getSubtype.equals("OP_FILTER")) {
                    this.doConnect();
                    this.doFilter();
                    this.doDisconnect();
                }
            }
        }catch(ClassNotFoundException e){
            System.out.println("ClassNotFoundException run connectionCental: " + e.getMessage());
        }
    }

    private void doFilter() {
        try{

        }catch(Exception e){
            System.out.println("Exception doFilter connectionCentral: " + e.getMessage());
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
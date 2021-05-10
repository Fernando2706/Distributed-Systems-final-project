package FinalProject;

import protocol.*;

import java.net.*;

import Global.GlobalFunctions;

import java.io.*;

public class Authentication {
    public static void main(String [] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getIP("AUTH"));
            
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

class Connection extends Thread{

    private Socket sClient,sCentral;
    private ObjectInputStream isClient,isCentral;
    private ObjectOutputStream osClient, osCentral;

    public Connection(Socket client){
        try {
            this.sClient = client;
            this.osClient = new ObjectOutputStream(this.sClient.getOutputStream());
            this.isClient = new ObjectInputStream(this.sClient.getInputStream());
            this.start();
        } catch (Exception e) {
            System.out.println("Connection (Auth): "+e.getMessage());
        }
    }

    public void run() {
        try{
            Request r = (Request) this.isClient.readObject();
            if(r.getType().equals("CONTROL_REQUEST")) {
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_LOGIN")) {
                    this.doConnect();
                    this.doLogin((byte []) cr.getArgs().get(0),(byte []) cr.getArgs().get(1));
                    this.doDisconnet();
                }
            }
        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException connection (Auth): " + e.getMessage());
        }catch(IOException e) {
            System.out.println("Readline connection (Auth): " + e.getMessage());
        }
    }

    private void doLogin(byte [] email, byte[] password) {
        try{
            String emailDecript = GlobalFunctions.decrypt(email);
            String passDecript = GlobalFuncitons.decrypt(password);
        }catch(Exception e){
            System.out.println("doLogin (Auth): " + e.getMessage());
        }
    }

    private void doConnect(){
        try {
            if(this.sCentral==null){
                this.sCentral = new Socket(GlobalFunctions.getIP("CENTRAL"),GlobalFunctions.getPort("CENTRAL"));
                this.osCentral = new ObjectOutputStream(this.sCentral.getOutputStream());
                this.isCentral = new ObjectInputStream(this.sCentral.getInputStream());
            }
        } catch (Exception e) {
            System.out.println("doConnect (Auth): "+e.getMessage());
        }
    }
    
    private void doDisconnet(){
       try {
           if(this.sCentral!=null){
               this.osCentral.close();
               this.osCentral = null;
               this.isCentral.close();
               this.isCentral = null;
               this.sCentral.close();
               this.sCentral=null;
           }
       } catch (Exception e) {
           System.out.println("doDisconnect (Auth): " + e.getMessage());
       }
    }

}
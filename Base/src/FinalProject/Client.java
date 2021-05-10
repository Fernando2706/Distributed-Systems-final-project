package FinalProject;

import protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.sql.ClientInfoStatus;

import Global.GlobalFunctions;

public class Client {
    public final static String version = "1.0";

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Console console;
    private String nick;

    public static void main(String[] args) {
        new Client();
    }

    public void init() {
        try{
            this.console = new Console(this.version);
        }catch(Exception e) {
            System.out.println("Exception init client: " + e.getMessage());
        }
    }

    public Client() {
        this.init();

        String cmd = this.console.getCommand();

        while(!cmd.equals("close")) {
            try{
                if(cmd.equals("login")) {
                    if(this.nick == "") {
                        
                    }else throw new Exception("There is another user connected");
                }else if(cmd.equals("filter")) {
                    if(this.nick != "") {

                    }
                }
            }catch(Exception e) {
                System.out.println("Exception client constructor: " + e.getMessage());
            }

            cmd = this.console.getCommand();
        }
    }

    private void doConnectAuth(String node, int count){
        try {
            if(this.socket == null) {
                this.socket = new Socket(GlobalFunctions.getIP(node+count),GlobalFunctions.getPort(node+count));
    
                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        }catch(UncheckedIOException e) {
            System.out.println("Client (Auth)"+e.getMessage());

        }catch(IOException e) {
            System.out.println("Client (Auth)"+e.getMessage());

        }catch(Exception e) {
            System.out.println("Client (Auth)"+e.getMessage());
        }
    }
    private void doConnectCentral(String node){
        try {
            if(this.socket == null) {
                this.socket = new Socket(GlobalFunctions.getIP(node),GlobalFunctions.getPort(node));
    
                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        }catch(UncheckedIOException e) {
            System.out.println("Client (Central)"+e.getMessage());

        }catch(IOException e) {
            System.out.println("Client (Central)"+e.getMessage());

        }catch(Exception e) {
            System.out.println("Client (Central)"+e.getMessage());
        }
    }

    private void doDisconnet(){
        try {
            if(this.socket!=null){
                this.os.close();
                this.os = null;
                this.is.close();
                this.is = null;
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            System.out.println("Disconnect (Auth): "+e.getMessage());
        }
    }

}
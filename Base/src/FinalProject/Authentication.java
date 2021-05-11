package FinalProject;

import protocol.*;
import java.net.*;
import Global.GlobalFunctions;
import java.io.*;

public class Authentication {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getIP("AUTH"));

            while (true) {
                System.out.println("Waiting auth node...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());

                new Connection(socket);
            }
        } catch (Exception e) {
            System.out.println("Main (Auth): " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Listen socket: " + e.getMessage());
        }
    }
}

class Connection extends Thread {

    private Socket sClient, sCentral;
    private ObjectInputStream isClient, isCentral;
    private ObjectOutputStream osClient, osCentral;
    private boolean done;
    private long start, end;

    public Connection(Socket client) {
        try {
            this.sClient = client;
            this.osClient = new ObjectOutputStream(this.sClient.getOutputStream());
            this.isClient = new ObjectInputStream(this.sClient.getInputStream());
            this.done = false;
            this.start = 0L;
            this.end = 0L;
            this.start();
        } catch (Exception e) {
            System.out.println("Connection (Auth): " + e.getMessage());
        }
    }

    public void run() {
        try {
            Request r = (Request) this.isClient.readObject();
            if (r.getType().equals("CONTROL_REQUEST")) {
                ControlRequest cr = (ControlRequest) r;
                if (cr.getSubtype().equals("OP_LOGIN")) {
                    this.doConnect();
                    this.doLogin((byte[]) cr.getArgs().get(0), (byte[]) cr.getArgs().get(1));
                    this.doDisconnet();
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException connection (Auth): " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Readline connection (Auth): " + e.getMessage());
        }
    }

    private void doLogin(byte[] email, byte[] password) {
        try {
            String emailDecrypt = GlobalFunctions.decrypt(email);
            String passDecrypt = GlobalFuncitons.decrypt(password);

            if (GlobalFunctions.isUser(emailDecrypt) && GlobalFunctions.getPassword(emailDecrypt).equals(passDecrypt)) {
                ControlRequest cr = new ControlRequest("OP_LOGIN");
                this.osCentral.writeObject(cr);

                Thread inactiveCentral = new Thread(new InactiveCentral("AuthLogin"));
                inactiveCentral.start();

                ControlResponse crs = (ControlRespose) this.isCentral.readObject();
                this.done = true;
                this.osClient.writeObject(crs);
                GlobalFunctions.setLatency("AuthLogin", (this.end - this.start));
                this.resetCurrentTime();
            }
        } catch (Exception e) {
            System.out.println("doLogin (Auth): " + e.getMessage());
        }
    }

    private void doConnect() {
        try {
            if (this.sCentral == null) {
                this.sCentral = new Socket(GlobalFunctions.getIP("CENTRAL"), GlobalFunctions.getPort("CENTRAL"));
                this.osCentral = new ObjectOutputStream(this.sCentral.getOutputStream());
                this.isCentral = new ObjectInputStream(this.sCentral.getInputStream());
            }
        } catch (Exception e) {
            System.out.println("doConnect (Auth): " + e.getMessage());
        }
    }

    private void doDisconnet() {
        try {
            if (this.sCentral != null) {
                this.osCentral.close();
                this.osCentral = null;
                this.isCentral.close();
                this.isCentral = null;
                this.sCentral.close();
                this.sCentral = null;
            }
        } catch (Exception e) {
            System.out.println("doDisconnect (Auth): " + e.getMessage());
        }
    }

    private void resetCurrentTime() {
        this.start = 0L;
        this.end = 0L;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public void setDone(boolean done) {
        this.done = done;
    }
    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
    }
}

class InactiveCentral implements Runnable {
    private Connection connection;

    public InactiveCentral(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        long sleep = 1000;

        try {
            sleep = GlobalFunctions.getLatency(this.type);
        } catch (Exception e) {
            System.out.println("InactiveCentral run: " + e.getMessage());
        }

        try {
            Thread.sleep(sleep);
            if (!this.client.isDone()) {
                this.client.doDisconnect();
                GlobalFunctions.setLatency(this.type, GlobalFunctions.getLatency(this.client.getNumberClient()) * 2);
            }
            this.client.setDone(false);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException run InactiveCentral: " + e.getMessage());
        }
    }
}
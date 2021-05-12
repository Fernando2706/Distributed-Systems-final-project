package FinalProject;

import protocol.*;
import java.net.*;
import Global.GlobalFunctions;
import java.io.*;

public class Authentication {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("AUTH"));

            while (true) {
                System.out.println("Waiting auth node...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());

                new Connection(socket);
            }
        } catch (Exception e) {
            System.out.println("Main (Auth): " + e.getMessage());
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
                }else if(cr.getSubtype().equals("OP_REGISTER")) {
                    if(!GlobalFunctions.isUser(GlobalFunctions.decrypt((byte []) cr.getArgs().get(1)))) {
                        GlobalFunctions.addUser((byte[]) cr.getArgs().get(0), (byte []) cr.getArgs().get(1), (byte []) cr.getArgs().get(2));
                        this.osClient.writeObject(new ControlResponse("OP_REGISTER_OK"));
                    }else {
                        this.osClient.writeObject(new ControlResponse("OP_REGISTER_NOK"));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException connection (Auth): " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Readline connection (Auth): " + e.getMessage());
        }catch (Exception e){
            try {
            	this.osClient.writeObject(new ControlResponse("OP_REGISTER_NOK"));
			} catch (Exception e2) {
				// TODO: handle exception
			}
        }
    }

    private void doLogin(byte[] email, byte[] password) {
        try {
            String emailDecrypt = GlobalFunctions.decrypt(email);
            String passDecrypt = GlobalFunctions.decrypt(password);
            if (GlobalFunctions.isUser(emailDecrypt) && GlobalFunctions.getPassword(emailDecrypt).equals(passDecrypt)) {
                ControlRequest cr = new ControlRequest("OP_LOGIN");
                this.osCentral.writeObject(cr);
                Thread inactiveCentral = new Thread(new InactiveCentral(this,"AuthLogin"));
                inactiveCentral.start();

                ControlResponse crs = (ControlResponse) this.isCentral.readObject();
                this.done = true;
                //crs.getArgs().add(GlobalFunctions.getUserName(emailDecrypt));
                this.osClient.writeObject(crs);
                GlobalFunctions.setLatency("AuthLogin", (this.end - this.start));
                this.resetCurrentTime();
            }else {
                this.osClient.writeObject(new ControlResponse("OP_LOGIN_NOK"));
            }
        } catch (Exception e) {
            System.out.println("doLogin (Auth): " + e.getMessage());
            e.printStackTrace();
            /*try {
				this.osClient.writeObject(new ControlResponse("OP_LOGIN_NOK"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
        }
    }

    private void doConnect() {
        try {
            if (this.sCentral == null) {
            	System.out.println(GlobalFunctions.getIP("CENTRAL")+":"+GlobalFunctions.getPort("CENTRAL"));
                this.sCentral = new Socket(GlobalFunctions.getIP("CENTRAL"), GlobalFunctions.getPort("CENTRAL"));
                this.osCentral = new ObjectOutputStream(this.sCentral.getOutputStream());
                this.isCentral = new ObjectInputStream(this.sCentral.getInputStream());
            }
        } catch (Exception e) {
            System.out.println("doConnect (Auth): " + e.getMessage());
        }
    }

    public void doDisconnet() {
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
    private String type;

    public InactiveCentral(Connection connection,String type) {
        this.connection = connection;
        this.type = type;
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
            if (!this.connection.isDone()) {
            	System.out.println("Central out");
                this.connection.doDisconnet();
                GlobalFunctions.setLatency(this.type, GlobalFunctions.getLatency(this.type) * 2);
            }
            this.connection.setDone(false);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException run InactiveCentral: " + e.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
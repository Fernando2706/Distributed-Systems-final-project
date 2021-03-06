package FinalProject;

import protocol.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.sql.ClientInfoStatus;

import javax.imageio.ImageIO;

import Global.GlobalFunctions;

public class Client {
    public final  String version = "1.0";

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Console console;
    private String nick;
    private long start, end;
    private boolean done;

    public static void main(String[] args) {
        new Client();
    }

    public void init() {
        try {
            this.console = new Console(this.version);
            this.start = 0;
            this.start = 0;
            this.done = false;
            this.nick="";
        } catch (Exception e) {
            System.out.println("Exception init client: " + e.getMessage());
        }
    }

    public Client() {
        this.init();

        String cmd = this.console.getCommand();

        while (!cmd.equals("close")) {
            try {
                if (cmd.equals("login")) {
                    if (this.nick == "") {
                        this.doConnectAuth(1);
                        String[] credentials = this.console.getCommandLogin();
                        this.doLogin(credentials);
                        this.doDisconnet();
                    } else
                        throw new Exception("There is another user connected");
                } else if(cmd.equals("register")) {
                    if(this.nick == "") {
                        this.doConnectAuth(1);
                        String [] credentials = this.console.getCommandRegister();
                        this.doRegister(credentials);
                        this.doDisconnet();
                    }else throw new Exception("You can only register when they are no users online");
                } else if (cmd.equals("filter")) {
                    if (this.nick != "") {
                        this.doConnectCentral();
                        String[] params = this.console.getCommandFilter();
                        this.doFilter(params);
                        this.doDisconnet();
                    } else
                        throw new Exception("You need to be connected to use the filter command");
                } else if (cmd.equals("logout")) {
                    if (this.nick != "") {
                        this.nick = "";
                        this.console.setPrompt("v", this.version);
                        this.console.writeMessage("Disconnecting from user account...");
                    } else
                        throw new Exception("You were already logout");
                }
            } catch (Exception e) {
                System.out.println("Exception client constructor: " + e.getMessage());
            }

            cmd = this.console.getCommand();
        }
    }

    private void doLogin(String[] credentials) {
        try {
            this.start = System.currentTimeMillis();
            ControlRequest cr = new ControlRequest("OP_LOGIN");
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[1]));

            this.os.writeObject(cr);

            Thread inactiveAuth = new Thread(new InactiveNode(this, "ClientLogin"));
            inactiveAuth.start();

            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crs.getSubtype().equals("OP_LOGIN_OK")){
                this.nick = "fernando";
                //System.out.println(crs.getArgs().get(0).toString());
                this.console.setPrompt(this.nick, this.version);
                this.console.writeMessage("Login OK");
            }else if(crs.getSubtype().equals("OP_LOGIN_NOK")){
                this.console.writeMessage("Login failed...");
            }
            this.end = System.currentTimeMillis();

            GlobalFunctions.setLatency("ClientLogin",(this.end-this.start));
            this.resetCurrentTime();
        } catch (Exception e) {
            System.out.println("DoLogin (Client): " + e.getMessage());
        }
    }

    private void doFilter(String[] params) {
        try {
        	BufferedImage image = ImageIO.read(new File(params[0]));
        	ImageIO.write(image,"jpg",new File("//FERNANDO/publico/img/image.jpg"));
            this.start = System.currentTimeMillis();
            ControlRequest cr = new ControlRequest("OP_FILTER");
            cr.getArgs().add("//FERNANDO/publico/img/image.jpg");
            cr.getArgs().add(params[1]);

            this.os.writeObject(cr);
            Thread inactiveCentral = new Thread(new InactiveNode(this, "ClientFilter"));
            inactiveCentral.start();

            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done=true;
            if(crs.getSubtype().equals("OP_FILTER_OK")){
                this.console.writeMessage(crs.getArgs().get(0).toString());
            }else if(crs.getSubtype().equals("OP_FILTER_NOK")){
                this.console.writeMessage("There was a problem while filtering the image");
            }
            this.end = System.currentTimeMillis();
            GlobalFunctions.setLatency("ClientFilter", (this.end-this.start));

        } catch (Exception e) {
            System.out.println("doFilter (Client): " + e.getMessage());
        }
    }

    private void doRegister(String [] credentials) {
        try{
            this.start = System.currentTimeMillis();
            ControlRequest cr = new ControlRequest("OP_REGISTER");
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[1]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[2]));

            this.os.writeObject(cr);
            Thread inactiveAuth = new Thread(new InactiveNode(this,"ClientRegister"));
            inactiveAuth.start();

            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crs.getSubtype().equals("OP_REGISTER_OK")) {
                this.console.writeMessage("You have register successfuly");
            }else if(crs.getSubtype().equals("OP_REGISTER_NOK")) {
                this.console.writeMessage("There was a problem during the registration");
            }
            this.end = System.currentTimeMillis();
            GlobalFunctions.setLatency("ClientRegister", (this.end-this.start));
        }catch(Exception e) {
            System.out.println("Exception doRegister (Client): " + e.getMessage());
        }
    }

    private void doConnectAuth(int count) {
        try {
            if(count > GlobalFunctions.getExternalVariables("MAXAUTH")) throw new Exception("All the authentification nodes are disconnected");
            if (this.socket == null) {
                this.socket = new Socket(GlobalFunctions.getIP("AUTH" + count), GlobalFunctions.getPort("AUTH" + count));
                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        } catch (UncheckedIOException e) {
            System.out.println("Client (Auth)" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Client (Auth)" + e.getMessage());
            this.doConnectAuth(count++);
        } catch (Exception e) {
            System.out.println("Client (Auth)" + e.getMessage());
        }
    }

    private void doConnectCentral() {
        try {
            if (this.socket == null) {
                this.socket = new Socket(GlobalFunctions.getIP("CENTRAL"), GlobalFunctions.getPort("CENTRAL")); 

                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        } catch (UncheckedIOException e) {
            System.out.println("Client (Central)" + e.getMessage());

        } catch (IOException e) {
            System.out.println("Client (Central)" + e.getMessage());

        } catch (Exception e) {
            System.out.println("Client (Central)" + e.getMessage());
        }
    }

    public void doDisconnet() {
        try {
            if (this.socket != null) {
                this.os.close();
                this.os = null;
                this.is.close();
                this.is = null;
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            System.out.println("Disconnect (Auth): " + e.getMessage());
        }
    }

    private void resetCurrentTime() {
        this.start = 0L;
        this.end = 0L;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return the os
     */
    public ObjectOutputStream getOs() {
        return this.os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(ObjectOutputStream os) {
        this.os = os;
    }

    /**
     * @return the is
     */
    public ObjectInputStream getIs() {
        return this.is;
    }

    /**
     * @param is the is to set
     */
    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    /**
     * @return the console
     */
    public Console getConsole() {
        return this.console;
    }

    /**
     * @param console the console to set
     */
    public void setConsole(Console console) {
        this.console = console;
    }

    /**
     * @return the nick
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * @param nick the nick to set
     */
    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return this.start;
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
        return this.end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
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
}

class InactiveNode implements Runnable {
    private Client client;
    private String type;

    public InactiveNode(Client client, String type) {
        this.client = client;
        this.type = type;
    }

    @Override
    public void run() {
        long sleep = 1000;
        
        try{
            sleep = GlobalFunctions.getLatency(this.type);
        }catch(Exception e) {
            System.out.println("InactiveNode run: " + e.getMessage());
        }

        try {
            Thread.sleep(sleep);
            if(!this.client.isDone()) {
            	System.out.println("Proxy out");
                this.client.doDisconnet();
                GlobalFunctions.setLatency(this.type, GlobalFunctions.getLatency(this.type)*2);
            }
            this.client.setDone(false);
        }catch(InterruptedException e) {
            System.out.println("InterruptedException run InactiveNode: " + e.getMessage());
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
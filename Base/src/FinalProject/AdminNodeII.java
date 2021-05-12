package FinalProject;

import Global.GlobalFunctions;

import java.io.*;

public class AdminNodeII {
    public final String version = "1.0";

    private Console console;

    public static void main(String[] args) {
        new AdminNodeII();
    }

    public void init() {
        this.console = new Console(this.version);
        this.console.setPrompt("AdminII", this.version);
    }

    public AdminNodeII(){
        this.init();

        String cmd = this.console.getCommand();

        while(!cmd.equals("close")){
            try {
                if(cmd.equals("register")) {
                    String [] credentials = this.console.getCommandRegister();
                    if(!GlobalFunctions.isUser(credentials[1])) {
                        GlobalFunctions.addUser(GlobalFunctions.encryptMessage(credentials[0]), GlobalFunctions.encryptMessage(credentials[1]), GlobalFunctions.encryptMessage(credentials[2]));
                        this.console.writeMessage("You have register successfuly");
                    }else {
                        this.console.writeMessage("There was a problem during the registration");
                    }
                }else if(cmd.equals("delete")) {
                    String email = this.console.getEmail();
                    if(GlobalFunctions.isUser(email)){
                        if(!GlobalFunctions.deleteUser(email)){
                            this.console.writeMessage("There was a probleam deleting the user or the user doenst exists");
                        }else this.console.writeMessage("User deleted successfully");
                    }else{
                        this.console.writeMessage("User not found...");
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception Node Admin II constructor: "+e.getMessage());
            }

            cmd = this.console.getCommand();
        }
    }
}
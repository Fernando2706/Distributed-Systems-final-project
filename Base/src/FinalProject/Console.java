package FinalProject;

import java.io.*;

public class Console {
    public static String prompt;
    private InputStreamReader isr;
    private BufferedReader br;
    private String version, nick = "v";
    
    public Console(String version){
        this.isr = new InputStreamReader(System.in);
        this.br = new BufferedReader(this.isr);
        this.version = version;
        Console.prompt = "Cliente v " + this.version + "> ";
    }

    public void writeMessage(String msg) {
        System.out.println("> " + msg);
    }
    
    public String getCommand() {
        String line = "";

        try {
            System.out.println(Console.prompt);
            line = this.br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        return line;
    }

    public String[] getCommandLogin() {
        String [] credentials = new String[2];

        try {
            System.out.print("Choose an email: ");
            credentials[0] = this.br.readLine();

            System.out.print("Choose a password: ");
            credentials[1] = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        return credentials;
    }

    public String[] getCommandRegister() {
        String [] credentials = new String[3];

        try {
            System.out.println("User name: ");
            credentials[0] = this.br.readLine();

            System.out.print("Email: ");
            credentials[1] = this.br.readLine();

            System.out.print("Password: ");
            credentials[2] = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return credentials;
    }

    public String[] getCommandFilter() {
        String [] params = new String[2];

        try {
            System.out.print("Path to the image: ");
            params[0] = this.br.readLine();
            System.out.println("\tGrayFilter: FILTERA");
            System.out.println("\tOppositedFilter: FILTERB");
            System.out.println("\tFilterFilter: FILTERC");
            System.out.print("Type of filter: ");
            params[1] = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return params;
    }

    public String getEmail(){
        String email = "";

        try {
            System.out.print("Enter an email ");
            email = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return email;
    }



    public void setPrompt(String nick, String version) {
		if(nick == null) nick = "v";
		else if(nick != "v"){
			if(nick.length() == 1) nick = nick.toUpperCase();
			else nick = String.valueOf(nick.charAt(0)).toUpperCase() + nick.substring(1, nick.length());
		}
		if(version == null) version = "1.0";
				
		Console.prompt = "Client " + nick + " " + version + "> ";
	}
}
package Global;


import java.io.File;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class GlobalFunctions {
    static Cipher getCipher(boolean allowEncrypt) throws Exception {
        final String private_key = "idbwidbwjNFJERNFEJNFEJIuhifbewbaicaojopqjpu3873kxnmknmakKAQIAJ3981276396";
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(private_key.getBytes("UTF-8"));
        final SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");
    
        final Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
        if(allowEncrypt) {
            aes.init(Cipher.ENCRYPT_MODE, key);
        } else {
            aes.init(Cipher.DECRYPT_MODE, key);
        }
    
        return aes;
    }

    public static byte[] encryptMessage(String message) throws Exception {
        final byte[] bytes = message.getBytes("UTF-8");
        final Cipher aes = GlobalFunctions.getCipher(true);
        final byte[] encryptedMessage = aes.doFinal(bytes);
        return encryptedMessage;
    }

    public static String decrypt(byte [] encryptedMessage) throws Exception {
        final Cipher aes = GlobalFunctions.getCipher(false);
        final byte [] bytes = aes.doFinal(encryptedMessage);
        final String message = new String(bytes, "UTF-8");
        return message;
    }
    
    public static synchronized void setLatency(String type,long latency) throws Exception {
        File file = new File(type + ".txt");
        int i = 0;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency += Long.valueOf(scanner.next());
                i++;
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(latency/(i+1));
            outputFile.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
    }

    public static int getExternalVariables(String name) throws Exception {
        File file = new File("ExternalVariables.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                String [] port = scanner.nextLine().split(" ");
                if(port[0].equals(name)) return Integer.valueOf(port[1]);
            }
            scanner.close();
        }else {
            throw new Exception("The file ExternalVariables.txt does not exist");
        }
        if(name.equals("MAXSERVER")) return 0;
        return 8000;
    }

    public static synchronized long getLatency(String type) throws Exception {
        File file = new File(type + ".txt");
        long latency = 300;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency = Long.valueOf(scanner.next());
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        return latency;
    }

    public static int getPort(String name) throws Exception {
        File file = new File("PORT.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                String [] port = scanner.nextLine().split(" ");
                if(port[0].equals(name)) return Integer.valueOf(port[1]);
            }
            scanner.close();
        }else {
            throw new Exception("The file ExternalVariables.txt does not exist");
        }
        if(name.equals("MAXSERVER")) return 0;
        return 8000;
    }

    public static String getIP(String name) throws Exception {
        File file = new File("IP.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                String [] port = scanner.nextLine().split(" ");
                if(port[0].equals(name)) return port[1];
            }
            scanner.close();
        }else {
            throw new Exception("The file ExternalVariables.txt does not exist");
        }
        return "localhost";
    }
    
    public static void initFile(String name) {
    	try {    		
    		File file = new File(name);
    		PrintWriter outputfile = new PrintWriter(file);
    		if(name.equals("ClientLatency.txt")) {
    			outputfile.print(500);
    		}else if(name.contains("Proxy")) {
    			outputfile.print(300);
    		}else if(name.contains("Server")){
    			outputfile.print(0);
    		}
    		outputfile.close();
    	}catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    }

    public static synchronized void addUser(byte [] user, byte [] email, byte [] password) throws Exception {
    	String users = "";
    	File file = new File("Users.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                users += scanner.nextLine() + "\n";
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(users);
            for(int i = 0; i < email.length; i++) {
            	if(i == email.length-1) outputFile.print(email[i]);
            	else outputFile.print(email[i] + " ");
            }
            outputFile.print("/");
            for(int i = 0; i < password.length; i++) {
            	if(i == password.length-1) outputFile.print(password[i]);
            	else outputFile.print(password[i] + " ");
            }
            outputFile.print("/");
            for(int i = 0; i < user.length; i++){
                if(i == user.length-1) outputFile.print(email[i]);
                else outputFile.print(user[i]+ " ");
            }
            outputFile.close();
        }else {
            throw new Exception("The file "+file.getName()+" does not exist");
        }
    }

    public static synchronized boolean isUser(String email) throws Exception {
    	File file = new File("Users.txt");
        if(file.exists()) {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
            	String [] encryptedName = scanner.nextLine().split("/")[0].split(" ");
                byte [] nameInByte = new byte[encryptedName.length];
                for(int i = 0; i < encryptedName.length; i++) {
                	nameInByte[i] = Byte.valueOf(encryptedName[i]);
                }
                if(GlobalFunctions.decrypt(nameInByte).equals(email)) {
                	return true;
                }
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        
        return false;
    }
    
    public static synchronized String getPassword(String email) throws Exception {
    	File file = new File("Users.txt");
        if(file.exists()) {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
            	String [] encryptedPair = scanner.nextLine().split("/");
            	String [] encryptedEmail = encryptedPair[0].split(" ");
            	String [] encryptedPassword = encryptedPair[1].split(" ");
                byte [] nameInByte = new byte[encryptedEmail.length], passwordInByte = new byte[encryptedPassword.length];
                for(int i = 0; i < encryptedEmail.length; i++) nameInByte[i] = Byte.valueOf(encryptedEmail[i]);
                for(int i = 0; i < encryptedPassword.length; i++) passwordInByte[i] = Byte.valueOf(encryptedPassword[i]);
                if(GlobalFunctions.decrypt(nameInByte).equals(email)) {
                	return GlobalFunctions.decrypt(passwordInByte);
                }
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        
        return "";
    }

    public static synchronized String getUserName(String email) throws Exception {
        File file = new File("Users.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()) {
                String [] encryptedPair = scanner.nextLine().split("/");
            	String [] encryptedEmail = encryptedPair[0].split(" ");
            	String [] encryptedPass = encryptedPair[1].split(" ");
            	String [] encryptedName = encryptedPair[2].split(" ");
            	System.out.println(encryptedEmail.length+" "+encryptedPass.length);
                byte [] emailInByte = new byte[encryptedEmail.length], nameInByte = new byte[encryptedName.length];
                for(int i = 0; i < encryptedEmail.length; i++) emailInByte[i] = Byte.valueOf(encryptedEmail[i]);
                for(int i = 0; i < encryptedName.length; i++) nameInByte[i] = Byte.valueOf(encryptedName[i]);
                if(GlobalFunctions.decrypt(emailInByte).equals(email)) {
                	String aux =GlobalFunctions.decrypt(nameInByte);
                	return aux;
                }
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }

        return "";
    }

    public static synchronized boolean deleteUser(String email) throws Exception {
        File file = new File("Users.txt");
        boolean done = false;
        String users = "";
        int index = 0;
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()) {
            	
                String encryptedUser = scanner.nextLine();
                String [] encryptedEmail = encryptedUser.split("/")[0].split(" ");
                byte [] emailInByte = new byte[encryptedEmail.length];
                for(int i = 0; i < encryptedEmail.length; i++) emailInByte[i] = Byte.valueOf(encryptedEmail[i]);
                if(!GlobalFunctions.decrypt(emailInByte).equals(email)){
                	System.out.println(index+"	"+users);
                    users += (encryptedUser+"\n");
                    
                }else done = true;
                index++;
            }
            scanner.close();
        }
        System.out.println(users);
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.print(users);
        printWriter.close();
        return done;
    }
}
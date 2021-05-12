package Procesado1;

import java.net.ServerSocket;
import java.net.Socket;

import Global.GlobalFunctions;

public class Node2A {

	public static void main(String[] args) {
		try {
			ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getPort("NODEA2"));
			
			while(true) {
				System.out.println("Waiting node 2...");

				Socket s = listenSocket.accept();
				new ConnectionNodeA(2, GlobalFunctions.getExternalVariables("MAXNODES"), s,args);

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}



package test;

import Global.GlobalFunctions;

public class TestToken {

	public static void main(String[] args) {
		System.out.println(GlobalFunctions.getLessToken("FILTERA.txt"));
		GlobalFunctions.updateToken("FILTERA.txt", 2);

	}

}

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class URLReader {
	
	final public static String CHAT_BASE = "http://prithvijc.pythonanywhere.com/chat?instr=";
	final public static String ENROLL = "@";
	final public static String QUIT = "$";
	final public static String CHK_USR = "~";
	final public static String SEND_MSG = "!";
	final public static String CHK_MSG = "^";
	
	public static void enroll(String uname) throws Exception{
		ping(CHAT_BASE+ENROLL+uname);
	}
	
	public static void quit(String uname) throws Exception{
		ping(CHAT_BASE+QUIT+uname);
	}
	
	public static boolean isOnline(String uname) throws Exception{
		String res = ping(CHAT_BASE+CHK_USR+uname);
		boolean b[] = {false,true};
		return b[Integer.parseInt(res)];
	}
	
	public static void sendMsg(String msg, String uname) throws Exception{
		ping(CHAT_BASE+SEND_MSG+msg+ENROLL+uname);
	}
	
	public static String readMsg(String uname) throws Exception{
		String res = ping(CHAT_BASE+CHK_MSG+uname).trim();
		return res;
	}
	
	public static String ping(String url) throws Exception {
    	URL targetUrl = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(targetUrl.openStream()));
        String inputLine, txt = "";
        while ((inputLine = in.readLine()) != null)
        	txt += "\n"+inputLine;
        in.close();
        return txt.trim();
    }
}
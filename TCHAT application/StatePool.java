import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class StatePool {

	//Streams for the hardware device
	Scanner in;
	OutputStream out;
	
	//State of hardware device connection
	boolean isConnected = false;
	
	//Streams for the server
	BufferedReader servIn;
	PrintWriter servOut;
	
	//Chat usernames
	String userName = "";
	String clientUname = "";
	
	//State for chat session
	boolean inChat = false;
	
	//Flag to exit the app
	boolean exitFlag = false;
	
	//Current sensing mode
	int senseMode = TchatApp.IN_MOUSE;
	
	//Current receiving mode
	int recMode = TchatApp.REC;
}

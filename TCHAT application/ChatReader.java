import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatReader {
	
	/*StatePool spool;
	Plotter plotter;
	JTextArea chatLog;
	int delay = 1000;

	public ChatReader(StatePool spool, Plotter p, JTextArea chatLog){
		this.spool = spool;
		this.plotter = p;
		this.chatLog = chatLog;
	}

	public void run(){
		while(!spool.exitFlag){
			byte[] buff = new byte[2048];
			int x,y;
			while(spool.inChat == false){
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) { e.printStackTrace();}
			}
	        try{
	        	//Read messages from the server
				if(spool.isConnected){
					//Send messages over the server
					//Read position from server
					try{
						String msg = URLReader.readMsg(spool.userName);
						if(msg.length() > 0)
							chatLog.append(msg);
					}catch(Exception e){}
				}
				//Send recreate instruction
				//spool.out.write(PortConnector.STIM_POS);
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}*/
}
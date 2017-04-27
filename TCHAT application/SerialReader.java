import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * 
Send probe
Get Pos
MoveTo pos
Send pos with server

Get pos from server
Recreate pos

 * */

public class SerialReader implements Runnable{
	
	public final static char MSG_FLAG 	= '_';
	public final static char CRD_FLAG 	= '`';
	public final static char CRD_DELIM 	= ',';
	public final static char POS_FLAG 	= '|';
	
	final static int THREAD_DELAY 	= 1000;
	final static int CHAR_DELAY		= 200;
	final static int WORD_DELAY		= 1000;
	
	StatePool spool;
	Plotter plotter;
	JTextArea chatLog;

	public SerialReader(StatePool spool, Plotter p, JTextArea chatLog){
		this.spool = spool;
		this.plotter = p;
		this.chatLog = chatLog;
	}

	public void run(){
		while(!spool.exitFlag){
			byte[] buff = new byte[2048];
			int x,y;
	        try{
	        	if(spool.isConnected){
		        	if(spool.senseMode == TchatApp.IN_DEV){
						//Send probe: Request touch position
						spool.out.write(PortConnector.PROBE_POS);
						//Get pos
						x = spool.in.nextInt();
						y = spool.in.nextInt();
						//Move to position on plotter
				        System.out.println(x+" -- "+y);
						this.plotter.moveTo(x,y);
						//Send coordinates over the server if in chat
						if(spool.inChat){
							URLReader.sendMsg(""+x+CRD_DELIM+y+CRD_FLAG,spool.clientUname);
						}
					}
	        	}
	        	if(spool.senseMode == TchatApp.IN_MOUSE){
	        		//TODO
	        	}
				if(spool.inChat){
					/*//Check if the other user is still online
					if(!URLReader.isOnline(spool.clientUname)){
						JOptionPane.showMessageDialog(null, spool.clientUname+" has gone offline!");
						spool.clientUname = "";
						spool.inChat = false;
					}*/

					//Read messages from the server
					String msg = URLReader.readMsg(spool.userName);
					//Parse messages: Find coordinates and message list
					String str = "";
					ArrayList<String> msgList = new ArrayList<String>();
					ArrayList<Integer> X = new ArrayList<Integer>();
					ArrayList<Integer> Y = new ArrayList<Integer>();
					ArrayList<Integer> idList = new ArrayList<Integer>();
					System.out.println(msg+"<=");
					for(int i = 0; i < msg.length(); i++){
						char c = msg.charAt(i);
						switch(c){
							case MSG_FLAG: msgList.add(str);
										   str = "";
										   break;
							case CRD_FLAG: int xp = str.indexOf(CRD_DELIM);
								    	   X.add(Integer.parseInt(str.substring(0,xp)));
										   Y.add(Integer.parseInt(str.substring(xp+1,str.length())));
										   str = "";
										   break;
							case POS_FLAG: int id = Integer.parseInt(str);
										   idList.add(id);
										   str = "";
							 			   break;
							default: str+= c;
									 break;
						}
					}
					//Append received chat messages
					for(int i = 0; i < msgList.size(); i++){
						String recMsg = msgList.get(i);
						this.chatLog.append(spool.clientUname+":"+recMsg+"\n");
						//Translate messages if needed
						if(spool.recMode == TchatApp.TRANSLATE){
							for(int j = 0; j < recMsg.length(); j++){
								char ch = recMsg.charAt(j);
								//TODO: Translate ch to a set of IDs
								//TODO: Activate ID list on plotter
								//TODO: Send ID list to the device for recreation
								switch(ch){
									case ' ':
									case ',':
									case ';':
									case '.': Thread.sleep(WORD_DELAY);
											  break;
									default:  Thread.sleep(CHAR_DELAY);
											  break;
								}
							}
						}
					}
					//Plot received coordinates -- if in SMOOTH mode
					if(spool.recMode == TchatApp.SMOOTH){
						for(int i = 0; i < X.size(); i++)
							plotter.moveTo(X.get(i),Y.get(i));
					}
					//Activate received sensors IDs -- if in RECREATE mode
					else if(spool.recMode == TchatApp.REC){
						for(int i = 0; i < idList.size(); i++){
							System.out.println(idList.get(i));
							plotter.activate(idList.get(i));
							//TODO: Send recreation instruction here
							//if(spool.isConnected){
								//spool.out.write(PortConnector.STIM_POS);
								//spool.out.write()
							//}
						}
					}
				}
				
				//Delay
				Thread.sleep(THREAD_DELAY);
			}
			catch(IOException e){ e.printStackTrace();}
			catch(InterruptedException e) { e.printStackTrace();}
	        catch(Exception e) {e.printStackTrace();}
		}
	}
}
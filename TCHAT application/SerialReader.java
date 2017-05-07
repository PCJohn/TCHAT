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
	final static int CHAR_DELAY		= 1000;
	final static int WORD_DELAY		= 2000;
	
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
				if(spool.inChat){
					//Read messages from the server
					String msg = URLReader.readMsg(spool.userName);
					//Parse messages: Find coordinates and message list
					String str = "";
					ArrayList<String> msgList = new ArrayList<String>();
					ArrayList<Integer> X = new ArrayList<Integer>();
					ArrayList<Integer> Y = new ArrayList<Integer>();
					ArrayList<Integer> idList = new ArrayList<Integer>();
					//System.out.println(msg+"<=");
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
						//System.out.print(spool.recMode);
						//Thread.sleep(500);
						if(spool.recMode == TchatApp.TRANSLATE){
							for(int j = 0; j < recMsg.length(); j++){
								char ch = recMsg.charAt(j);
								//Translate ch to a set of IDs
								ArrayList<Integer> charIdList = Translator.charToId(ch);
								//System.out.println(ch+" -- "+charIdList);
								//Activate ID list on plotter
								plotter.reset();
								for(int k = 0; k < charIdList.size(); k++)
									plotter.activate(charIdList.get(k));
								if(spool.isConnected){
									if(charIdList.size() > 0){
										//Send ID list to the device for recreation
										System.out.println("Sending: "+PortConnector.STIM_POS);
										spool.out.write(PortConnector.STIM_POS);
										String arrStr = "";
										for(int k = 0; k < charIdList.size(); k++){
											spool.out.write(charIdList.get(k));
											arrStr += charIdList.get(k);
											spool.out.write(',');
											arrStr += ',';
										}
										//spool.out.write(charIdList.get(charIdList.size()-1));
										//arrStr += charIdList.get(charIdList.size()-1);
										spool.out.write(PortConnector.SEQ_MODE);
										arrStr += PortConnector.SEQ_MODE;
										System.out.println("Sending: "+arrStr);
									}
								}
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
					if(spool.recMode == TchatApp.REC){
						if(idList.size() > 0){
							plotter.reset();
							for(int i = 0; i < idList.get(i); i++)
								plotter.activate(idList.get(i));
						}
						if(spool.isConnected){
							if(idList.size() > 0){
								System.out.println("Sending: "+PortConnector.STIM_POS);
								spool.out.write(PortConnector.STIM_POS);
								String arrStr = "";
								for(int i = 0; i < idList.size(); i++){
									spool.out.write(idList.get(i));
									arrStr += idList.get(i);
									spool.out.write(',');
									arrStr += ',';
								}
								spool.out.write(PortConnector.SEQ_MODE);
								arrStr += PortConnector.SEQ_MODE;
								System.out.println("Sending: "+arrStr);
							}
						}
					}
				}
				
				//Delay
				Thread.sleep(THREAD_DELAY);
			}
			catch(IOException e){ /*e.printStackTrace();*/}
			catch(InterruptedException e) { /*e.printStackTrace();*/}
	        catch(Exception e) {/*e.printStackTrace();*/}
		}
	}
}
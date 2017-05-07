import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class TchatApp extends JFrame implements ActionListener,WindowListener,ItemListener{

	//Sensing modes
	public final static int IN_MOUSE 		= 0;
	public final static int IN_DEV 			= 1;
	public final static String[] sensModes 	= {"MOUSE","DEVICE"};

	//Recreation modes
	public final static int TRANSLATE 		= 0;
	public final static int REC				= 1;
	public final static int SMOOTH			= 2;
	public final static String[] recModes	= {"TRANSLATE","REC"};
	
	//Variables for every instance of TchatApp
	private String userName		  	= "";
	private Plotter plotter;

	private JPanel chatPanel 	  	= new JPanel();
	private JTextArea chatLog  	  	= new JTextArea();
	private JScrollPane logScroll 	= new JScrollPane(chatLog);
	private JPanel controlBar 	  	= new JPanel();

	private JPanel textBar 		  	= new JPanel();
	private JTextField statusBar  	= new JTextField();
	private JPanel connectBar	  	= new JPanel();
	
	private JPanel modeBar		  		= new JPanel();
	private JComboBox<String> sensMode 	= new JComboBox<String>(sensModes);
	private JComboBox<String> recMode 	= new JComboBox<String>(recModes);

	private JTextField ipField	  = new JTextField(15);
	private JButton connect 	  = new JButton("Connect");
	private JTextField textField  = new JTextField(30);
	private JButton send 		  = new JButton("Send");
	
	private StatePool spool;
	private Thread portConnector;
	private Thread serialReader;
	//private Thread server;

	public void actionPerformed(ActionEvent ae){
		if(ae.getSource().equals(send)){
			if(spool.clientUname.length() > 0){
				String msg = textField.getText().trim();
				if(msg.length() > 0){
					chatLog.append(this.userName+": "+msg+"\n");
					textField.setText("");
				}
				try {
					//System.out.println(msg+SerialReader.MSG_FLAG+spool.clientUname+"=====");
					if(msg.length() > 0)
						URLReader.sendMsg(msg+SerialReader.MSG_FLAG,spool.clientUname);
					ArrayList<Integer> activeIds = new ArrayList<Integer>();
					if(spool.senseMode == TchatApp.IN_MOUSE){
						//Get activated IDs from the mouse
						activeIds = plotter.getActivePoints();
					}
					else if(spool.senseMode == TchatApp.IN_DEV){
						//Send probe: Request touch position buffers
		        		System.out.println("Sending: "+PortConnector.PROBE_POS);
		        		spool.out.write(PortConnector.PROBE_POS);
						//Get pos
						String response = spool.in.next();
						System.out.println("Received: "+response);
						String str = "";
						//ArrayList<Integer> resIdList = new ArrayList<Integer>();
						for(int i = 0; i < response.length(); i++){
							char ch = response.charAt(i);
							if(ch == ','){
								activeIds.add(Integer.parseInt(str.trim()));
								str = "";
							}
							else{
								str += ch;
							}
						}
						//activeIds.add(Integer.parseInt(str.trim()));
						System.out.print("Received values: "+activeIds);
					}
					String idMsg = "";
					for(int i = 0; i < activeIds.size(); i++)
						idMsg = idMsg+activeIds.get(i)+SerialReader.POS_FLAG;
					//System.out.println(idMsg);
					URLReader.sendMsg(idMsg,spool.clientUname);
					plotter.reset();
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		if(ae.getSource().equals(connect)){
			String clientUname = ipField.getText().trim();
			//Check if this user is online
			try{
				if(URLReader.isOnline(clientUname) == true){
					spool.clientUname = clientUname;
					spool.inChat = true;
				}
				else{
					JOptionPane.showMessageDialog(null, clientUname+" is not online!");
				}
			}
			catch(Exception e){/*System.out.println(e.getMessage());*/}
		}
	}

	public void itemStateChanged(ItemEvent ivt){
		spool.senseMode = sensMode.getSelectedIndex();
		spool.recMode = recMode.getSelectedIndex();
	}

	public TchatApp(String userName) throws Exception {
		super("TCHAT -- "+userName+Translator.currentDir());
		this.userName = userName;
		
        //Start pool for shared variables and spawn threads
        spool = new StatePool();
        //Set current username on the shared pool
        spool.userName = this.userName;
		
        //Start a new plotter -- canvas to track the pressure points
        plotter = new Plotter(spool);
        
		//Make chat log area
		Font font1 = new Font("Helvetica",Font.BOLD,12);
		chatLog.setFont(font1);
		chatLog.setEditable(false);
		logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ((DefaultCaret)chatLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        //Add chat log and plotter to the chat panel
        chatPanel.setLayout(new GridLayout(1,2));
        chatPanel.add(plotter);
        chatPanel.add(logScroll);
        
        //Add chat panel to frame
		this.add(chatPanel);

		//Make the control bar
		textBar.add(textField,BorderLayout.WEST);
		send.addActionListener(this);
		textBar.add(send,BorderLayout.EAST);
		modeBar.add(new JLabel("Sensing"));
		modeBar.add(sensMode);
		sensMode.addItemListener(this);
		modeBar.add(new JLabel("Recreation"));
		modeBar.add(recMode);
		recMode.addItemListener(this);
		connectBar.add(ipField,BorderLayout.WEST);
		connect.addActionListener(this);
		connectBar.add(connect,BorderLayout.EAST);
		statusBar.setEditable(false);
		statusBar.setText("Status:");
		controlBar.setLayout(new GridLayout(3,1));
		controlBar.add(connectBar);
		controlBar.add(statusBar);
		controlBar.add(textBar);
		controlBar.add(modeBar);
		
		//Set ENTER key default button
		JRootPane rootPane = SwingUtilities.getRootPane(this); 
		rootPane.setDefaultButton(send);
		
		this.addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Add control bar to frame
		this.add(controlBar,BorderLayout.SOUTH);
		
		//Set window properties
		this.setVisible(true);
        this.setSize(1200,650);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setResizable(false);
        
        //Track status updates on the state pool
        Timer statUpd = new Timer(500,new StatusUpdate(spool,statusBar));
        statUpd.setRepeats(true);
        statUpd.start();
        //Look for hardware connection and start reading if present
        portConnector = new Thread(new PortConnector(spool));
        portConnector.start();
        //Start reading and plotting values when connected
        serialReader = new Thread(new SerialReader(spool,plotter,chatLog));
        serialReader.start();
        //Start server: Wait for connections
        //server = new Thread(new Server(spool)).start();
    }

	public void windowClosing(WindowEvent we){
		try{
			URLReader.quit(userName);
			spool.exitFlag = true;
			spool.inChat = false;
			spool.in.close();
			spool.out.close();
			spool.isConnected = false;
			spool.servIn.close();
			spool.servOut.close();
			System.exit(0);
		}
		catch(Exception e){}
	}
	
	public void windowDeactivated(WindowEvent we){}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}
}
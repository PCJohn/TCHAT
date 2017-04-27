import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class TchatApp extends JFrame implements ActionListener,WindowListener{

	//Sensing modes
	public final static int IN_MOUSE 	= 0;
	public final static int IN_DEV 		= 1;

	//Recreation modes
	public final static int TRANSLATE 	= 0;
	public final static int REC			= 1;
	public final static int SMOOTH		= 2;
	
	//Variables for every instance of TchatApp
	private String userName		  = "";
	private Plotter plotter;

	private JPanel chatPanel 	  = new JPanel();
	private JTextArea chatLog  	  = new JTextArea();
	private JScrollPane logScroll = new JScrollPane(chatLog);
	private JPanel controlBar 	  = new JPanel();

	private JPanel textBar 		  = new JPanel();
	private JTextField statusBar  = new JTextField();
	private JPanel connectBar	  = new JPanel();

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
				chatLog.append(this.userName+": "+msg+"\n");
				textField.setText("");
				try {
					//System.out.println(msg+SerialReader.MSG_FLAG+spool.clientUname+"=====");
					URLReader.sendMsg(msg+SerialReader.MSG_FLAG,spool.clientUname);
					//Get activated IDs from the mouse
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
			catch(Exception e){System.out.println(e.getMessage());}
		}
	}

	public TchatApp(String userName) throws Exception {
		super("TCHAT -- "+userName);
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
		connectBar.add(ipField,BorderLayout.WEST);
		connect.addActionListener(this);
		connectBar.add(connect,BorderLayout.EAST);
		statusBar.setEditable(false);
		statusBar.setText("Status:");
		controlBar.setLayout(new GridLayout(3,1));
		controlBar.add(connectBar);
		controlBar.add(statusBar);
		controlBar.add(textBar);
		
		//Set ENTER key default button
		JRootPane rootPane = SwingUtilities.getRootPane(this); 
		rootPane.setDefaultButton(send);
		
		this.addWindowListener(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Add control bar to frame
		this.add(controlBar,BorderLayout.SOUTH);
		
		this.setVisible(true);
        this.setSize(850,600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
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
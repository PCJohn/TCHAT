import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatusUpdate implements ActionListener {

	private JTextField statusBar;
	private StatePool spool;
	
	public StatusUpdate(StatePool spool, JTextField statusBar){
		this.spool = spool;
		this.statusBar = statusBar;
	}
	
	public void actionPerformed(ActionEvent e){
		String status = "Status: ";
		status += "     ";
		if(this.spool.isConnected)
			status += "TCHAT hardware connected";
		else
			status += "No TCHAT hardware";
		status += "     ";
		if(this.spool.inChat)
			status += "Connected to: "+this.spool.clientUname;
		else
			status += "No chat client";
		this.statusBar.setText(status);
	}
}

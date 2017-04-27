import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;

public class Intro extends JFrame implements ActionListener {
	
	private JPanel img 	  		= new JPanel();
	private JPanel lower  		= new JPanel();
	private JButton start 		= new JButton("Start");
	private JTextField userName = new JTextField(15);
	
	public Intro(){
		//ImageIcon image = new ImageIcon("src/Resources/intro_img2.png");
		ImageIcon image = new ImageIcon(getClass().getResource("/Resources/intro_img2.png"));
		JLabel label = new JLabel("Enter username:",image,JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setVerticalTextPosition(JLabel.BOTTOM);
		img.add(label, BorderLayout.CENTER);
		this.add(img,BorderLayout.CENTER);
		start.addActionListener(this);
		lower.add(userName);
		lower.add(start);
		this.add(lower,BorderLayout.SOUTH);
		this.setVisible(true);
        this.setSize(300,270);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		JRootPane rootPane = SwingUtilities.getRootPane(this); 
		rootPane.setDefaultButton(start);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
	public void actionPerformed(ActionEvent ae){
		try{
			String uName = userName.getText().trim();
			if(uName.length() > 0){
				URLReader.enroll(uName);
				new TchatApp(uName);
				this.dispose();
			}
		}catch(Exception e){System.out.println(e.getMessage());}
	}
	
	public static void main(String args[]){
		Intro intro = new Intro();
	}
}

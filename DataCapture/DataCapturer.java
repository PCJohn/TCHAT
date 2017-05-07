import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DataCapturer extends JFrame implements ActionListener{

	JPanel controlBar = new JPanel();
	JTextField label = new JTextField(15);
	JButton save = new JButton("Save");
	JButton seq = new JButton("Seq");
	CapturePanel cp = new CapturePanel();
	private String fname = "banszl.csv";
	private PrintWriter pw;

	public DataCapturer(){
		super("Data Capture");
		add(cp,BorderLayout.CENTER);

		controlBar.setLayout(new GridLayout(1,3));
		controlBar.add(label);
		controlBar.add(save);
		controlBar.add(seq);
		this.add(controlBar,BorderLayout.SOUTH);
		
		this.setSize(425,523);
		this.setResizable(false);
		this.setVisible(true);
		
		try {
			pw = new PrintWriter(fname);
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == save){
			ArrayList<Point> points = cp.getPoints();
			for(int i = 0; i < points.size(); i++){
				Point p = points.get(i);
				pw.println(p.x+","+p.y);
			}
		}
	}
	
	public static void main(String [] args){
		DataCapturer dc = new DataCapturer();
	}
}

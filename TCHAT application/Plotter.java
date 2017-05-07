import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Plotter extends JPanel implements MouseListener {
	
	public static final int DIAMETER = 20;
	
	private ArrayList<Point> strokeHistory;
	private ArrayList<Integer> idList;
	private Image background;
	private StatePool spool;
	private boolean backgroundSet = false;
	
	private static Color strokeColor 		= Color.BLACK;
	private static Color backgroundColor 	= Color.WHITE;
	private static Color ovalFill			= new Color(255,0,0,127);
	private static int strokeLen			= 30;

	public Plotter(StatePool spool){
		super();
		this.spool = spool;
		this.setBackground(this.backgroundColor);
		this.strokeHistory = new ArrayList<Point>();
		this.idList = new ArrayList<Integer>();
	    //this.background = new ImageIcon("src/Resources/hand.jpeg").getImage();
		this.background = new ImageIcon(getClass().getResource("/Resources/hand_sense.jpeg")).getImage();
	    Dimension size = new Dimension(50,50);
	    setPreferredSize(size);
	    setLayout(null);
	    this.addMouseListener(this);
	}

	public void setBackground(Graphics g){
		g.drawImage(this.background, 20, 20, this.getWidth()-50, this.getHeight()-50, null);
	}
	
	public void moveTo(int newX, int newY){
		strokeHistory.add(new Point(newX,newY));
		if(strokeHistory.size() > strokeLen)
			strokeHistory.remove(0);

		Graphics g = this.getGraphics();
		setBackground(g);
		((Graphics2D)g).setStroke(new BasicStroke(3.0f));
		g.setColor(this.strokeColor);
		if(strokeHistory.size() > 1){
			for(int i = 0; i < strokeHistory.size()-1; i++){
				Point p1 = strokeHistory.get(i);
				int x1 = (int)(p1.x*this.getWidth());
				int y1 = (int)(p1.y*this.getHeight());
				Point p2 = strokeHistory.get(i+1);
				int x2 = (int)(p2.x*this.getWidth());
				int y2 = (int)(p2.y*this.getHeight());
				g.drawLine(x1,y1,x2,y2);
			}
		}
	}

	public void reset(){
		strokeHistory.clear();
		idList.clear();
		Graphics g = this.getGraphics();
		setBackground(g);
		backgroundSet = true;
	}
	
	public void activate(int id){
		Graphics g = this.getGraphics();
		if(!backgroundSet){
			setBackground(g);
			backgroundSet = true;
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(ovalFill);
		Point idPoint = Translator.idToVec(id);
		int x = (int)(idPoint.x*this.getWidth());
		int y = (int)(idPoint.y*this.getHeight());
		//System.out.println(x+" +++++ "+y);
		g2d.fillOval(x-DIAMETER/2, y-DIAMETER/2, DIAMETER, DIAMETER);
	}
	
	public ArrayList<Integer> getActivePoints(){
		return idList;
	}

	public void mouseClicked(MouseEvent e) {
		if(spool.senseMode == TchatApp.IN_MOUSE){
			int x = e.getX();
			int y = e.getY();
			//System.out.println(((float)x)/this.getWidth()+" == "+((float)y)/this.getHeight());
			//Get ID from the X-Y coordinates
			int id = Translator.vecToId(x/((double)this.getWidth()),y/((double)this.getHeight()));
			//Append the nearest ID to the idList
			idList.add(id);
			//Activate this point -- The nearest ID will be activated for the other user
			Graphics g = this.getGraphics();
			if(!backgroundSet){
				setBackground(g);
				backgroundSet = true;
			}
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(ovalFill);
			g2d.fillOval(x-DIAMETER/2, y-DIAMETER/2, DIAMETER, DIAMETER);
		}
	}

	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}
}

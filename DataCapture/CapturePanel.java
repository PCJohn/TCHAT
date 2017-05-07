import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class CapturePanel extends JPanel implements MouseListener{

	private Image background;
	private boolean backgroundSet = false;
	private Color ovalFill = new Color(255,0,0,127);
	private int DIAMETER = 20;
	private ArrayList<Point> points;
	
	public CapturePanel(){
		super();
		this.background = new ImageIcon(getClass().getResource("/Resources/hand.jpeg")).getImage();
		this.addMouseListener(this);
	}
	
	public void setBackground(Graphics g){
		g.drawImage(this.background, 20, 20, this.getWidth()-50, this.getHeight()-50, null);
		backgroundSet = true;
	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println(x+" -- "+y);
		points.add(new Point(x,y));
		Graphics g = this.getGraphics();
		if(!backgroundSet){
			setBackground(g);
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(ovalFill);
		g2d.fillOval(x-DIAMETER/2, y-DIAMETER/2, DIAMETER, DIAMETER);
	}
	
	public ArrayList<Point> getPoints(){
		return points;
	}
	
	public void reset(){
		Graphics g = this.getGraphics();
		setBackground(g);
		this.backgroundSet = true;
		points.clear();
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}

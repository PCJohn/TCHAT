import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Translator {
	
	final static String POINT_FILE = "/home/prithvi/py/tchat/points.csv";
	final static String VEC_TO_ID_CMD = "python /home/prithvi/py/tchat/translator.py vec2id";
	final static String ID_TO_VEC_CMD = "python /home/prithvi/py/tchat/translator.py id2vec";
	
	//Map Cartesian coordinates to sensors IDs
	public static int vecToId(int x, int y){
		String cmd = VEC_TO_ID_CMD+" "+POINT_FILE+" "+x+" "+y;
		int id = -1;
		try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String line;
		    while((line = br.readLine()) != null){
		    	id = Integer.parseInt(line.trim());
		    }
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ System.out.println(e.getMessage()); }
		return id;
	}
	
	//Map pressure point ID to Cartesian coordinates
	public static Point idToVec(int id){
		String cmd = ID_TO_VEC_CMD+" "+POINT_FILE+" "+id;
		int x = 0, y = 0;
		try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String out = br.readLine().trim();
		    int cpos = out.indexOf(',');
		    x = Integer.parseInt(out.substring(0,cpos));
		    y = Integer.parseInt(out.substring(cpos+1,out.length()));
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ System.out.println(e.getMessage()); }
		return new Point(x,y);
	}
	
	public static ArrayList<Integer> charToVec(){
		ArrayList<Integer> vec = new ArrayList<Integer>();
		return vec;
	}
}

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Scanner;

public class Translator {
	
	final static String POINT_FILE = currentDir()+File.separatorChar+"points.csv";
	final static String CHAR_FILE = currentDir()+File.separator+"banszl.csv";
	final static String VEC_TO_ID_CMD = "python "+currentDir()+File.separatorChar+"translator.py vec2id";
	final static String ID_TO_VEC_CMD = "python "+currentDir()+File.separatorChar+"translator.py id2vec";
	final static String CHAR_TO_ID_CMD = "python "+currentDir()+File.separatorChar+"translator.py char2id";
	final static String ID_TO_CHAR_CMD = "python "+currentDir()+File.separatorChar+"translator.py id2char";

	//Map Cartesian coordinates to sensors IDs
	public static int vecToId(double x, double y){
		String cmd = VEC_TO_ID_CMD+" "+POINT_FILE+" "+CHAR_FILE+" "+x+" "+y;
    	System.out.println("Running: "+cmd);
		int id = -1;
		try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String line;
		    while((line = br.readLine()) != null){
		    	id = Integer.parseInt(line.trim());
		    	//System.out.println(id+" __ "+idToVec(id));
		    }
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ System.out.println(e.getMessage()); }
		return id;
	}

	//Map pressure point ID to Cartesian coordinates
	public static Point idToVec(int id){
		String cmd = ID_TO_VEC_CMD+" "+POINT_FILE+" "+CHAR_FILE+" "+id;
		System.out.println("Running: "+cmd);
		double x = 0, y = 0;
		try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String out = br.readLine().trim();
		    int cpos = out.indexOf(',');
		    x = Double.parseDouble(out.substring(0,cpos));
		    y = Double.parseDouble(out.substring(cpos+1,out.length()));
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ System.out.println(e.getMessage()); }
		return new Point(x,y);
	}

	//Translate a character to a list of IDs
	public static ArrayList<Integer> charToId(char ch){
		ArrayList<Integer> vec = new ArrayList<Integer>();
		String cmd = CHAR_TO_ID_CMD+" "+POINT_FILE+" "+CHAR_FILE+" "+ch;
		System.out.println("Running: "+cmd);
		try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String out = br.readLine().trim();
		    String str = "";
		    for(int i = 0; i < out.length(); i++){
		    	if((out.charAt(i) == ',') || (i == (out.length()-1))){
		    		vec.add((int)Float.parseFloat(str));
		    		str = "";
		    	}
		    	else
		    		str += out.charAt(i);
		    }
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ /*System.out.println(e.getMessage());*/ }		
		return vec;
	}

	//Translate a list of IDs to an English letter
	public static char idToChar(ArrayList<Integer> idList){
		String idListParam = "";
		for(int i = 0; i < idList.size()-1; i++)
			idListParam += idList.get(i).toString()+",";
		idListParam += idList.get(idList.size()-1);
		String cmd = ID_TO_CHAR_CMD+" "+POINT_FILE+" "+CHAR_FILE+" "+idListParam;
		System.out.println("Running: "+cmd);
		char c = '.';
	    try{
			Process p = Runtime.getRuntime().exec(cmd);
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String out = br.readLine().trim();
		    c = out.charAt(0);
		    p.waitFor();
		    p.destroy();
		}
		catch(Exception e){ System.out.println(e.getMessage()); }		
		return c;
	}

	public static String currentDir(){
		try {
			String path = URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getPath(), "UTF-8");
			return new File(path).getAbsolutePath();
		} catch (Exception e){ return ""; }
	}
	
	/*public static void main(String args[]){
		int[] arr = new int[]{8,9,18,6,7};
		ArrayList<Integer> v = new ArrayList<Integer>();
		for(int i = 0; i < 5; i++)
			v.add(arr[i]);
		System.out.println(idToChar(v));
		//System.out.println(charToId('c'));
	}*/
}
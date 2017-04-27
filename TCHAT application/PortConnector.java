import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class PortConnector implements Runnable{
    
	//Instructions to the board
	final static char PROBE_DEV = '#'; 	//Probe -- check if this is our device
	final static char DEV_RES = '%';	//Expected handshake response from device
	final static char PROBE_POS = 'p'; 	//Probe position -- request touch position
	final static char STIM_POS = 's';	//Instruction to send recreation position

	//Parameters for transmission to the board
	final static int TIME_OUT 	= 2000;
	final static int BAUD_RATE 	= 9600;
	
	private StatePool spool;
	
	public PortConnector(StatePool spool){
		this.spool = spool;
	}
	
    ArrayList<String> getSerialPorts(){
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> availablePorts = new ArrayList<String>();
        while(portEnum.hasMoreElements()){
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if(getPortTypeName(portIdentifier.getPortType()).trim().equals("Serial")){
            	availablePorts.add(portIdentifier.getName());
            }
        }
        return availablePorts;
    }
    
    static String getPortTypeName(int portType){
        switch(portType){
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    public void run(){
    	while(!spool.exitFlag){
	    	if(!spool.isConnected){
	    		ArrayList<String> availablePorts = getSerialPorts();
		    	for(String portName : availablePorts){
		    		try{
		    			CommPortIdentifier pi = CommPortIdentifier.getPortIdentifier(portName);
		    			if(pi.isCurrentlyOwned())
		    	            continue;
		    	        else{
		    	            CommPort commPort = pi.open("SerialComm",TIME_OUT); //Time out period
		    	            SerialPort serialPort = (SerialPort) commPort;
		    	            serialPort.setSerialPortParams(BAUD_RATE,
		    	                                           SerialPort.DATABITS_8,
		    	                                           SerialPort.STOPBITS_1,
		    	                                           SerialPort.PARITY_NONE);
		    	            
		    	            this.spool.in = new Scanner(serialPort.getInputStream());
		    	            this.spool.out = serialPort.getOutputStream();
		    	            
		    	            //Initial handshake -- Check if our device
		    	            this.spool.out.write(PROBE_DEV);
		    	            System.out.println("Here1");
		    	            char res = this.spool.in.next().charAt(0);
		    	            System.out.println("HERE_________");
		    	            if(res == DEV_RES){
		    	            	System.out.println("Connection established at port "+portName);
		    	            	//Set state to show that handshake successful
		    	            	this.spool.isConnected = true;
		    	            }
		    	            else
		    	            	continue;
		    	        }
		    		}
		    		catch(NoSuchPortException nspe){
		    			//TODO
		    		}
		    		catch(UnsupportedCommOperationException ucoe){
		    			//TODO
		    		}
		    		catch(IOException ioe){
		    			//TODO
		    		}
		    		catch(PortInUseException piue){
		    			//TODO
		    		}
		    	}
	    	}
    	}
    }
}

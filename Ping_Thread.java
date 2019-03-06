package imapClient;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This is a simple stream that every 20 seconds ping the specified 
 * IP address and displays the result in the form of a green or red 
 * light bulb on the main form of the program.
 */

public class Ping_Thread extends Thread{
	
	public boolean isCancelled;
	String pingSet = "";
	String pingAddress = "";
	Icon falseIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/pingFalse.png")));
	Icon greenIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/pingGreen.png")));	
	Icon redIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/pingRed.png")));
		
	public void cancel() {
		isCancelled = true;
		Form.label_ping.setText("");
		Form.label_ping.setIcon(null);
		this.interrupt();
	}

	public void run() {
		isCancelled = false;		
		while(!isCancelled) {
			
			//get specified IP address
			File file = new File(Form.propFile);
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(file), Charset.forName("UTF-8")
								)
						);
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("pingSet ")) pingSet = line.substring(line.indexOf("pingSet")+8);
					else if (line.startsWith("pingAddress ")) pingAddress = line.substring(line.indexOf("pingAddress")+12);
				}
				reader.close();
			} catch (Exception ex) {
				addExToLogs(ex);
			}
			if (isCancelled) break;
			
			//three attempts to ping ip address
			if (pingSet.equalsIgnoreCase("true")) {
				Form.label_ping.setText("Connection checking");
				Form.label_ping.setIcon(falseIcon);
				for (int i=0; i<3; i++) {
					Boolean rezult = ping(pingAddress);
					if (isCancelled) return;
					else {
						if (rezult) {
							Form.label_ping.setIcon(greenIcon);
							break;
						} else Form.label_ping.setIcon(redIcon);
					}					
				}
				try {
					Thread.sleep(20000);
				} catch (InterruptedException ex) {
					if (ex.getMessage().equals("sleep interrupted")) {
						break;
					}
					else addExToLogs(ex);
				}			
			} else this.cancel();
		}		
	}

	private Boolean ping(String pingAddress) {
		try {
	    	InetAddress inet = InetAddress.getByName(pingAddress);	    	
	        if (inet.isReachable(5000)) return true;
	    } catch (Exception ex) {
	    }
		return false;
	}
	
	private void addExToLogs(Exception ex) {
		Form.saveExceptions(ex, "imapClient.Ping_Thread");
		String exception = ex.getClass().getName();
		String exMsg = ex.getMessage();
		if (exMsg != null) exception += ": " + exMsg;
		else exception += " at " + ex.getStackTrace()[0].toString();
		Throwable cause = ex.getCause();
		if (cause != null) exception = exception + " Caused by: " + cause.getMessage();
		Form.addLogs("ERROR [imapClient.Ping_Thread]: "+exception+"\r\n", -1);	
	}
}
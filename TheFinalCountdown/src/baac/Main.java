package baac;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import chat.LobbyChat;

//Learning how to use Java to unzip a file:
//http://stackoverflow.com/questions/9324933/what-is-a-good-java-library-to-zip-unzip-files
/**
 * The Main class starts the client
 * 
 * @author reuintern
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (!(new File("src/voce/gram/every.gram").isFile())){
			if (!(new File("./voce.zip").isFile())){
				downloadFile();
			}
			extractFolder();
			try {
				Files.deleteIfExists(new File("./voce.zip").toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Thread client = new Thread(new BAAC());
		client.start();

	}
/**
 * This method downloads the voce zip file
 */
	public static void downloadFile(){
		URL website = null;
		try {
			website = new URL("https://solidary-obligation.000webhostapp.com/voce.zip");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ReadableByteChannel rbc = null;
		try {
			rbc = Channels.newChannel(website.openStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("./voce.zip");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * This method extracts the the voce zip file to the current directory
	 */
	private static  void extractFolder() 
	{
	    try
	    {
	        int BUFFER = 2048;
	        File file = new File("./voce.zip");

	        ZipFile zip = new ZipFile(file);
	        String newPath = "./src";

	        new File(newPath).mkdir();
	        Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

	        
	        while (zipFileEntries.hasMoreElements())
	        {
	            
	            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	            String currentEntry = entry.getName();

	            File destFile = new File(newPath, currentEntry);
	           
	            File destinationParent = destFile.getParentFile();

	      
	            destinationParent.mkdirs();

	            if (!entry.isDirectory())
	            {
	                BufferedInputStream is = new BufferedInputStream(zip
	                .getInputStream(entry));
	                int currentByte;
	          
	                byte data[] = new byte[BUFFER];

	     
	                FileOutputStream fos = new FileOutputStream(destFile);
	                BufferedOutputStream dest = new BufferedOutputStream(fos,
	                BUFFER);

	    
	                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                    dest.write(data, 0, currentByte);
	                }
	                dest.flush();
	                dest.close();
	                is.close();
	            }


	        }
	    }
	    catch (Exception e) 
	    {
	       
	    }

	}

}

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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import chat.LobbyChat;


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
		//downloadFile();
		//extractFolder();
		Thread client = new Thread(new BAAC());
		client.start();

	}

	public static void downloadFile(){
		URL website = null;
		try {
			website = new URL("http://localhost/voce.zip");
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

	        // Process each entry
	        while (zipFileEntries.hasMoreElements())
	        {
	            // grab a zip file entry
	            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	            String currentEntry = entry.getName();

	            File destFile = new File(newPath, currentEntry);
	            //destFile = new File(newPath, destFile.getName());
	            File destinationParent = destFile.getParentFile();

	            // create the parent directory structure if needed
	            destinationParent.mkdirs();

	            if (!entry.isDirectory())
	            {
	                BufferedInputStream is = new BufferedInputStream(zip
	                .getInputStream(entry));
	                int currentByte;
	                // establish buffer for writing file
	                byte data[] = new byte[BUFFER];

	                // write the current file to disk
	                FileOutputStream fos = new FileOutputStream(destFile);
	                BufferedOutputStream dest = new BufferedOutputStream(fos,
	                BUFFER);

	                // read and write until last byte is encountered
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

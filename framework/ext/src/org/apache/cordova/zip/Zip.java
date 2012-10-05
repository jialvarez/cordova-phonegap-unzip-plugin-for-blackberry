package org.apache.cordova.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.file.File;

import org.apache.cordova.json4j.JSONObject;
import org.apache.cordova.json4j.JSONArray;
import org.apache.cordova.json4j.JSONException;

import net.sf.zipme.ZipException;
import net.sf.zipme.ZipInputStream;
import net.sf.zipme.ZipArchive;
import net.sf.zipme.ZipEntry;

public class Zip extends Plugin {

	private static final String LOG_TAG = "Zip";
	private static final String DIRECTORY_SEPARATOR = "/";
	//private static final int BUFFER_SIZE = 2048;

	//private List<String> processedEntities = new ArrayList<String>();

	/**
	* Executes the request and returns PluginResult.
	*
	* @param action		The action to execute.
	* @param args		JSONArry of arguments for the plugin.
	* @param callbackId	The callback id used when calling back into JavaScript.
	* @return		A PluginResult object with a status and message.
	*/
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";

		try {
			// Parse common args
			String source = args.getString(0);

			if (action.equals("info")) {

				JSONObject zipInfo;
				
				zipInfo = this.info(source);

				return new PluginResult(status, zipInfo.toString());

			} else if (action.equals("compress")) {

				String target = args.getString(1);

				return new PluginResult(status, result);
				/*
				if (this.uncompress(source, target, callbackId)) {
					return new PluginResult(status, result);
				} else {
					return new PluginResult(PluginResult.Status.ERROR, result);
				}
				*/
			} else if (action.equals("uncompress")) {

				String target = args.getString(1);

				JSONObject ret = this.uncompress(source, target, callbackId);
				ret.put("completed", true);

				// Purge action only data structures.
				//this.processedEntities.clear();

				return new PluginResult(PluginResult.Status.OK, ret.toString());
				/*
				if (this.uncompress(source, target, callbackId)) {
					return new PluginResult(status, result);
				} else {
					return new PluginResult(PluginResult.Status.ERROR, result);
				}
				*/
			}
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
		} catch (IOException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());			
		} catch (InterruptedException e) {
			return new PluginResult(PluginResult.Status.IO_EXCEPTION, e.getMessage());
		}
	}

	/**
	* Read the compressed file and returns InputStream obtained.
	*
	* @param zipFile		Zip/Rar filename to read.
	* @return				InputStream containing zip file data.
	*/
    public InputStream readRarFile(String zipFile) 
    {
    	InputStream is = null;
    	
        try 
        {
        	Connection c = Connector.open(zipFile, Connector.READ);
            FileConnection fc = (FileConnection) c;
            is = fc.openInputStream();
        } 
        catch (Exception e) 
        {
            System.out.println(LOG_TAG + " - Error in reding the File");
        }
        
        return is;
    }
    
	/**
	* Write file or dir in target path.
	*
	* @param zis		Zip/Rar filename to read.
	* @param path		Path of the current item to write.
	* @param b			Array of bytes to write.
	* @param target		Parent path to write the current item.
	* @return			True or false if file/dir could be written.
	*/
    public boolean writeFile(ZipInputStream zis, String path, byte[] b, String target) 
    {
        String targetToWrite = target + path;
        FileConnection conn = null;
        
		try 
		{
            System.out.println(LOG_TAG + " - Processing file: " + targetToWrite);
            
            // Check if a filename we want to process is a DIRECTORY or a FILE
            if (path.charAt(path.length() - 1) == '/')
            {
            	// Process DIRECTORY
                System.out.println(LOG_TAG + " - dirname: " + targetToWrite);
                String dirsMade = "";

                if (!dirsMade.equals(targetToWrite)) 
                {
                    conn = (FileConnection) Connector.open(targetToWrite);
                    
                    // If it already exists as a dir, don't do anything
                    if (!(conn.exists() && conn.isDirectory())) 
                    {
                    	System.out.println(LOG_TAG + " - Creating dir: " + targetToWrite);
                    	conn.mkdir();
                    	System.out.println(LOG_TAG + " - Created directory: " + targetToWrite);
                        
                    	dirsMade = targetToWrite;
                    }

                    conn.close();
                }
            }
            else
            {
            	// Process FILES
            	conn = (FileConnection) Connector.open(targetToWrite);

        		if (!conn.exists() && !conn.isDirectory()) 
        		{
                    conn.create();
                } 
        		else 
        		{
                    conn.truncate(0);
                }

        		// Create file knowing size of array of bytes
                OutputStream os = conn.openOutputStream();
                
                int count = 0;
        		while ((count = zis.read(b, 0, b.length)) != -1) 
        		{
                    os.write(b, 0, count);
                }
        		
                os.flush();
            	conn.close();
            }
        } 
		catch (IOException e) 
		{
            // error
        	System.out.println(LOG_TAG + " - Exception creating dir: " + e.getMessage());
        } 
		catch (SecurityException e) 
		{
            // no permission to create/write
        	System.out.println(LOG_TAG + " - Exception c/w creating dir: " + e.getMessage());
        }
        catch (Exception e) 
        {
            System.out.println(LOG_TAG + " - Generic exception: " + e.toString());
            return false;
        } 
        
		return true;
    }

	/**
	* Identifies if action to be executed returns a value and should be run synchronously.
	*
	* @param action		The action to execute
	* @return		T=returns value
	*/
	public boolean isSynch(String action) {
		if (action.equals("info")) {
			return false;
		}
		else if (action.equals("compress")) {
			return true;
		}
		else if (action.equals("uncompress")) {
			return false;
		}

		return false;
	}

	/**
	* Info.
	*
	* @param source		The action to execute.
	* @return		A ZipFileEntry structure.
	*/
	private JSONObject info(String source) throws JSONException, ZipException, IOException {

		/*source = FileUtils.stripFileProtocol(source);

		File sourceFile = new File(source);
		ZipFile zipFile = new ZipFile(sourceFile);
		
		ZipFile a = new ZipFile(source);

		JSONObject zipInfo = new JSONObject();

		// Using FileUtils::getEntry to create an file info structure.
		zipInfo = fu.getEntry(sourceFile);

		zipInfo.put("entries", zipFile.size());
		
		return zipInfo;*/
		
		//int entries = this.zip("http://www.litio.org/tmp/test.zip");
		JSONObject zipInfo = new JSONObject();
		zipInfo.put("entries", 103);
		return zipInfo;
	}

	/**
	* Compress.
	*
	* @param source		The action to execute.
	* @param elements	An array of element to compress.
	* @return		True if all went fine, false otherwise.
	*/
	private boolean compress(String source, String callbackId) {

		// TODO: Implement it.
		return false;
	}

	/**
	* Uncompress.
	*
	* @param source		Sourcezip file location. (Local or remote)
	* @param destination	Directory destination.
	* @return		True.
	*/
	private JSONObject uncompress(String source, String target, String callbackId) throws JSONException, InterruptedException
	{
		// 'target' would be 'file:///SDCard/test'
        int entries = 0;
        
        try 
        {
        	// Get number of entries
            /*InputStream is = readRarFile(source);
        	ZipArchive za = new ZipArchive(is);
        	System.out.println("Zip file size: " + za.size());
        	is.close();*/
            
    		//Prepare for Rar or Zip File extraction
            InputStream is = readRarFile(source);
            ZipInputStream zis = new ZipInputStream(is);
            //is.close();
            
            ZipEntry ze;
            
            while ((ze = zis.getNextEntry()) != null) 
            {
                int i = (int) ze.getSize();
                byte[] b = new byte[i];
                
                // Write File or Dir, calling our function
                writeFile(zis, ze.getName(), b, target);
                zis.closeEntry();
                entries++;
            }
            
            zis.close();
        } 
        catch (Exception e) 
        {
          e.printStackTrace();
        }
		
		
		JSONObject lastMsg = new JSONObject();
		lastMsg.put("entries", entries);

		System.out.println(LOG_TAG + " - Processed entries: " + entries);
		
		return lastMsg;
	}

	private JSONObject publish(File file, int totalEntities, String callbackId) throws JSONException, InterruptedException
	{
		/*JSONObject msg = new JSONObject();
        boolean completed = totalEntities == this.processedEntities.size();

		// Using FileUtils::getEntry to create an file info structure.
		FileUtils fu = new FileUtils();
		msg = fu.getEntry(file);

		// Add new params for progress calculation.
		msg.put("completed", completed);
		msg.put("progress", this.processedEntities.size());

		PluginResult result = new PluginResult(PluginResult.Status.OK, msg.toString());
		result.setKeepCallback(true);

        // Avoid to send the message "uncompress completed" twice.
        // This message is sended in the execute method.
        if (!completed) {
		    success(result, callbackId);
        }

		Thread.sleep(100);

		return msg;*/
		JSONObject lastMsg = new JSONObject();
		lastMsg.put("yeah", "yeah");
		return lastMsg;
	}
}
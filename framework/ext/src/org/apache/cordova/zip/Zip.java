package org.apache.cordova.zip;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.file.File;

import org.apache.cordova.util.FileUtils;

import org.apache.cordova.json4j.JSONObject;
import org.apache.cordova.json4j.JSONArray;
import org.apache.cordova.json4j.JSONException;

import net.rim.device.api.ui.UiApplication;
import net.sf.zipme.ZipArchive;
import net.sf.zipme.ZipException;
import net.sf.zipme.ZipInputStream;
import net.sf.zipme.ZipEntry;

public class Zip extends Plugin {

	private static final String LOG_TAG = "Zip";
	private static final String DIRECTORY_SEPARATOR = "/";
	private static final int BUFFER_SIZE = 2048;
	private String url;

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

//Fetch Zip File
    public InputStream readRarFile(String url) {
        javax.microedition.io.Connection c = null;
        java.io.InputStream is = null;
        try {
            c = javax.microedition.io.Connector.open(url, javax.microedition.io.Connector.READ);
            javax.microedition.io.file.FileConnection fc = (javax.microedition.io.file.FileConnection) c;
            is = fc.openInputStream();
         } catch (Exception e) {
            System.out.println("Error in reding the File");
       }
        return is;
    }
    
    //Load  Zip or Rar File after Extraction
    public boolean writeFile(ZipInputStream zis, String path, byte[] b, String URL2) {
    	URL2 = "file:///SDCard/";
        try {
            System.out.println("filename:" + path);
         
            String url = URL2 + path;
            System.out.println("url:" + url);
            FileConnection conn = null;
			try {
                System.out.println("Enter the writing process");
                conn = (FileConnection) javax.microedition.io.Connector.open(url);
                // Write the File
                if (url.startsWith("/")) {
                    boolean warnedMkDir = false;
					if (!warnedMkDir) {
                        System.out.println("Ignoring absolute paths");
                    }
                    warnedMkDir = true;
                    url = url.substring(1);
                }
                // if a directory, just return. We mkdir for every file,
                // since some widely-used Zip creators don't put out
                // any directory entries, or put them in the wrong place.
                /*if (url.endsWith("/")) {
                    return false;
                }*/
                // Else must be a file; open the file for output
                // Get the directory part.
                System.out.println("Processing file: " + url);
                int ix = url.lastIndexOf('/');
                System.out.println("Processing parts of file:" + ix);

                if (ix > 0) {
                    System.out.println("Entering ix...");

                    String dirName = url.substring(0, ix);
                    
                    System.out.println("dirname: " + dirName);

                    String dirsMade = "";
					//    if (!dirsMade.contains(dirName))
                    if (!dirsMade.equals(dirName)) {
                        FileConnection conn1 = (FileConnection) javax.microedition.io.Connector.open(dirName+"/");
                       // If it already exists as a dir, don't do anything
                        if (!(conn1.exists() && conn1.isDirectory())) {
                        	System.out.println("Creating dir: " + dirName);
                            conn1.mkdir();
                            //createFiles(zis, conn1, b);
                            // Try to create the directory, warn if it fails
                            System.out.println("Create Directory: " + dirName);
                            if (!(conn1.isDirectory())) {
                                System.err.println("Warning: unable to mkdir " + dirName);
                            }
                            dirsMade = dirName;
                        }
                    }
               }
                createFiles(zis, conn, b);
            } catch (IOException e) {
                // error
            	System.out.println("Exception creating dir: " + e.getMessage());
            } catch (SecurityException e) {
                // no permission to create/write
            	System.out.println("Exception c/w creating dir: " + e.getMessage());
            }
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        } 
    }
	
    private void createFiles(ZipInputStream zis, FileConnection conn, byte[] b) throws IOException {
		if (!conn.exists() && !conn.isDirectory()) {
            conn.create();
        } else {
            conn.truncate(0);
        }
        OutputStream os = conn.openOutputStream();
        //Write Files
        int count;
		while ((count = zis.read(b, 0, b.length)) != -1) {
            os.write(b, 0, count);
        }
        os.flush();
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
		/*System.out.println("uncompress: " + source + " to " + target);

		List<String> extractedEntities = new ArrayList<String>();

		source = FileUtils.stripFileProtocol(source);
		target = FileUtils.stripFileProtocol(target);
		System.out.println( "stripped source: " + source);
		System.out.println( "stripped target: " + target);

		File sourceFile = new File(source);
		File targetFile = new File(target);


		ZipFile zipFile = new ZipFile(sourceFile);
		Enumeration zipEntities = zipFile.entries();
		ArrayList zipList = Collections.list(zipEntities);
        int totalEntities = zipList.size();

		String targetPath = "";

		JSONObject lastMsg = new JSONObject();

		// TODO: Handle possible cancelation.
		Iterator it = zipList.iterator();
		while (it.hasNext()) {

			ZipEntry entity = (ZipEntry) it.next();
			System.out.println( "Current entity: " + entity.getName());

			targetPath = targetFile.getAbsolutePath() + DIRECTORY_SEPARATOR + entity.getName();
			File currentTarget = new File(targetPath);
			File currentTargetParent = currentTarget.getParentFile();
			currentTargetParent.mkdirs();

			if (!entity.isDirectory()) {

				BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entity));
				FileOutputStream fos = new FileOutputStream(currentTarget);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

				int currentByte;
				byte data[] = new byte[BUFFER_SIZE];
				while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();

				this.processedEntities.add(currentTarget.getAbsolutePath());
				lastMsg = this.publish(currentTarget, totalEntities, callbackId);
			}

		}

		return lastMsg;*/
		
		
        this.url=source;
        FileConnection con = null;
        String root = null;
		//Create RAR  Directory
        try {
            //String URL = "http://www.litio.org/tmp/test.zip";
        	String URL = "file:///SDCard/";
            String rootDir = "file:///SDCard/";
            //  String  rootDir = "file:///e:/Other/rar/epub.txt";
            System.out.println("Create directory:Entry" + URL);
            String name = "/test/";
            URL.endsWith("/");
            
            root = URL + name;
            System.out.println("Directory to create: " + root);
            
            con = (FileConnection) javax.microedition.io.Connector.open(root);
            FileConnection con1 = (FileConnection) javax.microedition.io.Connector.open(rootDir);
            
            if (!(con.exists() && con.isDirectory())) 
            {
                con.mkdir();
                System.out.println("Create Directory");
                rootDir = con.getName();
                // Try to create the directory, warn if it fails
                System.out.println("Creating Directory: " + rootDir);
            } 
            else 
            {
                System.out.println("Always Available");
            }
            
            if (!(con1.exists() && con1.isDirectory())) 
            {
                con1.create();
            }
            
            root = URL + name;
            System.out.println("Root: " + root);
        } 
        catch (Exception e) 
        {
        	System.out.println("Excepcion creando directorios");
        }
        
        System.out.println("Root: " + root);
        
        int entries = 0;
        
		//Rar or Zip File  Extraction
        try {
            InputStream is = readRarFile(this.url);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;
            
            while ((ze = zis.getNextEntry()) != null) {
                int i = (int) ze.getSize();
                byte[] b = new byte[i];
                //Zip File Readinn Process
                writeFile(zis, ze.getName(), b, root);
                zis.closeEntry();
                entries++;
            }
            zis.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
		
		
		JSONObject lastMsg = new JSONObject();
		lastMsg.put("entries", entries);
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
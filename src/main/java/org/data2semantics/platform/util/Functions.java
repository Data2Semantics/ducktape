package org.data2semantics.platform.util;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.ModuleInstance;

public class Functions
{
	/** 
	 * toString with null check
	 * @param in
	 * @return
	 */
	public static String toString(Object in)
	{
		if(in == null)
			return "null";
		return in.toString();
	}
	
	/** 
	 * equals with null check
	 * @param in
	 * @return
	 */
	public static boolean equals(Object first, Object second)
	{
		if(first == null)
			return second == null;
		
		return first.equals(second);
	}
	
	/**
	 * Produces a random string of the given length, to serve as a unique identifier
	 * 
	 * @param stringLength
	 * @returns {String}
	 */
	public static String randomString(int length) 
	{
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		StringBuilder randomString = new StringBuilder();
		for (int i : Series.series(length)) 
		{
			int rnum = Global.random().nextInt(chars.length()+i);
			randomString.append(chars.charAt(rnum));
		}
		
		return randomString.toString();
	}
	
	public static boolean setCurrentDirectory(File directory)
	{
        boolean result = false;  // Boolean indicating whether directory was set

        directory = directory.getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);

        return result;
    }
	
	/**
	 * runs a python script from the rsources in the given directory.
	 * 
	 * @param dir
	 * @param name
	 * @throws InterruptedException 
	 */
	public static void python(File dir, String... scripts )
		throws IOException, InterruptedException
	{
		int i = 0;

		// * For each script:
		for(String script : scripts)
		{			
			String scriptName = script.split("/")[script.split("/").length-1];
			
			// * ... copy the script into the directory ./python/
			copy("scripts/" + script, dir); 
			
			// * Change directory
			Runtime runtime = Runtime.getRuntime();
			
			// * ... run the script in python/
			Process p = runtime.exec(
					"python " + scriptName, 
					null,
					dir);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			BufferedReader ebr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, format("script%03d.log", i))));			
			BufferedWriter ebw = new BufferedWriter(new FileWriter(new File(dir, format("script%03d.err.log", i))));
			
			System.out.println(p.waitFor());
			
			String line;
			while ( (line = br.readLine()) != null) 
				bw.write(line + "\n");
			while ( (line = ebr.readLine()) != null) 
				ebw.write(line + "\n");
			
			bw.close();
			ebw.close();
			
			i++;
		}
	}
	
	/**
	 * Copies all files and directories in the given classpath directory to 
	 * the given target directory in the filesystem.
	 * 
	 * @param cpDir
	 * @param target
	 */
	public static void copy(String cpDir, File target)
	{		
		URL sourcePath = Functions.class.getClassLoader().getResource(cpDir);
		Global.log().info("Copying static files from path " + sourcePath);
		
		// * Copy static files (css, js, etc)
		try
		{
			copyResources(sourcePath, target);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		Global.log().info("Finished copying");				
	}
	
	public static void copyResources(URL originUrl, File destination) 
			throws IOException 
	{
		System.out.println(originUrl);
	    URLConnection urlConnection = originUrl.openConnection();
	    
	    File file = new File(originUrl.getPath());
	    if (file.exists()) 
	    {	
	    	if(file.isDirectory())
	    		FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
	    	else
	    		FileUtils.copyFile(file, new File(destination, file.getName()));
	    } else if (urlConnection instanceof JarURLConnection) 
	    {
	        copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
	    } else {
	        throw new RuntimeException("URLConnection[" + urlConnection.getClass().getSimpleName() +
	                "] is not a recognized/implemented connection type.");
	    }
	}

	public static void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) 
			throws IOException 
	{
	    JarFile jarFile = jarConnection.getJarFile();
	    
	    Enumeration<JarEntry> entries = jarFile.entries();
	    
	    while(entries.hasMoreElements()) {
	    	JarEntry entry = entries.nextElement();
	    	
	        if (entry.getName().startsWith(jarConnection.getEntryName())) 
	        {
	            String fileName = removeStart(entry.getName(), jarConnection.getEntryName());
	            if (! entry.isDirectory())
	            {
	                InputStream entryInputStream = null;
	                entryInputStream = jarFile.getInputStream(entry);
					copyStream(entryInputStream, new File(destination, fileName));
	               
	            } else
	            {
	                new File(destination, fileName).mkdirs();
	            }
	        }
	    }
	}

	private static void copyStream(InputStream in, File file) 
			throws IOException
	{
		OutputStream out = new FileOutputStream(file);
		int bt = in.read();
		while(bt != -1)
		{
			out.write(bt);
			bt = in.read();
		}
		out.flush();
		out.close();
	}	
	
	private static String removeStart(String string, String prefix)
	{
		if(string.indexOf(prefix) != 0)
			return null;
		
		return string.substring(prefix.length());
	}

}

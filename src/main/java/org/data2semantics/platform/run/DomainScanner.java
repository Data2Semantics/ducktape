package org.data2semantics.platform.run;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.data2semantics.platform.Global;


/**
 * Class responsible for uploading additional modules provided in the domainPath argument of workflow run.
 * @author wibisono
 *
 */
public class DomainScanner {
	
	File baseDir = null;
	Logger log = Logger.getLogger(DomainScanner.class);
	
	public DomainScanner(String classPath) {
		baseDir = new File(classPath);
	}
	
	public void scanDomains(){
		process(baseDir);		
	}
	
	static FileFilter directoryFilter = new FileFilter() {
		@Override
		public boolean accept(File candidate) {
			return candidate.isDirectory();
		}
	};
	
	private void process(File baseDir) {
		File[] subdirs = baseDir.listFiles(directoryFilter);
		
		if(subdirs == null) return;
		
		for(File sd : subdirs){
			if(Global.domainExists(sd.getName())){
				
				if(sd.getName().equals("java")){
					try {
						addURL(sd.toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
				if(sd.getName().equals("python")){
					//Python domain will later on use this system property to add to system path when executing python module
					try {
						String pythonpath = sd.getCanonicalPath();
						pythonpath = pythonpath.replaceAll("\\\\","\\\\\\\\");
						System.setProperty("PYTHONPATH", pythonpath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}
	}
	
	/**
	 * From http://robertmaldon.blogspot.nl/2007/11/dynamically-add-to-eclipse-junit.html
	 * @param url
	 * @throws Exception
	 */
	public void addURL(URL url) throws Exception {
		  URLClassLoader classLoader
		         = (URLClassLoader) ClassLoader.getSystemClassLoader();
		  Class clazz= URLClassLoader.class;

		  // Use reflection
		  Method method= clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
		  method.setAccessible(true);
		  method.invoke(classLoader, new Object[] { url });
		}

}

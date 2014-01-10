package org.data2semantics.platform.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

public class PythonDomainUtil {
	
	public static Process invokePythonScript(String pythonSource) {
		
		ProcessBuilder pb;
		String osname = System.getProperty("os.name");
		
		
		if(osname.startsWith("Windows"))
			pb = new ProcessBuilder("C:\\python27\\python.exe", pythonSource);
		else
			pb = new ProcessBuilder("/usr/bin/python", pythonSource);
		
		Process process=null;
		try {
			process = pb.start();
			pb.redirectErrorStream(true);

			process.waitFor();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return process;
	}
	
	static final String DUCKTAPE_PYTHON_MODULE = "src/test/resources/python/";
	static final String APPEND_DUCKTAPE_LOCATION = "\nimport sys"+
												   "\nsys.path.append('"+DUCKTAPE_PYTHON_MODULE+"')";
	
	static final String IMPORT_DUCKTAPE = "\nimport ducktape"+
										  "\nimport msgpack";
	
	static final String DUMP_DUCKTAPE_REGISTRY = "\nducktape.dump_registry()";
	public static ModuleInfo getDucktapeModulesInfoFromPythonScript(String pythonSourceLocation){
		
		ModuleInfo result = new ModuleInfo();
		
		
		try {
			
			String sourceContent = getFileContent(pythonSourceLocation);
			String fullSource = APPEND_DUCKTAPE_LOCATION+ IMPORT_DUCKTAPE+sourceContent + DUMP_DUCKTAPE_REGISTRY;
			
			File temp = File.createTempFile("temp", ".tmp");
			
			dumpToTemporaryFile(fullSource, temp.getAbsolutePath());
			System.out.println(temp.getAbsolutePath());
			Process p = PythonDomainUtil.invokePythonScript(temp.getAbsolutePath());
			
			File moduleInfoFile = new File("moduleInfo.msg");
			FileInputStream fis = new FileInputStream(moduleInfoFile);
			
			MessagePack messagePack = new MessagePack();
			Unpacker unpacker = messagePack.createUnpacker(fis);
			
			result = unpacker.read(ModuleInfo.class);
			
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		return result;
	}



	public static String getFileContent(String pythonSource) {
		StringBuffer result = new StringBuffer();
		try {
			FileReader reader = new FileReader(new File(pythonSource));
			BufferedReader br = new BufferedReader(reader);
			String line = br.readLine();
			while(line != null){
				result.append("\n"+line);
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	public static void dumpToTemporaryFile(String modifiedSource,
			String modifiedPythonFile) {
		try {
			FileWriter writer =new FileWriter(new File(modifiedPythonFile));
			writer.write(modifiedSource.toString());
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static boolean validate(String pythonSource, List<String> errors) {
		Process p = PythonDomainUtil.invokePythonScript(pythonSource);
		StringBuffer buff = new StringBuffer();
		try {
			buff = readInputStreamToBuffer(p.getErrorStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(buff.length() == 0) return true;
		
		errors.add(buff.toString());
		
		return false;
	}
	
	public static StringBuffer readInputStreamToBuffer(InputStream inputStream)
			throws IOException {
		
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		StringBuffer result = new StringBuffer();
		char[] buf = new char[1024];

		int nread = inputStreamReader.read(buf);
		while (nread >= 0) {
			result.append(buf);
			nread = inputStreamReader.read(buf);
		}

		return result;
	}



	public static String getInputType(String config, String inputName) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(config);
		
		return info.inputType(inputName);
	}



	public static String getOutputType(String config, String outputName) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(config);
		
		return info.outputType(outputName);
	}



	public static List<String> outputs(String source) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(source);
		
		return info.outputs;
	}



	public static String getInputDescription(String source, String inputName) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(source);
		
		return info.inputDescription(inputName);
	}



	public static String getOutputDescription(String source, String outputName) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(source);
		return info.outputDescription(outputName);
	}
	
	
	public static String getModuleFunctionName(String source) {
		ModuleInfo info = getDucktapeModulesInfoFromPythonScript(source);
		
		return info.name;
	}
}

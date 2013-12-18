package org.data2semantics.platform.domain;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

class ConfigurationParser{


	static final String NAME = "name";

	static final String OUTPUTS = "outputs";
	static final String INPUTS = "inputs";
	static final String DESCRIPTION = "description";
	static final String TYPE = "type";
	private static final String COMMAND = "command";

	static List<String> outputs(String source){
		List<String> result = new ArrayList<String>();
		List<Map<String,String>> outputs = getOutputList(source);
		for(Map<?,?> output: outputs){
			result.add((String)output.get(NAME));
		}
		return result;
	}
	
	public static List<String> inputs(String source) {
		List<String> result = new ArrayList<String>();
		List<Map<String,String>> inputs = getInputList(source);
		for(Map<?,?> input: inputs){
			result.add((String)input.get(NAME));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	static List<Map<String, String>> getOutputList(String source){
		Map<?,?> configMap = getConfigMap(source);
		return  (List<Map<String,String>>)configMap.get(OUTPUTS);
	}
	
	@SuppressWarnings("unchecked")
	static List<Map<String, String>> getInputList(String source){
		Map<?,?> configMap = getConfigMap(source);
		return  (List<Map<String,String>>)configMap.get(INPUTS);
	}
	
	static String getCommand(String source){
		Map<?,?> configMap = getConfigMap(source);
		return (String) configMap.get(COMMAND);
	}
	
	// Get the input type from configuration and returned it, to be used for value matches etc on top, which would then have corresponding type value with the java type.
	@SuppressWarnings("unchecked")
	static String getInputType(String source, String inputName){
		Map<?,?> configMap = getConfigMap(source);
		List<Map<String,String>> inputList = (List<Map<String,String>>)configMap.get(INPUTS);
		String result = null;
		for(Map<String,String> input : inputList)
			if(input.get(NAME).equals(inputName)){
				result = input.get(TYPE);
				break;
			}
		
		if(result == null)
			
			throw new IllegalStateException("Input name "+inputName+" is undefined in source " + source);
		
		return result;
				
	}
	
	@SuppressWarnings("unchecked")
	static String getOutputType(String source, String outputName){
		Map<?,?> configMap = getConfigMap(source);
		List<Map<String,String>> inputList = (List<Map<String,String>>)configMap.get(OUTPUTS);
		String result = null;
		for(Map<String,String> input : inputList)
			if(input.get(NAME).equals(outputName)){
				result = input.get(TYPE);
				break;
			}
		
		if(result == null)
			
			throw new IllegalStateException("Output name "+outputName+" is undefined in source " + source);
		
		return result;
				
	}
	
	private static Map<?,?> getConfigMap(String source) {
		Map<?,?> result = null;
		if(source.contains(":")) source = source.split(":")[1];
		try{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
			result = (Map<?, ?>) new Yaml().load(bis);
		} catch(FileNotFoundException e){
			throw new IllegalArgumentException("Command line source configuration file " +source +" can not be found ");
		}
		return result;
	} 
	
}
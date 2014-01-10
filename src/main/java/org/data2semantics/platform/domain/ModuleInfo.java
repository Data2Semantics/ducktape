package org.data2semantics.platform.domain;

import java.util.ArrayList;
import java.util.List;

import org.msgpack.annotation.Message;

@Message
public class ModuleInfo {
	public String name;
	public String description;
	
	public List<String> inputs = new ArrayList<String>();
	public List<String> input_types = new ArrayList<String>();
	
	public List<String> outputs= new ArrayList<String>();
	public List<String> output_types= new ArrayList<String>();
	
	public String toString(){
		StringBuffer result= new StringBuffer();
		result.append("\nModule: "+name);
		result.append("\nDescription: "+name);
		result.append("\nInputs : " + inputs + " | " + input_types);
		result.append("\nOutputs : " + outputs + " | " + output_types);
		return result.toString();
	}
	
	//SHould have used map, but would require change in messagepack. SO for now.
	public String inputType(String inputName){
		String result = null;
		for(int i=0;i<inputs.size();i++){
			if(inputs.get(i).equals(inputName)) 
				return input_types.get(i);
		}
		return result;
	}
	
	public String outputType(String outputName){
		String result = null;
		for(int i=0;i<outputs.size();i++){
			if(outputs.get(i).equals(outputName)) 
				return output_types.get(i);
		}
		return result;
	}

	// We'll deal with these later.
	public String inputDescription(String inputName) {
		// TODO Auto-generated method stubingw
		
		return "";
	}

	public String outputDescription(String outputName) {
		
		return "";
	}
}

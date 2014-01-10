package org.data2semantics.platform.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonType implements DataType {

	public static enum Types {
		
		BOOLEAN("py_boolean"), 
		NUMBER("py_number"), 
		STRING("py_string"), 
		TUPLE("py_tuple"), 
		LIST("py_list"), 
		DICTIONARY("py_dictionary");

		String v;

		private Types(String v) {
			this.v = v;
		}

		public String toString() {
			return v;
		}
		
		public static Types getType(String inputType){
			Types result = STRING;
			if(inputType.contains("boolean"))
				return BOOLEAN;
			
			if(inputType.contains("number") || inputType.contains("int") )
				return NUMBER;
			
			if(inputType.contains("tuple"))
				return TUPLE;
			
			if(inputType.contains("list"))
				return LIST;
		
			if(inputType.contains("dictionary"))
				return DICTIONARY;
		
			return result;
		}
	}

	Types type;

	public PythonType(Types type) {
		this.type = type;
	}

	public PythonType(String inputType) {
		this.type = PythonType.Types.getType(inputType);
	}

	@Override
	public String name() {
		return type.toString();
	}

	@Override
	public String domain() {
		return "python";
	}

	// Getting java equivalent
	public Class<?> clazz() {
		switch (type) {
			case BOOLEAN:
				return Boolean.class;
			
			case NUMBER: //Big Integer instead ? How do I deduce this?
				return Integer.class;

			case STRING:
				return String.class;
			
			case LIST:
				return List.class;
			
			case TUPLE: //For now we deal with tuple as list, revised later.
				return List.class;
			
			case DICTIONARY: //Honestly I am not sure if this is the way to go.
				return Map.class;
				
			
		}

		return String.class;
	}

	public String toString() {
		return clazz().toString();
	}

	public Object valueOf(String stringValue) {

		switch (type) {
			case NUMBER:
				return Integer.valueOf(stringValue);
			
			case STRING:
				return new String(stringValue);
			
			case LIST:
				//TODO parse string as element and return proper arraylist or something
				return new ArrayList<Object>();
			
			case TUPLE:
				return new ArrayList<Object>();
				
			case DICTIONARY:
				return new HashMap<String, String>();

		}

		return stringValue;
	}
	public Types getType(){
		return type;
	}
}

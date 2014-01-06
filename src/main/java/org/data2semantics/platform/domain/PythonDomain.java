package org.data2semantics.platform.domain;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.DomainDefinition;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.PythonType;
import org.data2semantics.platform.util.PlatformUtil;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

@DomainDefinition(prefix="python")
public class PythonDomain implements Domain
{
	private static PythonDomain domain = new PythonDomain();

	
	public boolean execute(ModuleInstance instance, List<String> errors)
	{
	
		// Inject value to the python domain
		String pythonConfig = instance.module().source();
		String pythonSource = ConfigurationParser.getCommand(pythonConfig);
		
		
		
		
		// Call the real execution after value is assigned.
		
		// Extract output result of execution
		
		
		return false;
	}
	

	@Override
	public boolean typeMatches(Output output, Input input) {
		DataType outputType = output.dataType();
		DataType inputType =  input.dataType();
		
		return PlatformUtil.isAssignableFrom( inputType.clazz(), outputType.clazz());

	}

	public boolean check(ModuleInstance instance, List<String> errors)
	{
		return false;
	}

	public List<DataType> conversions(DataType type)
	{
		return null;
	}

	
	public static PythonDomain domain()
	{
		return domain;
	}
	
	public DataType inputType(String config, String inputName)
	{
		String inputType = ConfigurationParser.getInputType(config, inputName);
		DataType result = new PythonType(inputType);
		System.out.println(config + " " + result);
		return result;
	}

	public DataType outputType(String config, String outputName)
	{
		String outputType = ConfigurationParser.getOutputType(config, outputName);
		DataType result = new PythonType(outputType);
		return result;
	}

	@Override
	public List<String> outputs(String source) {

		return ConfigurationParser.outputs(source);
	}
	


	public boolean valueMatches(Object value, DataType type)
	{
		return PlatformUtil.isAssignableFrom(type.clazz(), value.getClass());
		
	}

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {

		StringBuffer input_script = new StringBuffer();
		
		input_script.append("\nimport msgpack\n");
		
		// Dump all input instances using message packers.
		// At the same time generate the input script prefix that is going to be prepended to Python code.
		
		MessagePack messagePack = new MessagePack();
		
		
		// Dump inputs to some agreed intermediate file
		
		// This is going to be ugly, one intermediate file for each of the inputs.
		for(InstanceInput ii : instance.inputs()){
			String currentInputFileName = instance.module().name()+"."+ii.name();
			
			try {
				FileOutputStream fos = new FileOutputStream(currentInputFileName);
			
				Packer packer = messagePack.createPacker(fos);
				
				// Perhaps there is more elegant way of doing this, based on datatype.
				// Still does not handle list/map/other objects
				PythonType type = (PythonType) ii.dataType();
				
				switch (type.getType()) {
				case BOOLEAN:
					packer.write((Boolean) ii.value());
					break;
				case STRING:
					packer.write((String) ii.value());
					break;

				case NUMBER:

					break;

				case TUPLE:
					break;
				case DICTIONARY:
					break;
				case LIST:
					break;

				}
				
				
				packer.close();
				fos.close();
				
				input_script.append("\nf=open("+currentInputFileName+")");
				input_script.append("\ncontent = f.read()");
				input_script.append("\n"+ currentInputFileName +"= msgpack.unpackb(content)");
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// Generate the output_script to dump and be appended at the e
		StringBuffer output_script = new StringBuffer();
		for(InstanceOutput io : instance.outputs()){
			String currentOutputFileName = instance.module().name()+"."+io.name();
			
			output_script.append("\nf=open('"+currentOutputFileName+"')");
			output_script.append("\nf.write(msgpack.packb("+currentOutputFileName+"))");
			output_script.append("\nf.close()");
			
		}
		
		
		
		// Source modification.
		String originalSource = instance.module().source();
		
		StringBuffer modifiedSource = new StringBuffer();
		// Append *input-script* to load the input instance dumps in Python source
		modifiedSource.append(input_script);
		modifiedSource.append(originalSource);
		// Append *output-script* to dump all output generated
		modifiedSource.append(output_script);
		
		
		
		// Execute Python source
		
		// Extract the dumped outputs from Python execution.
		for(InstanceOutput io : instance.outputs()){
			String currentOutputFileName = instance.module().name()+"."+io.name();
			
			try {
				FileInputStream fis = new FileInputStream(new File(currentOutputFileName));
				
				Unpacker unpacker = messagePack.createUnpacker(fis);
				
				// Use the python type to determine how to unpack this stuff.
				
				PythonType type = (PythonType)io.dataType();
				switch(type.getType()){
				
					case BOOLEAN:
						io.setValue(unpacker.readBoolean());
						break;
					case NUMBER:
						io.setValue(unpacker.readBigInteger());
						break;
					case STRING:
						io.setValue(unpacker.readString());
						break;
					case DICTIONARY:
						//TODO use unpacker templates
						break;
					case LIST:
						//TODO use unpacker template
						break;
					case TUPLE:
						//TODO: use unpacker template.
						break;
						
				}
				
				
				fis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		
		
		return false;
	}

	@Override
	public String inputDescription(String source, String name)
	{
		for(Map<String,String> input : ConfigurationParser.getInputList(source)){
			if(input.get(ConfigurationParser.NAME).equals(name))
				return input.get(ConfigurationParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public String outputDescription(String source, String name)
	{
		for(Map<String,String> output : ConfigurationParser.getOutputList(source)){
			if(output.get(ConfigurationParser.NAME).equals(name))
				return output.get(ConfigurationParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public boolean validate(String configuration, List<String> errors) {

		String source = ConfigurationParser.getCommand(configuration);
		PythonInterpreter interpreter = new PythonInterpreter();
		
		PyCode testCompile = null ;
		
		try {
			testCompile = interpreter.compile(new FileReader(source));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errors.add(e.getMessage());
		}
		
		// At least jython can compile this code
		return testCompile != null;
	}


	@Override
	public boolean printInput(String source, String input)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean printOutput(String source, String input)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	
}

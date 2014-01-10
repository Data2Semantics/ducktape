package org.data2semantics.platform.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

@DomainDefinition(prefix="python")
public class PythonDomain implements Domain
{
	private static PythonDomain domain = new PythonDomain();



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
		String inputType = PythonDomainUtil.getInputType(config, inputName);
		DataType result = new PythonType(inputType);
		
		return result;
	}

	public DataType outputType(String config, String outputName)
	{
		String outputType = PythonDomainUtil.getOutputType(config, outputName);
		DataType result = new PythonType(outputType);
		return result;
	}

	@Override
	public List<String> outputs(String source) {

		return PythonDomainUtil.outputs(source);
	}
	


	public boolean valueMatches(Object value, DataType type)
	{
		return PlatformUtil.isAssignableFrom(type.clazz(), value.getClass());
		
	}

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {

		
		// Dump all input instances using message packers.
		// At the same time generate the input script prefix that is going to be prepended to Python code.
		
		MessagePack messagePack = new MessagePack();
		
		
		// Dump inputs to some agreed intermediate file
		StringBuffer input_script = new StringBuffer();
		
		input_script.append("\nimport msgpack\n");
	
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
				
				input_script.append("\nf=open('"+currentInputFileName+"')");
				input_script.append("\ncontent = f.read()");
				// input name is the global variable name.
				input_script.append("\n"+ii.name()+"= msgpack.unpackb(content)");
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// Generate the output_script to dump and be appended at the e
		StringBuffer output_script = new StringBuffer();
		for(InstanceOutput io : instance.outputs()){
			String currentOutputFileName = instance.module().name()+"."+io.name();
			
			output_script.append("\nf=open('"+currentOutputFileName+"', 'w')");
			output_script.append("\nf.write(msgpack.packb("+io.name()+"))");
			output_script.append("\nf.close()");
			
		}
		
		
		
		// Source modification.

		String pythonSourceFile =instance.module().source();

		StringBuffer pythonSource = new StringBuffer();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(pythonSourceFile)));
			String line = reader.readLine();
			while(line != null){
					pythonSource.append("\n"+line);
					line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer modifiedSource = new StringBuffer();
		// Append *input-script* to load the input instance dumps in Python source
		modifiedSource.append(input_script);
		
		
		modifiedSource.append(pythonSource);
		// Append *output-script* to dump all output generated
		modifiedSource.append(output_script);
		
		
		// Execute Python source
		System.out.println("EXECUTING : ");
		System.out.println(modifiedSource);
		
		String modifiedPythonFile = instance.module().name()+".py";
		
		PythonDomainUtil.dumpToTemporaryFile(modifiedSource.toString(), modifiedPythonFile);
		
		PythonDomainUtil.invokePythonScript(modifiedPythonFile);       

		
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
						Boolean bValue =unpacker.readBoolean(); 
						io.setValue(bValue);
						//redundant, not sure why I need this.
						results.put(io.name(), bValue);
						break;
					case NUMBER:
						Integer iValue = unpacker.readInt();
						io.setValue(iValue);
						//redundant, not sure why I need this.
						results.put(io.name(),iValue);
						break;
					case STRING:
						String sValue =unpacker.readString();
						io.setValue(sValue);
						results.put(io.name(),sValue);
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
				System.out.println("File output not found: " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		
		}
		
		
		return false;
	}





	@Override
	public String inputDescription(String source, String inputName)
	{
		return PythonDomainUtil.getInputDescription(source,inputName);
	}

	@Override
	public String outputDescription(String source, String outputName)
	{
		return PythonDomainUtil.getOutputDescription(source,outputName);
	}

	@Override
	public boolean validate(String pythonSource, List<String> errors) {
		
		return PythonDomainUtil.validate(pythonSource, errors);
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

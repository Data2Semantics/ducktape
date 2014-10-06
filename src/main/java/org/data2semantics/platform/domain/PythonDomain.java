package org.data2semantics.platform.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
	Logger log = Logger.getLogger(PythonDomain.class.getName());
	
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
		log.info("TypeCheck " +config + " " + inputName + " " +inputType);
		
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

		// Dump inputs to files.
		dumpPackedInputInstancesToFile(instance);
		
		// Source modification.
		String pythonSourceFile =instance.module().source();
		String pythonSource = "";
		
	
		pythonSource += "\nimport sys" ;
		pythonSource +=	"\nsys.path.append('src/test/resources/python')\n";
		pythonSource +=	"\nsys.path.append('"+System.getProperty("PYTHONPATH")+"')\n";
		pythonSource += PythonDomainUtil.getFileContent(pythonSourceFile);
		
		//If there is a better way to do this.
		String underlyingFunction = PythonDomainUtil.getModuleFunctionName(pythonSourceFile);
	
		pythonSource+="\n"+underlyingFunction+"([])";

		log.info("This is what eventually will be run \n"+pythonSource);
		// Execute Python source
		String modifiedPythonFileName = instance.module().name()+".py";
		
		PythonDomainUtil.dumpToTemporaryFile(pythonSource.toString(), modifiedPythonFileName);
		
		Process p_exec = PythonDomainUtil.invokePythonScript(modifiedPythonFileName);       
		try {
			StringBuffer errorsStream = PythonDomainUtil.readInputStreamToBuffer(p_exec.getErrorStream());
			if(errorsStream.length() > 0)
				errors.add(errorsStream.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		retrievePackedOutputsBackfromFile(instance, results);
		
		cleanupPackedInputFiles(instance);
		
		// Clean up temporary modified file.
		File dumpFile = new File(modifiedPythonFileName);
		if(dumpFile.isFile()){
				dumpFile.delete();
		}
		
		return true;
	}



	private void retrievePackedOutputsBackfromFile(ModuleInstance instance,
			Map<String, Object> results) {
		MessagePack messagePack =new MessagePack();
		// Extract the dumped outputs from Python execution.
		for(InstanceOutput io : instance.outputs()){
			String currentOutputFileName = PythonDomainUtil.getModuleFunctionName(instance.module().source());
			
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
				log.info("File output not found: " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		
		}
	}

	private void  dumpPackedInputInstancesToFile(ModuleInstance instance) {
		// Dump all input instances using message packers.
		// At the same time generate the input script prefix that is going to be prepended to Python code.
		
		MessagePack messagePack = new MessagePack();
		
		
		// Dump inputs to some agreed intermediate file
		
		// This is going to be ugly, one intermediate file for each of the inputs.
		for(InstanceInput ii : instance.inputs()){
			String currentInputFileName = ii.name();
			
			try {
				FileOutputStream fos = new FileOutputStream(currentInputFileName);
			
				Packer packer = messagePack.createPacker(fos);
				
				// Perhaps there is a proper way of doing this, based on datatype.
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
					packer.write((Integer) ii.value());
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
				
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}

	
	/**
	 * Cleaning up files created in previous dumps.
	 * @param instance
	 */
	private void cleanupPackedInputFiles(ModuleInstance instance) {
		for(InstanceInput ii : instance.inputs()){
			String currentInputFileName = ii.name();
			File fi = new File(currentInputFileName);
			if(fi.isFile())
				fi.delete();
		}
		for(InstanceOutput ii : instance.outputs()){
			String currentOutputFileName = ii.name();
			File fi = new File(currentOutputFileName);
			if(fi.isFile())
				fi.delete();
		}
		
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
		return true;
	}

	@Override
	public boolean printOutput(String source, String output)
	{
		// For now always print output of python module. Need a decorator to denote this.
		return true;
	}
	
	
}

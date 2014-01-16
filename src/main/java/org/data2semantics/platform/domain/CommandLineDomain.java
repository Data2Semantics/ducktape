package org.data2semantics.platform.domain;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.CommandLineType;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.util.PlatformUtil;

public class CommandLineDomain implements Domain {

	private static CommandLineDomain domain = new CommandLineDomain();

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {
		// implement execute module, using pipe/runtime
		
		String cmdLineSource = instance.module().source();
		
		String command = ConfigurationParser.getCommand(cmdLineSource);
		
		// input and output perhaps either passed through file or environment variables
		// Setup inputs from module instance
		
		List<InstanceInput> inputs = instance.inputs();
		String []inputEnvironments = new String[inputs.size()];
		
		for(int i=0;i<inputEnvironments.length;i++){
			inputEnvironments[i] = inputs.get(i).name()+"="+inputs.get(i).value();
		}
		
		// Call the main method of the command line
		try {
			
			// Adding an additional command set to show environment variables, in unix this would be env.
			ProcessBuilder pb = new ProcessBuilder( command );

			if(command.endsWith(".py")){
				String osname = System.getProperty("os.name");
				if(osname.startsWith("Windows"))
					pb = new ProcessBuilder("C:\\python27\\python.exe", command);
				else
					pb = new ProcessBuilder("/usr/bin/python", command);
			}
			
			Map<String, String> env = pb.environment();
			
			for(InstanceInput input: inputs){
				env.put(input.name(), input.value().toString());
			}
			
			Process process = pb.start();       
			pb.redirectErrorStream(true);

			process.waitFor();
			
			InputStream inputStream = process.getInputStream ();
			String result = IOUtils.toString(inputStream, "UTF-8");
			
			results.put("result", result);
		
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Failed to execute command line module "+e.getMessage());
			
		}
		
		// Set back the output to list of results.
		// The assumption here is that outputs are stored in the environments variables also.
		
		
		return true;
	}
	
	/**
	 * Accept a by default string value of an output from this command line. In case there is an appropriate type cast to it.
	 * @param output
	 * @param stringValue
	 * @return
	 */
	@SuppressWarnings("unused")
	private Object castOutputType(InstanceOutput output, String stringValue) {
		CommandLineType type = (CommandLineType) output.dataType();
		
		return type.valueOf(stringValue);
	}

	@Override
	public boolean typeMatches(Output output, Input input) {
		DataType outputType = output.dataType();
		DataType inputType =  input.dataType();
		
		return PlatformUtil.isAssignableFrom( inputType.clazz(), outputType.clazz());

	}

	@Override
	public List<DataType> conversions(DataType type) {
		// TODO
		// Implement conversions, we so far only have JavaType datatype
		return null;
	}


	@Override
	public DataType inputType(String source, String inputName) {
		String inputType = ConfigurationParser.getInputType(source, inputName);
		
		if(inputType.contains(CommandLineType.Types.INTEGER.toString()))
			return new CommandLineType(CommandLineType.Types.INTEGER);
		
		return new CommandLineType(CommandLineType.Types.STRING);
	}

	@Override
	public DataType outputType(String source, String outputName) {
		String outputType = ConfigurationParser.getOutputType(source, outputName);
		
		if(outputType.contains(CommandLineType.Types.INTEGER.toString()))
			return new CommandLineType(CommandLineType.Types.INTEGER);
		
		return new CommandLineType(CommandLineType.Types.STRING);
	}

	@Override
	public boolean valueMatches(Object value, DataType type) {
		
		return PlatformUtil.isAssignableFrom(type.clazz(), value.getClass());
	}

	@Override
	public List<String> outputs(String source) {

		return ConfigurationParser.outputs(source);
	}
	
	public List<String> inputs(String source) {

		return ConfigurationParser.inputs(source);
	}
	
	public String getCommand(String source){
		return ConfigurationParser.getCommand(source);
	}


	@Override
	public String inputDescription(String source, String name) {
		for(Map<String,String> input : ConfigurationParser.getInputList(source)){
			if(input.get(ConfigurationParser.NAME).equals(name))
				return input.get(ConfigurationParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public String outputDescription(String source, String name) {
		for(Map<String,String> output : ConfigurationParser.getOutputList(source)){
			if(output.get(ConfigurationParser.NAME).equals(name))
				return output.get(ConfigurationParser.DESCRIPTION);
		}
		return null;
	}

	@Override
	public boolean check(ModuleInstance instance, List<String> errors) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validate(String source, List<String> errors) {
		
		
		return true;
	}

	
	public static CommandLineDomain domain(){
		return domain;
	}
	
	
	@Override
	public boolean printInput(String source, String input)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean printOutput(String source, String input)
	{
		// TODO Auto-generated method stub
		return true;
	}

}

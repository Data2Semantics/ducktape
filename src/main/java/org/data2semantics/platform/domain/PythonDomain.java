package org.data2semantics.platform.domain;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.annotation.DomainDefinition;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.data.DataType;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.PythonType;
import org.data2semantics.platform.util.PlatformUtil;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

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

	public boolean execute(ModuleInstance instance, List<String> errors)
	{
		return false;
	}
	
	public static PythonDomain domain()
	{
		return domain;
	}
	
	public DataType inputType(String source, String inputName)
	{
		String inputType = ConfigurationParser.getInputType(source, inputName);
		DataType result = new PythonType(inputType);
		return result;
	}

	public DataType outputType(String source, String outputName)
	{
		String outputType = ConfigurationParser.getOutputType(source, outputName);
		DataType result = new PythonType(outputType);
		return result;
	}

	public List<String> outputs(String source)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean valueMatches(Object value, DataType type)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean execute(ModuleInstance instance, List<String> errors,
			Map<String, Object> results) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String inputDescription(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String outputDescription(String source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validate(String source, List<String> errors) {

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

	private static class PythonConfigParser {
		
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

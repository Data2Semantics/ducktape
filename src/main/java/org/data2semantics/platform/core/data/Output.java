package org.data2semantics.platform.core.data;

import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.State;

public class Output implements Data 
{
	private String name, description;
	private Module module;
	private DataType dataType;
	
	public Output(String name, String description, Module module, DataType dataType)
	{
		this.name = name;
		this.module = module;
		this.dataType = dataType;
		this.description = description;
	}

	/**
	 * @return The inputs which reference this output.
	 */
	public Module module()
	{
		return module;
	}

	public String name()
	{
		return name;
	}

	public String description()
	{
		return description;
	}
	
	public DataType dataType()
	{
		return dataType;
	}
	
}

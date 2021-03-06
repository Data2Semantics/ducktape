package org.data2semantics.platform.core.data;

import java.io.Serializable;

public class JavaType implements DataType, Serializable
{
	
	protected Class<?> javaClass;
	
	
	public JavaType(Class<?> javaClass)
	{
		super();
		this.javaClass = javaClass;
	}

	@Override
	public String name()
	{
		return javaClass.getCanonicalName();
	}

	@Override
	public String domain()
	{
		return "java";
	}

	public Class<?> clazz(){
		return javaClass;
	}
	
	public String toString(){
		return clazz().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
	
		return javaClass.equals(((JavaType)obj).clazz());
	}
}

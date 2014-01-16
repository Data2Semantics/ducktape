package org.data2semantics.platform.exception;

public class WorkflowCodeMatchException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkflowCodeMatchException(Throwable cause)
	{
		super("The workflow description did not match the implementation specifics.", cause);
	}
	
	public WorkflowCodeMatchException(String message)
	{
		super("The workflow description did not match the implementation specifics. \n Details: " + message);
	}

}

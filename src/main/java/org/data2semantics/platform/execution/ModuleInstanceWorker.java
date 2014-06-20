package org.data2semantics.platform.execution;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.data2semantics.platform.core.ModuleInstance;

public class ModuleInstanceWorker implements Callable<Boolean> {

	Logger log = Logger.getLogger(ModuleInstanceWorker.class);
	ModuleInstance mi;
	
	
	public ModuleInstanceWorker(ModuleInstance mi) 
	{
		this.mi = mi;
	}

	@Override
	public Boolean call() throws Exception 
	{
		log.debug("Starting instance " + mi.module().name() + " thread "+Thread.currentThread().getName());
		
		Boolean result= mi.execute();
		
		log.debug("Finish instance " + mi.module().name() + " thread "+Thread.currentThread().getName());
		return result;
	}

	
}

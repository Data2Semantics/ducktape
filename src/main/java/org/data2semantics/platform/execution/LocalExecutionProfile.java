package org.data2semantics.platform.execution;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.reporting.Reporter;


/**
 * Local execution profile will just run the module in the current VM
 * @author wibisono
 *
 */
public class LocalExecutionProfile extends ExecutionProfile {

	Logger log = Logger.getLogger(LocalExecutionProfile.class);
	@Override
	public void executeModules(List<Module> modules, List<Reporter> reporters) {
		
		for(Module m : modules){
			for(Reporter reporter : reporters){
				
				try {
					reporter.report();
			
				} catch (IOException e) {
					
				}
			}
			
			if(m.ready()){
				
				// Instances of this module will be created
				// Outputs from previous dependency are also provided here.
				m.instantiate();

				
				for(ModuleInstance mi : m.instances()){
	
					log.debug(" Executing instance of module  : " + mi.module().name());
					log.debug("    Inputs : "+mi.inputs());
					mi.execute();
					log.debug("    Outputs : "+mi.outputs());
					log.debug(mi+" "+mi.state());
							
				}
				
			
			} else 
				throw new IllegalStateException("Module not ready: " + m.name());
		}
		
		
	}
	
	
	

}

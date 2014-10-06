package org.data2semantics.platform.execution;

import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.reporting.Reporter;



/**
 * Every execution profile should be able to execute workflow right?
 * Ideally this will be derived by the different execution environment.
 * 
 * 
 * Main responsibility of this class is to execute a module in a certain environment.
 * 
 * @author wibisono
 *
 */
public interface ExecutionProfile {
	
		/**
		 * Execute one by one list of modules within a workflow which are already sorted by the rank.
		 * @param modules	modules within a workflow, which are already sorted based on their rank.
		 * @param reporters  reporters reporting workflow execution.
		 */
		
		public void executeModules(List<Module> modules, List<Reporter> reporters);
}

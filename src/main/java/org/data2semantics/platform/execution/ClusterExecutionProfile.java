package org.data2semantics.platform.execution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.exception.ClusterConfigurationException;
import org.data2semantics.platform.reporting.Reporter;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Job;
import edu.rit.pj2.Task;
import edu.rit.pj2.Tuple;

/**
 * Cluster execution profile. Workflow is treated as Job in Parallel Java 2 Environment.
 * This is also implemented as ExecutionProfile.
 *  
 * @author Adianto
 *
 */

public class ClusterExecutionProfile extends Job implements ExecutionProfile {

	private List<Module> modules;
	
	public ClusterExecutionProfile(String tracker) {
		super();
		try {

			String hp[] = tracker.split(":");
			trackerHost(hp[0]);
			trackerPort(new Integer(hp[1]));
		
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new ClusterConfigurationException("Incorrect job tracker configuration " + tracker);
		}
	}

	public void setJar(String jarFile){
		try {
			jar(new File(jarFile));
		} catch (IOException e) {
			throw new ClusterConfigurationException("Failed to load jar file for cluster execution");
		}
	}
	
	@Override
	public void executeModules(List<Module> modules, List<Reporter> reporters) {
		try {
			this.modules = modules;
			main(new String[0]);
			execute();
		} catch (Exception e) {
			new ClusterExecutionProfile("Failed to execute module");
		}
	}

	
	/**
	 * Main rules setup for the jobs.
	 */
	@Override
	public void main(String[] jobArgs) throws Exception {
		for(Module module : modules){
			if(module.ready()){
				module.instantiate();
				for (ModuleInstance mInstance : module.instances()){
						putTuple(new ModuleTuple(mInstance));
						rule().task(ModuleTask.class).args(new String[]{mInstance.module().name()});
				}
			}
		}
	}

	/*
	 * Maybe I already have to take module from tuple here.
	 */
	private static class ModuleTask extends Task {

		@Override
		public void main(String[] taskArgs) throws Exception {
				System.out.println("Running a task, producing tuples " + Arrays.toString(taskArgs));
				ModuleTuple m = takeTuple(new ModuleTuple());
				System.out.println("Executing " + m.instance().module().name());
				boolean result = m.execute();
				System.out.println("Result "+ result + " "+m.instance().module().name()+ "  "+m.instance().outputs().iterator().next());
		}
		
	}
	
	private static class ModuleTuple extends Tuple {
		ModuleInstance moduleInstance;
		public ModuleTuple(){
			
		}
		public ModuleInstance instance(){
			return moduleInstance;
		}
		public ModuleTuple(ModuleInstance mi){
			moduleInstance = mi;
		}
		@Override
		public void readIn(InStream in) throws IOException {
			moduleInstance = (ModuleInstance) in.readObject();
		}
		@Override
		public void writeOut(OutStream out) throws IOException {
			out.writeObject(moduleInstance);
		}
		
		public boolean execute(){
			return moduleInstance.execute();
		}
	}
}

package org.data2semantics.platform.execution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Branch;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.State;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.exception.ClusterConfigurationException;
import org.data2semantics.platform.reporting.Reporter;
import org.openrdf.rio.RDFParserFactory;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Job;
import edu.rit.pj2.Task;
import edu.rit.pj2.TaskSpec;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.example.ZombieClu;

/**
 * Cluster execution profile. Workflow is treated as Job in Parallel Java 2 Environment.
 * This is also implemented as ExecutionProfile.
 *  
 * @author Adianto
 *
 */

public class ClusterExecutionProfile extends Job implements ExecutionProfile {

	protected Workflow workflow;
	private String tracker;
	
	public ClusterExecutionProfile(){
		super();
		tracker = "localhost:20618";
		init();
	}
	
	public ClusterExecutionProfile(String tracker) {
		super();
		this.tracker = tracker;
		init();
	}
	
	private void init(){
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
	

	public void setTracker(String trackerHostPort) {
		this.tracker = trackerHostPort;
		init();
		
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
			this.workflow = modules.iterator().next().workflow();
			Global.log().info("Start executing modules, setting up workflow rules");
	    	
			main(new String[0]);
			Global.log().info("Start executing workflows ");
	    	
			execute();
			Global.log().info("Finished executing workflows");
	    	
	
		} catch (Exception e) {
			new ClusterExecutionProfile("Failed to execute module");
		}
	}

	
	/**
	 * Main rules setup for the jobs. This is the master process, which would run at the same place as the job submitter, 
	 * most probably on front nodes, or the same place where tracker is running.
	 */
	@Override
	public void main(String[] jobArgs) throws Exception {
		
		//Start rule, immediately start executing this instance remotely
		// How do I pass all the modules to the WorkflowTask ?
		putTuple(new WorkflowTuple(workflow));
		rule().task(WorkflowTask.class).args(new String[0]);
		rule().whenMatch(new ModuleInstanceTuple()).task(ModuleTask.class);
		
	}

	private static class WorkflowTask extends Task {
	
		/**
		 * Accept number of initial module involves, not instances. All are available as tuples.
		 */
		@Override
		public void main(String[] args) throws Exception {
			WorkflowTuple wTuple = takeTuple(new WorkflowTuple());
			List<Module> modules = wTuple.workflow().modules();

			for(Module module : modules){
				if(module.ready()){
					Global.log().info("Instantiating " + module.name());
					module.instantiate();
					
					for (ModuleInstance mInstance : module.instances()){
							putTuple(new ModuleInstanceTuple(mInstance));
					}
					
					// Gathering remote instances. Blocking. Requiring enough workers.
					List<ModuleInstance> remoteInstances = new ArrayList<ModuleInstance>();
					
					System.out.println("Blocking, and gathering result of execution ");
					for(int i=0;i<module.instances().size();i++){
						ExecutedModuleTuple executed = takeTuple(new ExecutedModuleTuple());
						remoteInstances.add(executed.instance());
					}
					
					module.instances(remoteInstances);
					System.gc();
				} 
			}
			Global.log().info("All workflow task done");
		}
		
	}
	
	/*
	 * Module task that will be executed remotely.
	 */
	private static class ModuleTask extends Task {

		@Override
		public void main(String[] taskArgs) throws Exception {
				ModuleInstanceTuple m = (ModuleInstanceTuple)getMatchingTuple(0);
				System.out.println("Executing " + m.instance().module().name());
				boolean result = m.execute();
				System.out.println("Result "+ result + " "+m.instance().module().name()+ "  "+m.instance().outputs().iterator().next());
				putTuple(new ExecutedModuleTuple(m.instance()));
		}
		
	}
	
	private static class WorkflowTuple extends Tuple {
		Workflow workflow ;
		public WorkflowTuple(){
			
		}
		public Workflow workflow(){
			return workflow;
		}
		public WorkflowTuple(Workflow m){
			workflow = m;
		}
		@Override
		public void readIn(InStream in) throws IOException {
			workflow = (Workflow) in.readObject();
		}
		@Override
		public void writeOut(OutStream out) throws IOException {
			out.writeObject(workflow);
		}
	}
	
	private static class ModuleInstanceTuple extends Tuple {
		ModuleInstance moduleInstance;
		public ModuleInstanceTuple(){
			
		}
		public ModuleInstance instance(){
			return moduleInstance;
		}  
		public ModuleInstanceTuple(ModuleInstance mi){
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
	
	private static class ExecutedModuleTuple extends Tuple{
		ModuleInstance executedInstance;
		public ExecutedModuleTuple(){
			
		}
		public ExecutedModuleTuple(ModuleInstance mi){
			executedInstance = mi;
		}
		public ModuleInstance instance(){
			return executedInstance;
		}
		@Override
		public void readIn(InStream in) throws IOException {
			executedInstance = (ModuleInstance) in.readObject();
		}
		@Override
		public void writeOut(OutStream out) throws IOException {
			out.writeObject(executedInstance);
		}
		
	}

}

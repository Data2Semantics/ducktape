package org.data2semantics.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.execution.ClusterExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestClusterEnvironment {

	Logger log = Logger.getLogger(TestClusterEnvironment.class);
	

	@Test
	public void testClusterEnvironment() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/Diamond.yaml");
		
		log.debug("Check Workflow " +workflow);
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ClusterExecutionProfile clusterExecutionProfile = new ClusterExecutionProfile("localhost:20618");
		
		clusterExecutionProfile.setJar("ducktape.jar");
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, clusterExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
	}
	@Test
	public void testSerializationWorkflowModule() throws Exception {
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/Diamond.yaml");
		Workflow wfClone = SerializationUtils.clone(workflow);
		
		List<Module> clModules = wfClone.modules();
		
		List<Module> modules = workflow.modules();
		
		System.out.println(clModules.size() + " "+ modules.size());
		
		System.out.println(clModules.iterator().next().name()+ " "+ modules.iterator().next().name());
		
	}
	@Test
	public void testSerializationOfModuleInstance() throws Exception {
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/Diamond.yaml");
		
		
		List<Module> modules = workflow.modules();
		Module firstModule = modules.iterator().next();
		firstModule.instantiate();
		for(ModuleInstance mi : firstModule.instances()){
			ModuleInstance miClone = SerializationUtils.clone(mi);
			System.out.println(miClone.module().name());
			List<InstanceInput> instanceInputs= mi.inputs();
			for(InstanceInput ii: instanceInputs){
				InstanceInput iiClone = SerializationUtils.clone(ii);
				System.out.println(iiClone.name());
				
				
			}
			
		}
	}
	
		

}

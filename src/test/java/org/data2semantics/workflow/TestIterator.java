package org.data2semantics.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.reporting.HTMLReporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.PlatformUtil;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestIterator {

	@Test
	public void testIterators() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/iterator-test.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.execute();
		
		for(Module m : workflow.modules()){
			System.out.println("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					System.out.print(io.name()+":"+io.value()+ " ");
			}
		}
		
				
		HTMLReporter reporter = new HTMLReporter(workflow, new File("output_dir_iterator"));
		reporter.report();
	}
	
	@Test
	public void testAnAdder() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/AnAdder.yaml");
		
		
		System.out.println("Check Workflow " +workflow);
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.execute();
		
		for(ModuleInstance mi :  workflow.modules().get(0).instances())
		for(InstanceOutput io : mi.outputs())
			System.out.print(io.value()+ " ");
				
		//workflowContainer.dumpIntermediateResults();
		platformOrchestrator.writeOutput("output_dir1");
		
	}
	
	@Test
	public void testAMultiplier() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/AMultiplier.yaml");
		
		
		System.out.println("Check Workflow " +workflow);
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		
		platformOrchestrator.execute();
		
		for(ModuleInstance mi :  workflow.modules().get(0).instances())
		for(InstanceOutput io : mi.outputs())
			System.out.print(io.value()+ " ");
		
		//workflowContainer.dumpIntermediateResults();
		platformOrchestrator.writeOutput("output_dir1");
		
	}
	
	@Test
	public void testAList() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/AListSum.yaml");
		
		
		System.out.println("Check Workflow " +workflow);
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		LocalExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		
		platformOrchestrator.execute();
		
		System.out.println(workflow.modules().get(0).instances().get(0).outputs().get(0).value());
		
		//workflowContainer.dumpIntermediateResults();
		platformOrchestrator.writeOutput("output_dir1");
		
	}

	public void testUnroll (){
			Object [] args = new Object[3];
			Object [] temp = new Object[3];
			List<Object[]> unrolled = new ArrayList<Object[]>();
			boolean [] tobeExpanded = new boolean []{true,true,true};
			
			for(int j=0;j<3;j++){
				List<Integer> test = new ArrayList<Integer>();
				for(int i=0;i<10;i++) test.add(j*10+i);
				args[j] = test;
			}
			
			PlatformUtil.unroll(0, temp, unrolled, args, tobeExpanded);
			
			for(Object[] x : unrolled){
				for(Object y : x)
					System.out.print(y+" ");
				System.out.println();
			}
	}
	

}

package org.data2semantics.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

public class TestPythonDomain {


	@Test
	public void testHelloWorld() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/python/hello.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace);
		
		platformOrchestrator.orchestrate();
		
		for(Module m : workflow.modules()){
			System.out.println("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					System.out.print(io.name()+":"+io.value()+ " ");
			}
		}
		
	}
	

	
}

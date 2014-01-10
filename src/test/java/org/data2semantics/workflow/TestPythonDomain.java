package org.data2semantics.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.domain.ModuleInfo;
import org.data2semantics.platform.domain.PythonDomainUtil;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

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
	

	@Test
	public void testModuleInfo1() throws IOException{
		
		ModuleInfo info = PythonDomainUtil.getDucktapeModulesInfoFromPythonScript("src/test/resources/python/count_triples.py");
		System.out.println(info);
	
	}
	
	@Test
	public void testModuleInfo2() throws IOException{
		
		ModuleInfo info = PythonDomainUtil.getDucktapeModulesInfoFromPythonScript("src/test/resources/python/hello.py");
		System.out.println(info);
	
	}
	
	@Test
	public void messagePackTest() throws IOException{
			ModuleInfo info = new ModuleInfo();
			info.name = "Test module";
			info.description = " Test module description";
			info.inputs.add("Test");
			info.input_types.add("String");
			
			MessagePack messagePack = new MessagePack();
			FileOutputStream fos = new FileOutputStream(new File("test.out"));
			Packer packer = messagePack.createPacker(fos);
			packer.write(info);
			
			fos.close();
	}
}

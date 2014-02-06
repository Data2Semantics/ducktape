package org.data2semantics.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.execution.ThreadedLocalExecutionProfile;
import org.data2semantics.platform.reporting.PROVReporter;
import org.data2semantics.platform.reporting.ExtractTable;
import org.data2semantics.platform.reporting.Reporter;
import org.data2semantics.platform.reporting.TableSpecification;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.PlatformUtil;
import org.data2semantics.platform.util.WorkflowParser;
import org.junit.Test;

public class TestParseProvReport {

	Logger log = Logger.getLogger(TestParseProvReport.class);
	
	@Test
	public void testReconstructWorkflow() throws Exception {
		
		Workflow workflow = WorkflowParser.parseYAML("src/test/resources/AnAdder.yaml");
		
		ResourceSpace resourceSpace = new ResourceSpace();
		
		ExecutionProfile localExecutionProfile = new LocalExecutionProfile();
		
		List<Reporter> reporters = new ArrayList<Reporter>();
		PROVReporter provReporter = new PROVReporter(workflow, new File("prov"));
		
		reporters.add(provReporter);
		
		Orchestrator platformOrchestrator = new Orchestrator(workflow, localExecutionProfile, resourceSpace, reporters);
		
		platformOrchestrator.orchestrate();
		
		for(Module m : workflow.modules()){
			log.debug("\nModule " + m.name());
			
			for(ModuleInstance mi :  m.instances()){
					for(InstanceOutput io : mi.outputs())
					log.debug(io.name()+":"+io.value()+ " ");
			}
		}
		
		
		ExtractTable reconstructor = new ExtractTable(provReporter.getPROVModel());
		//reconstructor.testPrintPROVStatements();
							
		TableSpecification spec = new TableSpecification();
		spec.setOutput("result");
		spec.setRow("first");
		spec.setCol("second");
		spec.setModuleName("ModuleB");
		
		reconstructor.extractTable(spec);
		
	}
	
	

}

package org.data2semantics.workflow;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.data2semantics.platform.bean.InputBean;
import org.data2semantics.platform.bean.ModuleBean;
import org.data2semantics.platform.bean.WorkflowBean;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;


public class TestAnnotationWrapper {

	Logger log = Logger.getLogger(TestAnnotationWrapper.class);

	@Test
	public void testWorkflowBean() {
			
			
			InputBean first = new InputBean("first", 1500);
			InputBean second = new InputBean("second", 1500);
			List<InputBean> inputs = new ArrayList<InputBean>();
			inputs.add(first);
			inputs.add(second);
			
			ModuleBean module1 = new ModuleBean("org.data2semantics.modules.Adder", "Adder1", inputs);
			ModuleBean module2 = new ModuleBean("org.data2semantics.modules.Adder", "Adder2", inputs);
					
			List<ModuleBean> modules = new ArrayList<ModuleBean>();
			modules.add(module1);
			modules.add(module2);
			
			WorkflowBean wf = new WorkflowBean(null, "TestWorkflow");
			wf.setModules(modules);
			
			
			Yaml ym = new Yaml();
			
			String dumpedString = ym.dump(wf);
			
			WorkflowBean loadedBean = (WorkflowBean)ym.load(dumpedString);
			
			int val = 1000;
			for(ModuleBean m : loadedBean.getModules()){
				List<InputBean> inputBeans = m.getInputs();
				inputBeans.get(0).setValue(val++);
			}
			
			for(ModuleBean m : loadedBean.getModules()){
				List<InputBean> inputBeans = m.getInputs();
				log.debug("Check " + inputBeans.get(0).getValue());
			}
			
	}
	
	
}

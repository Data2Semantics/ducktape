package org.data2semantics.platform.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.Input;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.data2semantics.platform.core.data.Output;
import org.data2semantics.platform.core.data.ReferenceInput;
import org.data2semantics.platform.util.FrequencyModel;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class HTMLReporter implements Reporter
{
	protected static Configuration fmConfig = new Configuration();
	
	private Workflow workflow;
	private File root;
	
	public HTMLReporter(Workflow workflow, File root)
	{
		this.workflow = workflow;
		this.root = root;
		
		fmConfig.setClassForTemplateLoading(getClass(), "/html/templates");
		fmConfig.setObjectWrapper(new DefaultObjectWrapper());  
	}

	@Override
	public void report() throws IOException
	{
		new ReportWriter();
	}

	@Override
	public Workflow workflow()
	{
		return workflow;
	}
	
	/**
	 * A singular environment for the purpose of writing a report.
	 * 
	 * @author Peter
	 *
	 */
	private class ReportWriter
	{
		public ReportWriter()
				throws IOException
		{
			for(Module module : workflow.modules())
			{
				// * Output module information
				File moduleDir = new File(root, "/modules/"+ReporterTools.safe(module.name()));
				
				if(module.ready())
				{
					int i = 0;
					for(ModuleInstance instance : module.instances())
					{
						// * Output the instance information
						int padding = 1 + (int)Math.log10(module.instances().size());
						File instanceDir = new File(moduleDir, String.format("./%0"+padding+"d/", i));
	
						instanceOutput(instance, instanceDir, i);
						i++;
					}
				}
				
				moduleOutput(module, moduleDir);
					
			}
			
			// * Output the workflow information
			workflowOutput(workflow, root);
		}

		private String produceDotString(){
			StringBuffer result = new StringBuffer();
			result.append ("digraph{");
			for(Module module : workflow.modules()){
					for(Input inp : module.inputs() ){
						if(inp instanceof ReferenceInput){
							ReferenceInput ri = (ReferenceInput) inp;
							result.append(ri.reference().module().name()+ "->" +  module.name()  + "[label=\"" + ri.reference().name()+ "\"]");
						}
					}
					
			}
			
			result.append("}");
			
			return result.toString();
			
		}
		private void workflowOutput(Workflow workflow, File root)
			throws IOException
		{
			// copy the static files
			ReporterTools.copy("html/static", root);
			
			// * The data we will pass to the template
			Map<String, Object> templateData = new LinkedHashMap<String, Object>();
			
			templateData.put("name", workflow.name());
			templateData.put("short_name", workflow.name());
			templateData.put("tags", "");
			
			templateData.put("dotstring", produceDotString());
			
			List<Map<String, Object>> modules = new ArrayList<Map<String, Object>>();
			
			for(Module module : workflow.modules())
			{
				Map<String, Object> moduleMap = new LinkedHashMap<String, Object>();
				
				moduleMap.put("name", module.name());
				moduleMap.put("url", "./modules/"+ReporterTools.safe(module.name())+"/index.html");
				moduleMap.put("instances", module.instances().size());
				
				modules.add(moduleMap);
			}
			
			templateData.put("modules", modules);
			
			// TODO Image of workflow
			
			// * Load the template
			Template tpl = null;
			try
			{
				tpl = fmConfig.getTemplate("workflow.ftl");
			} catch (IOException e)
			{
				// * Non fatal error (results may be recoverable from the log file). Log and continue
				Global.log().warning("Failed to load module template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			}
			
			root.mkdirs();

			// * Process the template
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(root, "index.html")));
				tpl.process(templateData, out);
				out.flush();			
				
			} catch (IOException e)
			{
				Global.log().warning("Failed to write to module directory. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			} catch (TemplateException e)
			{
				Global.log().warning("Failed to process module template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;			
			}					
		}

		private void moduleOutput(Module module, File moduleDir)
				throws IOException
		{
			// * The data we will pass to the template
			Map<String, Object> templateData = new LinkedHashMap<String, Object>();
			
			templateData.put("name", module.name());
			templateData.put("short_name", module.name());
			templateData.put("tags", "");

			templateData.put("instantiated", module.instantiated());
			
			List<Map<String, Object>> instances = new ArrayList<Map<String, Object>>();
			if(module.instantiated())
			{
				// * Collect the input names
				
				List<String> inputNames = new ArrayList<String>();
				for(Input input : module.inputs())
					inputNames.add(input.name());
				templateData.put("input_names", inputNames);
				
				int i = 0;
				int padding = 1 + (int)Math.log10(module.instances().size());
				
				for(ModuleInstance instance : module.instances())
				{
					// * Collect the instance inputs
					List<String> instanceInputs = new ArrayList<String>();
					for(InstanceInput input : instance.inputs())
						instanceInputs.add(input.value().toString());
					
					// * Collect the instance information
					Map<String, Object> instanceMap = new LinkedHashMap<String, Object>();
					instanceMap.put("url", String.format("./%0"+padding+"d/index.html", i));
					instanceMap.put("inputs", instanceInputs);
					
					instances.add(instanceMap);
					i ++;
				}
			}
			
			templateData.put("instances", instances);
			
			List<Map<String, Object>> outputs = new ArrayList<Map<String,Object>>();
			for(Output output : module.outputs())
			{
				Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
				outputMap.put("name", output.name());
				outputMap.put("description", output.description());
				
				List<Map<String, Object>> outputInstances = new ArrayList<Map<String,Object>>();
				
				// * Collect values for all instances
				List<Object> values = new ArrayList<Object>();
				for(ModuleInstance instance : module.instances())
				{
					Map<String, Object> instanceMap = new LinkedHashMap<String, Object>();

					List<String> inputs = new ArrayList<String>();
					for(InstanceInput input : instance.inputs())
						inputs.add(input.value().toString());
					
					instanceMap.put("inputs", inputs);
					
					Object value =instance.output(output.name()).value();
					instanceMap.put("output", value.toString());
					values.add(value);
					
					outputInstances.add(instanceMap);
				}
				outputMap.put("instances", outputInstances);
				
				// * Collect summary info
				boolean isNumeric = isNumeric(values);
				outputMap.put("is_numeric", isNumeric);
				
				FrequencyModel<Object> fm = new FrequencyModel<Object>(values);
				outputMap.put("mode", fm.sorted().get(0).toString());
				outputMap.put("mode_frequency", fm.frequency(fm.sorted().get(0)));
				outputMap.put("num_instances", values.size());
				outputMap.put("entropy", fm.entropy());
				
				if(isNumeric)
				{
					List<Number> numbers = numbers(values);
					
					outputMap.put("mean", mean(numbers));
					outputMap.put("dev", standardDeviation(numbers));
					outputMap.put("median", median(numbers));
					
				}
				
				outputs.add(outputMap);
			}
			
			templateData.put("outputs", outputs);
			
			
			List<Map<String, Object>> inputs = new ArrayList<Map<String,Object>>();
			for(Input input : module.inputs())
			{
				Map<String, Object> inputMap = new LinkedHashMap<String, Object>();
				inputMap.put("name", input.name());
				inputMap.put("description", input.description());
				
				List<String> values = new ArrayList<String>();
				for(ModuleInstance instance : module.instances())
					values.add(instance.input(input.name()).value().toString());
				
				inputMap.put("values", values);
				inputs.add(inputMap);
			}
			
			templateData.put("inputs", inputs);
			
			// * Load the template
			Template tpl = null;
			try
			{
				tpl = fmConfig.getTemplate("module.ftl");
			} catch (IOException e)
			{
				// * Non fatal error (results may be recoverable from the log file). Log and continue
				Global.log().warning("Failed to load module template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			}
			
			moduleDir.mkdirs();

			// * Process the template
			try
			{
				BufferedWriter out = new BufferedWriter( new FileWriter(new File(moduleDir, "index.html")));
				tpl.process(templateData, out);
				out.flush();			
				
			} catch (IOException e)
			{
				Global.log().warning("Failed to write to module directory. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			} catch (TemplateException e)
			{
				Global.log().warning("Failed to process module template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;			
			}			
		}

		/**
		 * Prepare and write a specific module instance to an HTML page.
		 * @param instance
		 * @param instanceDir
		 */
		private void instanceOutput(ModuleInstance instance, File instanceDir, int i)
				throws IOException
		{
			
			// * The data we will pass to the template
			Map<String, Object> templateData = new LinkedHashMap<String, Object>();
			
			templateData.put("name", instance.module().name() + "("+i+")");
			templateData.put("short_name", instance.module().name() + "("+i+")");
			templateData.put("tags", "");
			
			List<Map<String, Object>> inputs = new ArrayList<Map<String, Object>>();
			
			for(InstanceInput input : instance.inputs())
			{
				Map<String, Object> inputMap = new LinkedHashMap<String, Object>();
				inputMap.put("name", input.name());
				inputMap.put("description", input.description());
				inputMap.put("value", input.value());
				
				inputs.add(inputMap);
			}
			
			templateData.put("inputs", inputs);
			
			List<Map<String, Object>> outputs = new ArrayList<Map<String, Object>>();
			
			for(InstanceOutput output : instance.outputs())
			{
				Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
				outputMap.put("name", output.name());
				outputMap.put("description", output.description());
				outputMap.put("value", output.value().toString());
				
				outputs.add(outputMap);
			}
			
			templateData.put("outputs", outputs);
		
			// * Load the template
			Template tpl = null;
			try
			{
				tpl = fmConfig.getTemplate("instance.ftl");
			} catch (IOException e)
			{
				// * Non fatal error (results may be recoverable from the log file). Log and continue
				Global.log().warning("Failed to load instance template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			}
			
			instanceDir.mkdirs();

			// * Process the template
			try
			{
				BufferedWriter out = new BufferedWriter( new FileWriter(new File(instanceDir, "index.html")));
				tpl.process(templateData, out);
				out.flush();			
				
			} catch (IOException e)
			{
				Global.log().warning("Failed to write to instance directory. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;
			} catch (TemplateException e)
			{
				Global.log().warning("Failed to process instance template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
				return;			
			}
		}
		
	}
	
	/**
	 * Whether all result values represent numbers
	 * @return
	 */
	public static boolean isNumeric(List<?> values)
	{
		for(Object value : values)
			if(! (value instanceof Number))
				return false;
		return true;
	}

	public static double mean(List<? extends Number> values)
	{
	
		double sum = 0.0;
		double num = 0.0;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				sum += v; 
				num ++;
			}
		}
		
		return sum/num;
	}
	
	public static double standardDeviation(List<? extends Number> values)
	{
		double mean = mean(values);
		double num = 0.0;
		
		double varSum = 0.0;
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			
			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				double diff = mean - v;
				varSum += diff * diff;
				num++;
			}
		}

		double variance = varSum/(num - 1);
		return Math.sqrt(variance);
	}
	
	public static double median(List<? extends Number> values)
	{
		List<Double> vs = new ArrayList<Double>(values.size());
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();

			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				vs.add(v);
			}
		}
		
		if(vs.isEmpty())
			return -1.0;
		
		Collections.sort(vs);
		
		if(vs.size() % 2 == 1)
			return vs.get(vs.size()/2); // element s/2+1, but index s/2
		return (vs.get(vs.size()/2 - 1) + vs.get(vs.size()/2)) / 2.0;
	}
	
	public static <T> T mode(List<T> values)
	{
		FrequencyModel<T> model = new FrequencyModel<T>(values);
		
		return model.sorted().get(0);
	}

	public static List<Number> numbers(List<Object> values)
	{
		List<Number> numbers = new ArrayList<Number>(values.size());
		
		for(Object value : values)
			try {
				numbers.add( (Number) value);
			} catch (ClassCastException e)
			{
				throw new RuntimeException("Non-number object (type: "+value.getClass()+", toString: "+value+") found in list. Cannot convert to list of numbers.");
			}
		
		return numbers;
	}

}

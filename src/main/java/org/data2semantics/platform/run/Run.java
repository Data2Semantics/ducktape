package org.data2semantics.platform.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.execution.ClusterExecutionProfile;
import org.data2semantics.platform.execution.ExecutionProfile;
import org.data2semantics.platform.execution.LocalExecutionProfile;
import org.data2semantics.platform.execution.Orchestrator;
import org.data2semantics.platform.execution.ThreadedLocalExecutionProfile;
import org.data2semantics.platform.reporting.CSVReporter;
import org.data2semantics.platform.reporting.HTMLReporter;
import org.data2semantics.platform.reporting.PROVReporter;
import org.data2semantics.platform.reporting.Reporter;
import org.data2semantics.platform.resourcespace.ResourceSpace;
import org.data2semantics.platform.util.WorkflowParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Run
{	
	
	enum ExecutionProfiles {LOCAL, THREADED, CLUSTER}
	@Option(name="--profile", usage="Execution profile to be used LOCAL THREADED HADOOP (default: LOCAL) ")
	private static ExecutionProfiles execProfile = ExecutionProfiles.LOCAL;
	
	@Option(name="--workflowjar", usage="Jar file name needed to run your modules for Cluster environment")
	private static String jarFileName=null;
	
	
	@Option(name="--tracker", usage="tracker host:port for Cluster environment")
	private static String trackerHostPort=null;
	
    @Option(name="--domainpath", usage="A directory containing source code and resources to be loaded." +
    		" Each source file should be in a directory that matches the name of its controller " +
    		"(ie. java files should be in a directory called 'java'). (default: none)")
	private static String domainPath = "";

    @Option(name="--output", usage="The output directory (default: the working directory)")    
	private static File output = new File(".");
    
    @Argument
    private List<String> arguments = new ArrayList<String>(1);
    
    public static void main(String[] args) throws IOException, XmlPullParserException
    {

    	// * Parse and check the command line input
    	Run run = new Run();
    	    	
    	CmdLineParser parser = new CmdLineParser(run);
    	try
		{
			parser.parseArgument(args);
		} catch (CmdLineException e)
		{
			usageExit(e.getMessage(), parser);
		}
    	
    	if(run.arguments.size() > 1)
    		usageExit("Too many non-option arguments: " + run.arguments, parser);
    	
    	File file;
    	if(run.arguments.isEmpty())
    		file = new File("workflow.yaml");
    	else 
    		file = new File(run.arguments.get(0));
    	
    	if(! file.exists())
    		usageExit("Workflow file ("+file+") does not exist.", parser);
    	
    	if(!output.exists()){
    		output.mkdir();
    	}
    	
    	// -- Beyond this point, errors are not the user's fault, and should not
    	//    cause a usage print. 
    	
    	Global.setBase(output);
    	
    	// * Scan the classpath for any Domains and add them dynamically to the 
    	//   global Domain store.
    	
    	DomainScanner domainScanner = new DomainScanner(domainPath);
    	domainScanner.scanDomains();
    	
    	// * Read the workflow description from a yaml file into a map
    	
    	Workflow workflow = WorkflowParser.parseYAML(file);
    	List<Dependency> dependencies = getMavenDependencies();
    	workflow.setDependencies(dependencies);
    	
    	// -- The workflow object will check the consistency of the inputs and 
    	//    outputs and make sure that everything can be executed.  
    	    	
    	// Set the status file
    	File statusRunning = new File(output, "status.running");
    	statusRunning.createNewFile();
    	
		ExecutionProfile executionProfile;
		
		switch(execProfile){
			case LOCAL:
				executionProfile = new LocalExecutionProfile();
				break;
			case THREADED:
				executionProfile = new ThreadedLocalExecutionProfile();
				break;
			case CLUSTER:
				executionProfile = new ClusterExecutionProfile();
				break;
				
			default:
				executionProfile = new LocalExecutionProfile();
		}
		
		if(jarFileName !=null){
			if(!(executionProfile instanceof ClusterExecutionProfile)){
				usageExit("Jar file only need to be provided for Cluster Execution Profile",parser);
			}
			((ClusterExecutionProfile)executionProfile).setJar(jarFileName);
			
		}
		
		if(trackerHostPort !=null){
			if(!(executionProfile instanceof ClusterExecutionProfile)){
				usageExit("Tracker file only need to be provided for Cluster Execution Profile",parser);
			}
			((ClusterExecutionProfile)executionProfile).setTracker(trackerHostPort);
			
		}
		
		ResourceSpace rp = new ResourceSpace();
		
		
		List<Reporter> reporters = Arrays.asList(
					new HTMLReporter(workflow, new File(output, "report/")),
					new CSVReporter(workflow, new File(output, "csv/"))					
				);
		// new PROVReporter(workflow, new File(output, "prov/")) (removed because of maven issue)
		
		
    	Orchestrator orchestrator = new Orchestrator(workflow,  executionProfile, rp, reporters);
    	Global.log().info("Start orchestrating ");
    	
    	orchestrator.orchestrate();
    	Global.log().info("Finished orchestrating ");
    	for(Reporter reporter : reporters)
	    		reporter.report();
    	
    	
    	// Set status to finished
    	File statusFinished = new File(output, "status.finished");
    	statusFinished.createNewFile();
    	
    	statusRunning.delete();
    	
    	Global.log().info("Workflow execution finished.");
		System.exit(0);
    }
    
    public static void usageExit(String message, CmdLineParser parser)
    {
    	System.err.println(message);
        System.err.println("java -jar Platform.jar [options...] [input file (default:workflow.yaml)]");
        parser.printUsage(System.err);
        
        System.exit(1);
    }

    public static List<Dependency> getMavenDependencies() throws FileNotFoundException, IOException, XmlPullParserException{
    	MavenXpp3Reader reader = new MavenXpp3Reader();
    	
    	File pomFile = new File("pom.xml");
    	List<Dependency> dependencies;
    	
    	if(pomFile.exists())
    	{
			Model model = reader.read(new FileReader(pomFile));
			dependencies = model.getDependencies();
    	} else
    		dependencies = Collections.emptyList();
			
		return dependencies;
    }
    
}

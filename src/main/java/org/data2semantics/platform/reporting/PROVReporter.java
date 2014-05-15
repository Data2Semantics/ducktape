package org.data2semantics.platform.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.model.Dependency;
import org.data2semantics.platform.Global;
import org.data2semantics.platform.core.Module;
import org.data2semantics.platform.core.ModuleInstance;
import org.data2semantics.platform.core.Workflow;
import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;





/**
 * Class to report the provenance of a workflow in PROV RDF
 * 
 * 
 * @author Gerben
 *
 */
public class PROVReporter implements Reporter {
	private static final String NAMESPACE = "http://prov.data2semantics.org/";
	private static final String VOCABULARY = NAMESPACE + "vocab/ducktape/";
	private static final String RESOURCE = NAMESPACE + "resource/ducktape/";
	
	
	private static final String PROV_NAMESPACE =  "http://www.w3.org/ns/prov#";
	
	private static final String PROV_FILE = "prov-o.ttl"; 
	
	
	
	private Workflow workflow;
	private File root;

	
	static	ValueFactory factory = ValueFactoryImpl.getInstance();		
	static	URI provEntityURI = factory.createURI(PROV_NAMESPACE, "Entity");
	static	URI provActivityURI = factory.createURI(PROV_NAMESPACE, "Activity");
	static	URI provUsedURI = factory.createURI(PROV_NAMESPACE, "used");
	static	URI provWasGeneratedByURI  = factory.createURI(PROV_NAMESPACE, "wasGeneratedBy");
	static	URI	profGenAtURI  = factory.createURI(PROV_NAMESPACE, "generatedAtTime");
	static	URI	provStartAtURI  = factory.createURI(PROV_NAMESPACE, "startedAtTime");
	static	URI	provEndAtURI  = factory.createURI(PROV_NAMESPACE, "endedAtTime");
	
	static	URI provAgentURI = factory.createURI(PROV_NAMESPACE, "Agent");
	static	URI provWasAttributedToURI  = factory.createURI(PROV_NAMESPACE, "wasAttributedTo");
	static	URI provWasAssociatedWithURI  = factory.createURI(PROV_NAMESPACE, "wasAssociatedWith");
	
	
	static	URI provPlanURI  = factory.createURI(PROV_NAMESPACE, "Plan");
	static	URI provAssociationURI  = factory.createURI(PROV_NAMESPACE, "Association");	
	static	URI provQualifiedAssociationURI  = factory.createURI(PROV_NAMESPACE, "qualifiedAssociation");	
		
	static	URI provHadPlanURI  = factory.createURI(PROV_NAMESPACE, "hadPlan");
	static	URI provHadAgentURI  = factory.createURI(PROV_NAMESPACE, "agent");
		
	static	URI d2sValueURI = factory.createURI(VOCABULARY, "value");
	static	URI d2sDatasetURI = factory.createURI(VOCABULARY, "Dataset");
	static	URI d2sAggregatorURI = factory.createURI(VOCABULARY, "Aggregator");
	
	//static	URI d2sResultURI = factory.createURI(VOCABULARY, "Result");
	
	//static URI  d2sInputURI = factory.createURI(VOCABULARY, "Input");
	//static URI  d2sOutputURI = factory.createURI(VOCABULARY, "Output");
	//static URI  d2sModuleURI = factory.createURI(VOCABULARY, "Module");
	//static URI d2sInstanceOfURI = factory.createURI(VOCABULARY, "instanceOf");
			
	
	static URI d2sUsesArtifactURI = factory.createURI(VOCABULARY, "usesArtifact");
	static URI d2sHasArtifactIdURI = factory.createURI(VOCABULARY, "hasArtifactId");
	static URI d2sHasGroupIdURI = factory.createURI(VOCABULARY, "hasGroupId");
	static URI d2sHasVersionURI = factory.createURI(VOCABULARY, "hasVersion");
	static URI d2sResultOf = factory.createURI(VOCABULARY, "resultOf");
	
	public PROVReporter(Workflow workflow, File root) {
		super();
		this.workflow = workflow;
		this.root = root;
		root.mkdirs();
	}

	public Model getPROVModel() throws IOException{
		return writePROV();
	}
	@Override
	public void report() throws IOException {
		writePROV();
	}

	@Override
	public Workflow workflow() {
		return workflow;
	}
	
	private Model writePROV() throws IOException {

		Model stmts = new LinkedHashModel();
		
		FileInputStream fis = new FileInputStream(workflow.file());
		String workflowMD5sum = DigestUtils.md5Hex(fis);
		long currentTimeMilis = System.currentTimeMillis();
		
		// Define all the URI's that we are going to (re)use

		URI platformURI = factory.createURI(RESOURCE + "ducktape/", InetAddress.getLocalHost().getHostName() + "/" + Global.getSerialversionuid());
		URI workflowURI = factory.createURI(RESOURCE + "workflow/", workflow.file().getAbsolutePath() + "/" + workflowMD5sum);
			
		// The software is the agent and the workflow is the plan
		stmts.add(factory.createStatement(platformURI, RDF.TYPE, provAgentURI)); 
		stmts.add(factory.createStatement(workflowURI, RDF.TYPE, provPlanURI));
		
		// Labels for the platform (ducktape) and current workflow
		stmts.add(factory.createStatement(platformURI, RDFS.LABEL, 
				Literals.createLiteral(factory, "ducktape on: " + InetAddress.getLocalHost().getHostName() + ", versionID: " + Global.getSerialversionuid())));
		stmts.add(factory.createStatement(workflowURI, RDFS.LABEL, 
				Literals.createLiteral(factory, workflow.name() + ", date: " + new Date(workflow.file().lastModified()))));
		
		
		// Add the artifact dependencies as usesArtifact to the plan (workflowURI)
		for (Dependency d : workflow.getDependencies()) {
			URI dependencyURI = factory.createURI(RESOURCE, d.getGroupId() + "/" + d.getArtifactId() + "/" + d.getVersion());
			stmts.add(factory.createStatement(workflowURI, d2sUsesArtifactURI, dependencyURI));
			stmts.add(factory.createStatement(dependencyURI, RDFS.LABEL, Literals.createLiteral(factory, d.getGroupId() + "." + d.getArtifactId() + "." + d.getVersion())));
			stmts.add(factory.createStatement(dependencyURI, d2sHasArtifactIdURI, Literals.createLiteral(factory, d.getArtifactId())));
			stmts.add(factory.createStatement(dependencyURI, d2sHasGroupIdURI, Literals.createLiteral(factory, d.getGroupId())));
			stmts.add(factory.createStatement(dependencyURI, d2sHasVersionURI, Literals.createLiteral(factory, d.getVersion())));
		}
	
		
		String moduleInstanceSumTimestamp = "module/instance/"+InetAddress.getLocalHost().getHostName()+"/"+workflowMD5sum+"/"+currentTimeMilis+"/";
		String moduleClassSumTimestamp = "module/class/"+InetAddress.getLocalHost().getHostName()+"/"+workflowMD5sum+"/"+currentTimeMilis+"/";
		
		for (Module module : workflow.modules()) {
			
			// Create module class, as subclass of prov:Activity
			URI mcURI = factory.createURI(RESOURCE + moduleClassSumTimestamp, module.name());
			stmts.add(factory.createStatement(mcURI, RDFS.SUBCLASSOF, provActivityURI));
			
			for (ModuleInstance mi : module.instances()) {
				// Create provenance for the module (as an activity)
				URI miURI = factory.createURI(RESOURCE + moduleInstanceSumTimestamp, module.name() + mi.moduleID());
				
				
				// ** miURI is type of module class which is subclass of activity ** //
				stmts.add(factory.createStatement(miURI, RDF.TYPE, mcURI)); 				
				
				stmts.add(factory.createStatement(miURI, provStartAtURI, Literals.createLiteral(factory, new Date(mi.startTime())))); // Start time
				stmts.add(factory.createStatement(miURI, provEndAtURI, Literals.createLiteral(factory, new Date(mi.endTime())))); // end time			
				stmts.add(factory.createStatement(miURI, provWasAssociatedWithURI, platformURI)); // wasAssociatedWith
				stmts.add(factory.createStatement(miURI, RDFS.LABEL, Literals.createLiteral(factory, module.name() + mi.moduleID()))); // This activity is labeled as its module name.
				
				//stmts.add(factory.createStatement(miURI,RDF.TYPE, d2sModuleURI));
			
				stmts.add(factory.createStatement(mcURI, RDFS.LABEL, Literals.createLiteral(factory, module.name())));
				
				
				// qualified Association
				BNode bn = factory.createBNode();
				stmts.add(factory.createStatement(bn, RDF.TYPE, provAssociationURI));
				stmts.add(factory.createStatement(bn, provHadPlanURI, workflowURI));
				stmts.add(factory.createStatement(bn, provHadAgentURI, platformURI));
				stmts.add(factory.createStatement(miURI, provQualifiedAssociationURI, bn));
				
				// Create provenance for the outputs (as entities)
				for (InstanceOutput io : mi.outputs()) {
					URI ioURI = factory.createURI(RESOURCE + moduleInstanceSumTimestamp, module.name() + mi.moduleID() + "/output/" + io.name());

					URI coURI = factory.createURI(RESOURCE + moduleClassSumTimestamp, module.name() + "/output/" + io.name());
					
					stmts.add(factory.createStatement(coURI, RDFS.SUBCLASSOF, provEntityURI)); // output class, subclass of prov:Entity
						
					// ** ioURI is of type coURI, which is subclass of entity** //
					
					stmts.add(factory.createStatement(ioURI, RDF.TYPE, coURI)); // coURI
					
					// ** ioURI was Generated By miURI ** //
					stmts.add(factory.createStatement(ioURI, provWasGeneratedByURI, miURI)); // wasGeneratedBy
					stmts.add(factory.createStatement(ioURI, profGenAtURI, Literals.createLiteral(factory, new Date(io.creationTime())))); // generated at time
					stmts.add(factory.createStatement(ioURI, provWasAttributedToURI, platformURI)); // wasAttributedTo
					
					stmts.add(factory.createStatement(coURI, RDFS.LABEL, Literals.createLiteral(factory, io.name())));
					
					// If we can create a literal of the value, save it and create a rdfs-label
					if (Literals.canCreateLiteral(io.value())) {
						stmts.add(factory.createStatement(ioURI, d2sValueURI, Literals.createLiteral(factory, io.value())));
						stmts.add(factory.createStatement(ioURI, RDFS.LABEL, Literals.createLiteral(factory, io)));		
					}
					
					if (io.original().isResult()) {
						stmts.add(factory.createStatement(ioURI, d2sResultOf, workflowURI)); // result
						stmts.add(factory.createStatement(coURI, d2sResultOf, workflowURI)); // result
					}
				}
				
				// Create provenance for the inputs (as entities)
				for (InstanceInput ii : mi.inputs()) {
					URI iiURI = null;
					
					if (ii.instanceOutput() != null) { // It is also an output somewhere
						iiURI = factory.createURI(RESOURCE + moduleInstanceSumTimestamp, ii.instanceOutput().module().name() 
								+ ii.instanceOutput().instance().moduleID() + "/output/" + ii.instanceOutput().name());
					} else {
						iiURI = factory.createURI(RESOURCE + moduleInstanceSumTimestamp, module.name() + mi.moduleID()
								+ "/input/" + ii.name());
						URI ciURI = factory.createURI(RESOURCE + moduleClassSumTimestamp, module.name() + "/input/" + ii.name());
						stmts.add(factory.createStatement(ciURI, RDFS.SUBCLASSOF, provEntityURI)); // instance class subclass of prov:Entity
						
						stmts.add(factory.createStatement(iiURI, RDF.TYPE, ciURI));
						stmts.add(factory.createStatement(ciURI, RDFS.LABEL, Literals.createLiteral(factory, ii.name())));
						
						if (ii.original().isDataset()) {
							stmts.add(factory.createStatement(ciURI, RDF.TYPE, d2sDatasetURI)); // dataset
						}
						if (ii.original().isAggregator()) {
							stmts.add(factory.createStatement(ciURI, RDF.TYPE, d2sAggregatorURI)); // aggregator
						}
						
						// If we can create a literal
						if (Literals.canCreateLiteral(ii.value())) {
							stmts.add(factory.createStatement(iiURI, d2sValueURI, Literals.createLiteral(factory, ii.value())));
							stmts.add(factory.createStatement(iiURI, RDFS.LABEL, Literals.createLiteral(factory, ii)));			
						}			
					}
							
					stmts.add(factory.createStatement(iiURI, RDF.TYPE, provEntityURI)); // entity
					stmts.add(factory.createStatement(miURI, provUsedURI, iiURI)); // used					
				
					
					if (ii.original().isDataset()) {
						stmts.add(factory.createStatement(iiURI, RDF.TYPE, d2sDatasetURI)); // dataset
					}
					if (ii.original().isAggregator()) {
						stmts.add(factory.createStatement(iiURI, RDF.TYPE, d2sAggregatorURI)); // aggregator
					}
				}
			}
		}
		
		File file = new File(root, PROV_FILE);
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileWriter(file));
		
		try {
			writer.startRDF();
			for (Statement stmt : stmts) {
				writer.handleStatement(stmt);
			}
			writer.endRDF();
			
		} catch (RDFHandlerException e) {
			throw new RuntimeException(e);
		}
		
		return stmts;
	}

}

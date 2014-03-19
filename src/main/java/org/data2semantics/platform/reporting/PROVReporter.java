package org.data2semantics.platform.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
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
	private static final String NAMESPACE = "http://www.data2semantics.org/d2s-platform/";
	private static final String PROV_NAMESPACE =  "http://www.w3.org/ns/prov#";
	
	private static final String PROV_FILE = "prov-o.ttl"; 
	
	
	
	private Workflow workflow;
	private File root;

	
	static	ValueFactory factory = ValueFactoryImpl.getInstance();		
	static	URI eURI = factory.createURI(PROV_NAMESPACE, "Entity");
	static	URI acURI = factory.createURI(PROV_NAMESPACE, "Activity");
	static	URI usedURI = factory.createURI(PROV_NAMESPACE, "used");
	static	URI wgbURI  = factory.createURI(PROV_NAMESPACE, "wasGeneratedBy");
	static	URI	genAtURI  = factory.createURI(PROV_NAMESPACE, "generatedAtTime");
	static	URI	startAtURI  = factory.createURI(PROV_NAMESPACE, "startedAtTime");
	static	URI	endAtURI  = factory.createURI(PROV_NAMESPACE, "endedAtTime");
		
	static	URI valueURI = factory.createURI(NAMESPACE, "value");
	static	URI datasetURI = factory.createURI(NAMESPACE, "Dataset");
	static	URI resultURI = factory.createURI(NAMESPACE, "Result");
	
	static	URI agURI = factory.createURI(PROV_NAMESPACE, "Agent");
	static	URI watURI  = factory.createURI(PROV_NAMESPACE, "wasAttributedTo");
	static	URI wawURI  = factory.createURI(PROV_NAMESPACE, "wasAssociatedWith");
		
	static	URI planURI  = factory.createURI(PROV_NAMESPACE, "Plan");
	static	URI assoURI  = factory.createURI(PROV_NAMESPACE, "Association");	
	static	URI qualAssoURI  = factory.createURI(PROV_NAMESPACE, "qualifiedAssociation");	
		
	static	URI hadPlanURI  = factory.createURI(PROV_NAMESPACE, "hadPlan");
	static	URI hadAgentURI  = factory.createURI(PROV_NAMESPACE, "agent");
	
	static URI inputURI = factory.createURI(NAMESPACE, "Input");
	static URI outputURI = factory.createURI(NAMESPACE, "Output");
	static URI moduleURI = factory.createURI(NAMESPACE, "Module");
	
	static URI instanceOfURI = factory.createURI(NAMESPACE, "instanceOf");
			
	
	
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

		URI platformURI = factory.createURI(NAMESPACE + "ducktape/", InetAddress.getLocalHost().getHostName() + "/" + Global.getSerialversionuid());
		URI workflowURI = factory.createURI(NAMESPACE + "workflow/", workflow.file().getAbsolutePath() + "/" + workflowMD5sum);
			
		// The software is the agent and the workflow is the plan
		stmts.add(factory.createStatement(platformURI, RDF.TYPE, agURI)); 
		stmts.add(factory.createStatement(workflowURI, RDF.TYPE, planURI));
		
		// Labels for the platform (ducktape) and current workflow
		stmts.add(factory.createStatement(platformURI, RDFS.LABEL, 
				Literals.createLiteral(factory, "ducktape on: " + InetAddress.getLocalHost().getHostName() + ", versionID: " + Global.getSerialversionuid())));
		stmts.add(factory.createStatement(workflowURI, RDFS.LABEL, 
				Literals.createLiteral(factory, workflow.name() + ", date: " + new Date(workflow.file().lastModified()))));
		
	
		
		String moduleInstanceSumTimestamp = "module/instance/"+InetAddress.getLocalHost().getHostName()+"/"+workflowMD5sum+"/"+currentTimeMilis+"/";
		String moduleClassSumTimestamp = "module/class/"+InetAddress.getLocalHost().getHostName()+"/"+workflowMD5sum+"/"+currentTimeMilis+"/";
		
		for (Module module : workflow.modules()) {
			
			for (ModuleInstance mi : module.instances()) {
				// Create provenance for the module (as an activity)
				URI miURI = factory.createURI(NAMESPACE + moduleInstanceSumTimestamp, module.name() + mi.moduleID());
				URI mcURI = factory.createURI(NAMESPACE + moduleClassSumTimestamp, module.name());
				
				
				// ** miURI is activity ** //
				stmts.add(factory.createStatement(miURI, RDF.TYPE, acURI)); // Activity
				stmts.add(factory.createStatement(miURI, startAtURI, Literals.createLiteral(factory, new Date(mi.startTime())))); // Start time
				stmts.add(factory.createStatement(miURI, endAtURI, Literals.createLiteral(factory, new Date(mi.endTime())))); // end time			
				stmts.add(factory.createStatement(miURI, wawURI, platformURI)); // wasAssociatedWith
				stmts.add(factory.createStatement(miURI, RDFS.LABEL, Literals.createLiteral(factory, module.name() + mi.moduleID()))); // This activity is labeled as its module name.
				
				stmts.add(factory.createStatement(miURI,RDF.TYPE, moduleURI));
				stmts.add(factory.createStatement(miURI, instanceOfURI, mcURI));
				stmts.add(factory.createStatement(mcURI, RDFS.LABEL, Literals.createLiteral(factory, module.name())));
				
				
				// qualified Association
				BNode bn = factory.createBNode();
				stmts.add(factory.createStatement(bn, RDF.TYPE, assoURI));
				stmts.add(factory.createStatement(bn, hadPlanURI, workflowURI));
				stmts.add(factory.createStatement(bn, hadAgentURI, platformURI));
				stmts.add(factory.createStatement(miURI, qualAssoURI, bn));
				
				// Create provenance for the outputs (as entities)
				for (InstanceOutput io : mi.outputs()) {
					URI ioURI = factory.createURI(NAMESPACE + moduleInstanceSumTimestamp, module.name() + mi.moduleID() + "/output/" + io.name());
					URI coURI = factory.createURI(NAMESPACE + moduleClassSumTimestamp, module.name() + "/output/" + io.name());
					
					// ** ioURI is entity** //
					stmts.add(factory.createStatement(ioURI, RDF.TYPE, eURI)); // entity
					// ** ioURI was Generated By miURI ** //
					stmts.add(factory.createStatement(ioURI, wgbURI, miURI)); // wasGeneratedBy
					stmts.add(factory.createStatement(ioURI, genAtURI, Literals.createLiteral(factory, new Date(io.creationTime())))); // generated at time
					stmts.add(factory.createStatement(ioURI, watURI, platformURI)); // wasAttributedTo
					
					stmts.add(factory.createStatement(ioURI, RDF.TYPE, outputURI));
					stmts.add(factory.createStatement(ioURI, instanceOfURI, coURI));
					stmts.add(factory.createStatement(coURI, RDFS.LABEL, Literals.createLiteral(factory, io.name())));
					
					// If we can create a literal of the value, save it and create a rdfs-label
					if (Literals.canCreateLiteral(io.value())) {
						stmts.add(factory.createStatement(ioURI, valueURI, Literals.createLiteral(factory, io.value())));
						stmts.add(factory.createStatement(ioURI, RDFS.LABEL, Literals.createLiteral(factory, io)));		
					}
					
					if (io.original().isResult()) {
						stmts.add(factory.createStatement(ioURI, RDF.TYPE, resultURI)); // result
					}
				}
				
				// Create provenance for the inputs (as entities)
				for (InstanceInput ii : mi.inputs()) {
					URI iiURI = null;
					
					if (ii.instanceOutput() != null) { // It is also an output somewhere
						iiURI = factory.createURI(NAMESPACE + moduleInstanceSumTimestamp, ii.instanceOutput().module().name() 
								+ ii.instanceOutput().instance().moduleID() + "/output/" + ii.instanceOutput().name());
					} else {
						iiURI = factory.createURI(NAMESPACE + moduleInstanceSumTimestamp, module.name() + mi.moduleID()
								+ "/input/" + ii.name());
						URI ciURI = factory.createURI(NAMESPACE + moduleClassSumTimestamp, module.name() + "/input/" + ii.name());
						
						stmts.add(factory.createStatement(iiURI, instanceOfURI, ciURI));
						stmts.add(factory.createStatement(ciURI, RDFS.LABEL, Literals.createLiteral(factory, ii.name())));
						
						// If we can create a literal
						if (Literals.canCreateLiteral(ii.value())) {
							stmts.add(factory.createStatement(iiURI, valueURI, Literals.createLiteral(factory, ii.value())));
							stmts.add(factory.createStatement(iiURI, RDFS.LABEL, Literals.createLiteral(factory, ii)));			
						}			
					}
							
					stmts.add(factory.createStatement(iiURI, RDF.TYPE, eURI)); // entity
					stmts.add(factory.createStatement(miURI, usedURI, iiURI)); // used					
				
					stmts.add(factory.createStatement(iiURI, RDF.TYPE, inputURI));
					
					if (ii.original().isDataset()) {
						stmts.add(factory.createStatement(iiURI, RDF.TYPE, datasetURI)); // dataset
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

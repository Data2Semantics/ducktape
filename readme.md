# Ducktape Experimental Platform 
### *Alpha Release*


##Background

Experiments in [Data2Semantics](http://www.data2semantics.org/) involve various computations which can be organized in a modular way. These modular components, will be reusable in different use cases, using different parameter settings. Deriving semantics from data in this fashion allows for reproducability and hopefully more productive experimentation.

Ducktape aims to achieve this goal  by providing a platform for defining modular component, composing experiments, executing the experiments and generating provenance and reports. Initial experimental use case incorporated in this platform comprise of machine learning experiments developen in WP1-WP2 of Data2Semantics project.


##Features


*Module Creation* :

- easily incorporate existing code as a modular components using a few simple annotations, and to run it in a controlled environment
- support for modules in Java, Python, (Matlab under development)
- command line programs can be used as modules for unsupported languages

*Experiment Composition* :

- complete separation of code and running parameters
- definition of parameter sweep
- coupling of parameter inputs

*Reporting and Provenance* :

- automatically generated reports in HTML/CSV that offer quick insight into the result of an experimental run
- automatic provenance information generated based on PROV-O

*Scalable Execution Environment*:

- scale your experiments from your laptop to a cluster with minimal effort (under development).


##Module Creation

Modules are the main concept in this platform. They are self contained, processing components which can do many operations, data conversion, data analysis, perform graph operations or machine learning tasks. A module is a self-contained unit of annotated code so that the platform can run it. The method of annotation is specified by the domain.

These modules will be treated as black boxes which can be composed from either existing Java or Python code or any other executable that can be wrapped as a module. Modules should accept input data, parameters, a reference to the platform context where it will be executed and will produce output data. For Java and Python we are using annotation to convert existing code base as module.

### Module Domain

A domain describes a programming language and its associated information. An example of a domain is the Java domain. This is the collective name for the method of annotating Java code, the Java type system, the way raw values are converted to Java objects and so on. Some domains do not describe a traditional programming language, but just an execution environment (for instance, the command line domain).


####Java Domain

Module in Java domain is created by annotating existing Java Class. @Module annotation is used to denote the class which will be used as a module. Furthermore, one of the methods defined in this class will be considered as the @Main method to be executed, thus annotated as @Main. This @Main annotation can also refer to constructor of a Java class.

To identify inputs to a module, we can use @In annotation to mark a class field or a function parameter as an input of a module. Example of input annotations are as follows:
 
####Input Annotation on constructors

    @Module(name="LibLINEARParms")
    public class LibLINEARParmsModule {
        public LibLINEARParmsModule(
                @In(name="cs") List<Double> cs, 
                @In(name="nrFolds") int folds, 
                @In(name="target") List<Double> target
                ) {
            [...]
        }
    }

####Input annotation on fields
    @Module(name="Adder", description="Adds two numbers together.")
    public class AdderModule {
        @In(name="first")
        public Integer first;
        @In(name="second")
        public Integer second
    }


Output is annotated in the similar manner as inputs. One or more method can be declared as an output of the module. In the following we can see sample of output annotations either on existing public methods or public fields of a class.

####Output annotation on public methods or fields

    @Out(name="accuracy")
    public double getAccuracy() {
        [...]
    }

    @Out(name="weights")
    public double[] weights;

###Python Module

For Python modules, we are using Python decorator to annotate existing methods/function as a module. The decorator is defined in *ducktape.py* which needs to be imported when defining a module. In Python domain, we have only one decorator @ducktape.main(input_types, ..., output_type) to indicate that a Python function is a module.

####Example Python Module
    import ducktape
    from rdflib import Graph, URIRef

    @ducktape.main("str", "int")
    def countTriples(resourceURI):
        g = Graph()
        g.parse(resourceURI)
        return len(g)

Int the above function, countTriples is converted as a module accepting strings, as input type and integer as output type. The output name will automatically named countTriples the same name as the method name.

###Command Line Module

For command line domain, we assume an existing code runnable from command line. To convert this existing command line code as a module we need an additional configuration file which will describe a required input and output of the command line script.

Assuming an existing script which adds two integers, for example a configuration file will be a YAML file which indicates input and outputs name, type and descriptions.

####Command line configuration example.
    name: 
        Simplest command line
    
    descriptions:
        Basic arithmetic with two inputs from command line

    inputs:
        - name: first
          type: integer
          description: this is the first input
        - name: second
          type: integer
          description: this is the second input
          
    outputs:
        - name: result
          type: integer
          description: default result for this module which is now the same as produc
                   
    command:
        src/test/resources/commandLine/arith.sh

##Experiment Composition

In Ducktape we compose experiments by defining collections of modules together with their inputs, and how they are chained together. We use YAML to capture this experiment definition. 

The structure of the YAML first describe the Workflow name and description, followed by list of modules. Each modules need to provide its name, source and inputs. Inputs to modules can be either raw YAML values (strings, doubles, ints, bools or lists of these) or references to the outputs of other modules.

### Reference

A reference is when one module uses as one of its inputs the output of another module. 

### Sweep

A sweep occurs when the input to a modules (either by reference or raw) is a list of values, where the input specifies a single value which matches each of the entries in the list. When a sweep is encountered, the execution branches: the module is executed once for each value in the list. If a module contains multiple sweeps, the exeuction is branched for each value of the cartesian product of the individual sweeps.

Any module dependent on the output of a module which has been branched is executed once for each branch.  If another sweep is encountered downstream of an existing sweep, a new branch is created for each value of the second sweep. 


### Example workflows excerpts from Affiliation Prediction 

####Workflow
    workflow:
       name: "Affiliation Experiment Test"
       modules:

       - module:
          name: RDFDataSet
          source: org.data2semantics.exp.modules.RDFDataSetModule
          inputs:
             filename: "input.rdf"
             mimetype: "text/n3"
          
       - module:
          name: AffiliationDataSet
          source: org.data2semantics.exp.modules.AffiliationDataSetModule
          inputs: 
             dataset:
                reference: RDFDataSet.dataset
             minSize: 0
            [...]
                
       - module: 
          name: RDFWLSubTreeKernel
          source: org.data2semantics.exp.modules.RDFWLSubTreeKernelModule
          inputs:
             iterations: [0, 2, 4]
             depth: [1, 2]
             dataset:
                reference: RDFDataSet.dataset
             instances:
                reference: AffiliationDataSet.instances
             blacklist:
                reference: AffiliationDataSet.blacklist
                
                
     [...]


##Reporting and Provenance


We support automatic generation of report from a running experiment. The reports is generated based on input and output definition of each experiment modules. 

###CSV Reporter

In this report inputs and corresponding outputs for each individual module instances executed during experiments are reported as CSV data tables. One CSV file is created for each type of module, wherein each module instances is reported as one row of data, with inputs and corresponding outputs is used as columns.

###HTML Reporter

HTML report is a more advanced version of the reporting, which essentially reports the same data represented in the CSV reporter, but additional graph and html formatted table is provided to make it easier to visualize and understand the results of the experiment.

### PROV Reporter

This exports PROV descriptions of how preprocessing and analysis codes are chained together. This would allow experiment to be reproduced.


##Scalable Execution Environment

We are aiming to develop scalable execution environment which allows user to scale their experiment from local machines up to clusters or even using resources in the cloud. Current implemented execution environments are still very basic covering local execution and threaded local execution.


##Deployment

The source code is available in [Github](http://github.com/data2semantics/ducktape).  The following steps are required to use DuckTape:

 * Install DuckTape by obtaining our latest  build of Ducktape from our [Maven Repositories](https://github.com/Data2Semantics/d2s-tools/wiki/Maven-Repositories)
 * Create modules using annotation (or configuration for command line), make sure all these created module are available in current execution environment (as in classpath or PYTHONPATH in case of Python).
 * Compose workflow by creating a Yaml file containing the parameters you would like to run with.
 * Run the workflow using : 

        mvn exec:java -Dexec.mainClass=org.data2semantics.platform.run.Run


##Acknowledgments 

This deliverable was supported by the Dutch national program [COMMIT](http://www.commit-nl.nl/)




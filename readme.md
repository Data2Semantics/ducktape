# Ducktape Experimental Platform 

Experiments in [Data2Semantics](http://www.data2semantics.org/) involve various computations which can be organized in a modular way. These modular components, will be reusable in different use cases, using different parameter settings. Deriving semantics from data in this fashion allows for reproducability and hopefully more productive experimentation.

Ducktape aims to achieve this goal  by providing a platform for defining modular component, composing experiments, executing the experiments and generating provenance and reports. Initial experimental use case incorporated in this platform comprise of machine learning experiments developen in WP1-WP2 of Data2Semantics project.

*[Module Creation](https://github.com/Data2Semantics/ducktape/wiki/Module-Creation)*

- easily incorporate existing Java code as a module using annotations.
- support for modules in Java, Python
- command line programs can be used as modules for unsupported languages

*[Workflow Composition](https://github.com/Data2Semantics/ducktape/wiki/Workflow-Composition)* 

- definition of parameter sweep
- coupling of parameter inputs

*[Reporting and Provenance](https://github.com/Data2Semantics/ducktape/wiki/Reporting-and-Provenance)* 

- automatically generated reports in HTML/CSV that offer quick insight into the result of an experimental run
- automatic provenance information generated based on PROV-O

*[Scalable Execution Environment](https://github.com/Data2Semantics/ducktape/wiki/Scalable-Execution)*

- scale your experiments from your laptop to a cluster with minimal effort. Initial version based on [PJ2](http://www.cs.rit.edu/~ark/pj2.shtml) is implemented, allow remote execution of Java modules.

More info :
* [Installation](https://github.com/Data2Semantics/ducktape/wiki/Installation)
* [Ducktape Wiki](https://github.com/Data2Semantics/ducktape/wiki)

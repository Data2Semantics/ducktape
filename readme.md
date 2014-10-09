# Ducktape Experimental Platform 



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


#I included PJ2 Library from http://www.cs.rit.edu/~ark/pj2.shtml in the lib directory
#Since it's nowhere provided in any maven repository, the following command would install it in your local repository so you can continue building ducktape.
#It is referred to in ducktape maven pom.xml


mvn install:install-file -Dfile=lib/pj2.jar -DgroupId=edu.rit.pj2 -DartifactId=parallel-java -Dversion=1.0 -Dpackaging=jar


#Maven assembly overwrite service provider descriptors in META-INF/services
#So if you have multiple RDFParserFactory for example, only the last dependency would be written.
#For all implementation of these Factory, you need to add the required services.
#See comment from Jeen Broekstra: http://stackoverflow.com/a/24490902


if [ $# -ne 1 ]
then
	echo "Usage ./update.sh <JARFILE>"
	exit
fi

JARFILE=$1
mkdir tmp
cd tmp
echo "Extracting META-INF/services"
jar xf ../$JARFILE META-INF/services
echo "Appending RDFWriterFactory and RDFParserFactory"
echo org.openrdf.rio.n3.N3WriterFactory >> META-INF/services/org.openrdf.rio.RDFWriterFactory
echo org.openrdf.rio.n3.N3ParserFactory >> META-INF/services/org.openrdf.rio.RDFParserFactory
echo org.openrdf.rio.rdfxml.RDFXMLWriterFactory >> META-INF/services/org.openrdf.rio.RDFWriterFactory
echo org.openrdf.rio.rdfxml.RDFXMLParserFactory >> META-INF/services/org.openrdf.rio.RDFParserFactory
echo org.openrdf.rio.turtle.TurtleWriterFactory >> META-INF/services/org.openrdf.rio.RDFWriterFactory
echo org.openrdf.rio.turtle.TurtleParserFactory >> META-INF/services/org.openrdf.rio.RDFParserFactory

echo "Updating jar file"
jar uf ../$JARFILE META-INF/services META-INF/services
cd ..
rm -r tmp


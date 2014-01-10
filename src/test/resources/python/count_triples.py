import ducktape
from rdflib import Graph, URIRef

@ducktape.main("str", "int")
def countTriples(resourceURI):
    g = Graph()
    g.parse(resourceURI)
    return len(g)

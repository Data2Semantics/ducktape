package org.data2semantics.workflow;

import java.io.IOException;
import java.util.ServiceLoader;

import org.junit.Test;
import org.openrdf.rio.RDFParserFactory;

public class TestResourceFinderForServices {
	
	@Test
	public final void testLoadService() throws ClassNotFoundException, IOException{
		ServiceLoader<RDFParserFactory> loader = ServiceLoader.load(RDFParserFactory.class);
		for(RDFParserFactory rpf : loader){
			System.out.println(rpf);
		}
	}
	
}

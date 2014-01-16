package org.data2semantics.workflow;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.data2semantics.platform.util.PlatformUtil;
import org.junit.Test;

public class TestAnnotationOutputField {
	
	Logger log = Logger.getLogger(TestAnnotationOutputField.class);
	
	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException{
		AnnotationOutputField tao = new AnnotationOutputField();
		tao.setResult(100);
		
		log.debug("Just checking " +PlatformUtil.getOutputField(tao, "intField"));
		
	}
	
	@Test
	public void anotherTest() throws Exception {
		Method mainMethod = PlatformUtil.getMainMethod(AnnotationOutputField.class);
		
		Object myModuleObj = PlatformUtil.createModuleWithDefaultConstructor(AnnotationOutputField.class);
		Object [] args = {1234};
		mainMethod.invoke(myModuleObj, args);
		
		log.debug(PlatformUtil.getOutputField(myModuleObj, "intField"));
		
		args = new Object[]{123};
		mainMethod.invoke(myModuleObj, args);
		
		log.debug(PlatformUtil.getOutputField(myModuleObj, "intField"));
		
	}
}

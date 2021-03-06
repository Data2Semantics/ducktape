package org.data2semantics.platform.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation marking the output of a module. Can annotate either a field or a 
 * module.
 * 
 * @author wibisono
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Out {
	
	/**
	 * Name of this output
	 * @return
	 */
	public String name();
	
	public String description() default "";
	
	/**
	 * Whether the value is printed in reports
	 * @return
	 */
	public boolean print() default true;
}

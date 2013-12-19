package org.data2semantics.platform.core.data;

public interface DataType {

		// Name of this data type
		public String name();
				
		// Domain of this datatype
		public String domain();

		// Mapping to java equivalent class/type, must be provided by individual domain.
		public Class<?> clazz();
		
}

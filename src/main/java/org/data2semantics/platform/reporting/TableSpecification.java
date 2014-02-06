package org.data2semantics.platform.reporting;

import org.data2semantics.platform.core.data.InstanceInput;
import org.data2semantics.platform.core.data.InstanceOutput;


/**
 * Simplest specification of a table that should be generated based on PROV result.
 * 
 * 
 * Different Tables that can be specified:
 * 		
 * 		Two Inputs + One output
 * 		- Specify a pair of inputs, and one output. One will provides rows (rowInput), and the other column(columnINput) values.
 * 		- In addition to selecting two pairs of input and an output, user might wanted to fix some parameter values for relevant modules.
 * 		- Somehow the second condition should first be handled, as limiting the universe, an a Graph Sparql query extracting subgraph which is relevant.
 * 
 * 		One input one Output
 * 		- Simple result, would be similar to what we have now.
 * 
 * @author wibisono
 *
 */
public class TableSpecification {

		InstanceInput row, col;
		InstanceOutput out;
		
		String outputSpec=null;
		String rowSpec = null;
		String colSpec = null;
		
		String moduleName=null;
		
		public TableSpecification() {
		}

		public void setOutput(String outputName) {
			outputSpec = outputName;
		}
		
		public void setModuleName(String moduleName){
			this.moduleName = moduleName;
		}
		
		public void setRow(String inputName) {
			rowSpec = inputName;
		}
		
		public void setCol(String inputName) {
			colSpec = inputName;
		}
		
		
		public String getOutputName(){
			return outputSpec == null ? out.name() : outputSpec;
		}
		
		public String getColInputName(){
			return colSpec == null ? col.name() : colSpec;
		}
		
		public String getRowInputName(){
			return rowSpec == null ? row.name() : rowSpec;
		}
		
		public String getModuleName(){
			return moduleName != null? moduleName : out.module().name();
		}
}

package org.data2semantics.platform.execution;

import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;

class IOTuple extends Tuple {
	public Object x = null;
	
	public IOTuple(){
		
	}
	public IOTuple(Object x){
		this.x = x;
	}
	
	@Override
	public void readIn(InStream in) throws IOException {
		x = in.readObject();
	}
	
	@Override
	public void writeOut(OutStream out) throws IOException {
		out.writeObject(x);
		
	}
}

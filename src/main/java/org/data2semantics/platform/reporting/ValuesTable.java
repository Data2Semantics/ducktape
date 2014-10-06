package org.data2semantics.platform.reporting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Table representation, backedup by TreeMap
 * @author wibisono
 *
 * @param <V>
 */

public  class ValuesTable <V> {

	HashMap<V, HashMap<V,V>> rows = new HashMap<V, HashMap<V,V>>();
	
	//Better actually if we can assume these are comparable then use treeset.
	Set<V> uniqueRow = new HashSet<V>();
	
	Set<V> uniqueCol = new HashSet<V>();
	
	public void add(V vRow, V vCol, V vOut) {
		uniqueRow.add(vRow);
		uniqueCol.add(vCol);
		if(!rows.containsKey(vRow)){
			HashMap<V,V> newRow = new HashMap<V, V>();
			newRow.put(vCol, vOut);
			rows.put(vRow,newRow);
		} else {
			HashMap<V,V> curRow = rows.get(vRow);
			curRow.put(vCol, vOut);
		}
	}


	public Set<V> uniqueRows(){
		return uniqueRow;
	}
	
	public Set<V> uniqueCols(){
		return uniqueCol;
	}
	
	public V getValue(V row, V col){
		if(rows.containsKey(row)){
			HashMap<V,V> cols = rows.get(row);
			if(cols.containsKey(col))
				return cols.get(col);
		}
		return null;
	}
	
	public HashMap<V,V> getRow(V row){
		return rows.get(row);
	}
	
		
}

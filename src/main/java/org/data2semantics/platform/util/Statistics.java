package org.data2semantics.platform.util;

import static org.data2semantics.platform.util.Series.series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility functions, mostly for reflection
 * @author Peter
 *
 */
public class Statistics
{
	/**
	 * Whether all result values represent numbers
	 * @return
	 */
	public static boolean isNumeric(List<?> values)
	{
		for(Object value : values)
			if(! (value instanceof Number))
				return false;
		return true;
	}

	public static double mean(List<? extends Number> values)
	{
	
		double sum = 0.0;
		double num = 0.0;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				sum += v; 
				num ++;
			}
		}
		
		return sum/num;
	}
	
	/**
	 * TODO compute more safely
	 * @param values
	 * @return
	 */
	public static double standardDeviation(List<? extends Number> values)
	{
		double mean = mean(values);
		double num = 0.0;
		
		double varSum = 0.0;
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			
			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				double diff = mean - v;
				varSum += diff * diff;
				num++;
			}
		}

		double variance = varSum/(num - 1);
		return Math.sqrt(variance);
	}
	
	public static double median(List<? extends Number> values)
	{
		List<Double> vs = new ArrayList<Double>(values.size());
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();

			if(!(Double.isNaN(v) || Double.isNaN(v)))
			{
				vs.add(v);
			}
		}
		
		if(vs.isEmpty())
			return -1.0;
		
		Collections.sort(vs);
		
		if(vs.size() % 2 == 1)
			return vs.get(vs.size()/2); // element s/2+1, but index s/2
		return (vs.get(vs.size()/2 - 1) + vs.get(vs.size()/2)) / 2.0;
	}
	
	public static <T> T mode(List<T> values)
	{
		FrequencyModel<T> model = new FrequencyModel<T>(values);
		
		return model.maxToken();
	}
	
	public static double min(List<? extends Number> values)
	{
	
		double min = Double.POSITIVE_INFINITY;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
				min = Math.min(min, v);
		}
		
		return min;
	}
	

	public static double max(List<? extends Number> values)
	{
	
		double max = Double.NEGATIVE_INFINITY;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
				max = Math.max(max, v);
		}
		
		return max;
	}	
	
	public static double[] toArray(List<? extends Number> values)
	{
		double[] result = new double[values.size()];
		
		for(int i : series(values.size()))
			result[i] = ((Number) values.get(i)).doubleValue();
		
		return result;
	}
}

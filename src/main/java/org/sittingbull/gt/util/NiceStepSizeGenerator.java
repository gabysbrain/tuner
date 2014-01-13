package org.sittingbull.gt.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * Author: Ahmet Karahan
 */


/**
 * 
 * Nice numbers are just step sizes and used for tick spacing. We will use only niceStep tick
 * spaces and place tick marks at multiples of tick spacing.
 * 
 * References:
 * 	[1] An Extension of Wilkinson's Algorithm for positioning Tick Labels on Axes
 * 		 				(Justin Talbot, Sharon Lin, Pat Hanrahan)
 *  [2] Nice Numbers for Graph Labels (Paul S. Heckbert, Graphic Gems I)
 *
 */
public class NiceStepSizeGenerator {
	
	
	/**
	 * Changing log base requires to change the maximum niceStep step size in Q 
	 * (cannot be bigger than the base), 
	 * otherwise next() will throw ArrayOutOfBound exception during offset calculations...
	 */
	double base = 10.0; 	  			    // num base
	double logB(double a) {
		return Math.log(a)/Math.log(base);  
	}
	public void changeBase(double base) {
		this.base = base;
		reset();
	}
	
	

	/*
	 * State variables of next() method. We can generate infinitely many niceStep step sizes.
	 * However as j increases, the step sizes will be less niceStep...
	 *  
	 */
	double[] Q;		// initial step sizes which we use as seed of generator
	private double[] O;     // offsets
	private double   q;	    // current q (seed step size in Q)
	private double  ss;		// current generated step size j*q
	private double   o;     // current offset of iteration
	private int i, io, j;   
	private Map<Double, Set<Double>> ssOffMap; // step size and its offsets (to prevent repeated processing)

	
	/**
	 * Inner helper class NiceStep used as a C-struct to keep results between runs
	 * of next().
	 */
	public class NiceStep {
		public double stepSize, offset;
		public int i, j;
	}
	NiceStep niceStep = new NiceStep(); 

	
	public NiceStep next() {
		niceStep.stepSize = ss;
		niceStep.offset = o;
		niceStep.i = i; // all output and calculations should add 1 because Wilkinsons index start from 1
		niceStep.j = j;

		// Keep track of existing offsets and stepSizes to avoid duplicate returns of 
		// step size, offset pairs
		if(ssOffMap.containsKey(ss)) {  // ss already exist
			Set<Double> oSet = ssOffMap.get(ss);
			while (!oSet.add(o) && io<O.length-1)  {
				++io;
				niceStep.offset = o = O[io];
			}
			
		} else {
			Set<Double> oSet = new TreeSet<Double>();
			oSet.add(o);
			ssOffMap.put(ss, oSet);
		}
		++io; // position for next offset if it exists
		
		// iterate for next call
		if (io < O.length) {  	// if there are more offsets read them for next call
			o = O[io];    		// read offset and do nothing else
		} else {				// otherwise read for next step size
			io = 0;
			i = (i < Q.length-1) ? i+1 : 0;
			j = (i == 0) ? j+1 : j;
			q  = Q[i];
			ss = stepSize(j, q);
			O  = offsets(j, q);
			o = O[io];
		}	
		resetRequired = true;
		return niceStep; 
	}
	
	
	/**
	 * Generates a NiceNumberGenerator with default parameters 
	 */
	public NiceStepSizeGenerator() {
		this(new double[] {1, 5, 2, 2.5, 4, 3}, 10.0);
	}
	
	

	/**
	 * 
	 * @param Q - Preference ordered list of niceStep step sizes. The default is set by default 
	 * 			  constrauctor is taken from reference paper Ref[1]
	 * @param base - num base of logs and exps (usually changed together with Q).
	 * 
	 * This constructor creates a generator, and initializes all state variables of its
	 * next() method. Default constructor will call this one.
	 * 
	 */
	public NiceStepSizeGenerator(double[] Q, double base) {
		this.Q = Q;
		i = 0;
		io = 0;
		j = 1;
		q = this.Q[i];
		ss = stepSize(j, q);
		O = offsets(j, q);
		ssOffMap = new TreeMap<Double, Set<Double>>();
		resetRequired = false;
		this.base = base;   
	}
	
	
	private boolean resetRequired = false;
	/**
	 * Reset the generator for new production
	 */
	public void reset() {
		if(resetRequired) {
			i = 0;
			io = 0;
			j = 1;
			q = this.Q[i];
			ss = stepSize(j, q);
			O = offsets(j, q);
			ssOffMap.clear();
			resetRequired = false;
		}
	}
	

	
	private double stepSize(int j, double q) {
		return j*q/Math.pow(base, Math.floor(logB(j*q)));
	}


	private double[] offsets(int j, double q) {
		double[] offs = new double[j];
		for(int i=0; i<j; i++) {
			offs[i] = q*i/Math.pow(base, Math.floor(logB(j*q)));
		}
		return offs;
	} 
}

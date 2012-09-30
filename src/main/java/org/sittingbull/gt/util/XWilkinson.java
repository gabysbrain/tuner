package org.sittingbull.gt.util;

import java.text.DecimalFormat;


/*
 * Author: Ahmet Karahan
 */

/* 
 * Reference:
 * 	[1] An Extension of Wilkinson's Algorithm for positioning Tick Labels on Axes
 * 		 				(Justin Talbot, Sharon Lin, Pat Hanrahan)
 */
public class XWilkinson {
	
	private boolean loose = false;
	public void setLooseFlag(boolean value) {
		loose = value;
		nice.reset();
	}
	
	
//	private double w[] = {0.2, 0.25, 0.5, 0.05};
	private double w[] = {0.25, 0.2, 0.5, 0.05};
	private double w(double s, double c, double d, double l) {
		return w[0]*s + w[1]*c + w[2]*d + w[3]*l;
	}
	public void changeW(double[] w) {
		this.w = w;
		nice.reset();
	}
	
	private NiceStepSizeGenerator.NiceStep n;
	public NiceStepSizeGenerator nice;
	public XWilkinson(NiceStepSizeGenerator nice) {
		this.nice = nice;
	}
	
	/*
	 * a mod b for float numbers (reminder of a/b)
	 */
	private double flooredMod(double a, double n) {
		return a-n*Math.floor(a/n);
	}
	
	/*
	 * a conditional eps (should work with very small scales without limitation)
	 */
/*	private double eps(double step) {
		double power = nice.logB(step);
		power -= ((double) Double.MAX_EXPONENT - power > 10) ? 10 : Double.MAX_EXPONENT;
		return Math.pow(nice.base, power);
	}*/

	private double v(double min, double max, double step) {
//		double eps = eps(step);
//		double eps = Math.pow(nice.base, -10);
		return (flooredMod(min, step) < MachineEpsilon.doubleValue() && min<=0 && max>=0) ? 1 : 0;        
	}
	
	private double simplicity(double min, double max, double step) {
	    if(nice.Q.length>1)             
	    	return 1 - (double)n.i/(nice.Q.length-1) - n.j + v(min, max, step);    
	    else
	    	return 1 - n.j + v(min, max, step);
	}
	
	private double simplicity_max() {
		if(nice.Q.length>1)
			return 1 - (double)n.i/(nice.Q.length-1) - n.j + 1.0;                  
		 else
			 return 1 - n.j + 1.0;
	}
	
	private double coverage(double dmin, double dmax, double lmin, double lmax) {
		double a = dmax-lmax;
		double b = dmin-lmin;
		double c = 0.1*(dmax-dmin);
		return 1 - 0.5 * ((a*a+b*b)/(c*c));
	}
	
	private double coverage_max(double dmin, double dmax, double span) {
		double range = dmax-dmin;
		if(span > range) {
			double half = (span-range)/2;
			double r = 0.1*range;
			return 1 - half*half/(r*r);  
		} else
			return 1.0;
	}
	

	/*
	 * 
	 * @param k		number of labels
	 * @param m		number of desired labels
	 * @param dmin	data range minimum
	 * @param dmax	data range maximum
	 * @param lmin	label range minimum
	 * @param lmax	label range maximum
	 * @return		density
	 * 
	 * k-1 number of intervals between labels
	 * m-1 number of intervals between desired number of labels
	 * r   label interval length/label range
	 * rt  desired label interval length/actual range
	 */
	private double density(int k, int m, double dmin, double dmax, double lmin, double lmax) {
		double r = (k-1)/(lmax-lmin);
		double rt = (m-1)/(Math.max(lmax, dmax)-Math.min(lmin, dmin));
		return 2-Math.max(r/rt, rt/r);   // return 1-Math.max(r/rt, rt/r); (paper is wrong) 
	}
	
	private double density_max(int k, int m) {
		if(k >= m) 
			return 2-(k-1)/(m-1);        // return 2-(k-1)/(m-1); (paper is wrong) 	
		else
			return 1;
	}
	
	private double legibility(double min, double max, double step) {
		return 1; // Later more 
	}
	
	public class Label {
		public double min, max, step, score;
		public String toString() {
			DecimalFormat df = new DecimalFormat("00.00");
			String s = "(Score: " + df.format(score) + ") ";
			for(double x=min; x<=max; x=x+step) {
				s += df.format(x) + "\t";
			}
			return s;
		}
        public java.util.List<Double> toList() {
          java.util.ArrayList l = new java.util.ArrayList();
          for(double x=min; x<=max; x+=step) {
            l.add(x);
          }
          return l;
        }
	}
	
	/**
	 * 
	 * @param dmin  data range min
	 * @param dmax  data range max
	 * @param m     desired number of labels
	 * @return      XWilkinson.Label 
	 */
	public Label search(double dmin, double dmax, int m) {
		Label best = new Label();
		double bestScore = -2;
		double sm, dm, cm, delta;
		while(true) {
			n = nice.next();
			sm = simplicity_max();
			if(w(sm, 1, 1, 1) < bestScore) 
				break;
			for(int k=2;;k++) {
				dm = density_max(k, m);
				if(w(sm, 1, dm, 1) < bestScore) 
					break;
				delta = (dmax-dmin)/(k+1)/n.j/nice.Q[n.i];
				double z = Math.ceil(nice.logB(delta));
				while(true) {
					double step = n.j*nice.Q[n.i]*Math.pow(nice.base, z);
					cm = coverage_max(dmin, dmax, step*(k-1));
					if(w(sm,cm,dm,1) < bestScore)
						break;
					double min_start = (Math.floor(dmax/step) - (k-1)) * n.j;
					double max_start = Math.ceil(dmin/step) * n.j;
					if(min_start > max_start) {
						z = z+1;
						continue;
					}
					for(double start = min_start; start<=max_start; start++) {
						double lmin = start * step/n.j;
						double lmax = lmin + step*(k-1);
						double lstep = step;
						double c = coverage(dmin, dmax, lmin, lmax);
						double s = simplicity(lmin, lmax, lstep);
						double d = density(k, m, dmin, dmax, lmin, lmax);
						double l = legibility(lmin, lmax, lstep);
						double score = w(s, c, d, l);
						
						if(score > bestScore && (!loose || (lmin <= dmin && lmax >= dmax))) {
							best.min = lmin; best.max = lmax; best.step = lstep; best.score = score;
							bestScore = score; 
						}
					}
					z = z+1;
				}
			}
		}
		nice.reset();
		return best;
	}
}

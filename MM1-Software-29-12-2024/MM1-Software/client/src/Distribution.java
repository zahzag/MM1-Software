package client;
import java.util.Random;
import java.io.IOException;
import java.lang.Math;
import java.net.UnknownHostException;

/**
 * Class with method for calculation exponentially distributed integers.  
 *
 */
public class Distribution extends Random {

	private static final long serialVersionUID = 5356450403667622020L;

	/*
	public int nextPoisson(double lambda) {
		double elambda = Math.exp(-1 * lambda);
		double product = 1;
		int count = 0;
		int result = 0;
		while (product >= elambda) {
			product *= nextDouble();
			result = count;
			count++; 
		}
		return result;
	}*/
	
	/**
	 * Calculates an exponentially distributed integer.
	 * 
	 * @param b
	 * @return
	 */
	public double nextExponential(double b) {
	    double randx;
	    double result;
	    randx = nextDouble();
		// System.out.println("randx : "+randx);
	    if(randx == 0){
		randx = nextDouble();
	    }
		// System.out.println("MathRandx : "+Math.log(randx));
	    result = -1*b*Math.log(randx);
		System.out.println("results : "+result);
	    return result;
	}

	// public static void main(String[] args) throws UnknownHostException,
    //         IOException, InterruptedException {

	// 			Distribution d1 = new Distribution();
	// 			d1.nextExponential((double) 1);
	// 		}
}

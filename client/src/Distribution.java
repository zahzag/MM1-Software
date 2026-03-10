package client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.lang.Math;

/**
 * Class with method for calculation exponentially distributed integers.  
 * @author zahzag
 */

public class Distribution extends Random {

	private static final long serialVersionUID = 5356450403667622020L;
	private static final Logger log = LoggerFactory.getLogger(Distribution.class);

	public double nextExponential(double b) {
	    double randx;
	    double result;
		if (b <= 0) {
			throw new IllegalArgumentException("Mean arrival rate 'repeat' must be positive");
		}
	    randx = nextDouble();
	    while(randx == 0){
		randx = nextDouble();
	    }

	    return -b*Math.log(randx);
	}

	//to generate a random exponential distributed numnber between 1 and 1.6  to multiply it with repeat=50Million => min=1*50M=50M ; max = 1.6*50M=80M
	public double nextExponentialRepeat() {
		double randx;
		double result;
		double b = 1.0; // Scale parameter for the exponential distribution

		randx = nextDouble(); // Generate a uniform random number in [0, 1)
		if (randx == 0) {
			randx = nextDouble(); // Avoid zero to prevent issues with log
		}

		// Generate exponential random number
		double expValue = -1 * b * Math.log(randx);

		// Transform to fit the range [1, 1.6]
		double min = 1.0; // Lower bound
		double max = 1.33; // Upper bound
		result = min + (max - min) * (expValue / (expValue + 1)); // Scale and shift

		//System.out.println("Generated Exponential Result: " + result);
		return result;
	}

}



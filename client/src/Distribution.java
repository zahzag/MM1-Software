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

   public Distribution() {
   }

   public double nextExponential(double var1) {
      if (var1 <= (double)0.0F) {
         throw new IllegalArgumentException("Mean arrival rate 'repeat' must be positive");
      } else {
         double var3;
         for(var3 = this.nextDouble(); var3 == (double)0.0F; var3 = this.nextDouble()) {
         }

         return -var1 * Math.log(var3);
      }
   }

   public double nextExponentialRepeat() {
      double var5 = (double)1.0F;
      double var1 = this.nextDouble();
      if (var1 == (double)0.0F) {
         var1 = this.nextDouble();
      }

      double var7 = (double)-1.0F * var5 * Math.log(var1);
      double var9 = (double)1.0F;
      double var11 = 1.33;
      double var3 = var9 + (var11 - var9) * (var7 / (var7 + (double)1.0F));
      return var3;
   }
}



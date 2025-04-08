package Server;
/**
*This class simplify communication with cpu
*@author Ayman Zahir
*
*/
import com.sun.jna.Library;

import com.sun.jna.Native;

public class CpuCoreID {
    public interface CLibrary extends Library {
        CLibrary INSTANCE= (CLibrary) Native.loadLibrary("c",CLibrary.class);
        int sched_getcpu();
    }
}

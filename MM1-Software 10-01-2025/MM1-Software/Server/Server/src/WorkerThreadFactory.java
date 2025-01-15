package Server;
import java.util.concurrent.ThreadFactory;

/**
 * This class implements a thread factory for worker threads.
 * 
 * @author Kirschner
 *
 */
public class WorkerThreadFactory implements ThreadFactory {

    String name;
    
    public WorkerThreadFactory (String name) {
        
        this.name = name;
    }
    
    //@Override
    public Thread newThread(Runnable runnable) {

        Thread worker = new Thread(runnable);

        return worker;
    }

}

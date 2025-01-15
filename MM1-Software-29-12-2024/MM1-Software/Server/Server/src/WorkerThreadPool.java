package Server;

import java.util.concurrent.*;

import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.apache.commons.collections4.bag.SynchronizedSortedBag;
import org.apache.poi.util.SystemOutLogger;

import javax.swing.plaf.synth.SynthSpinnerUI;

//import net.openhft.affinity.AffinityStrategies;
//import net.openhft.affinity.AffinityThreadFactory;

/**
 * This class implements a Worker Thread Pool.
 * 
 * @author Kirschner, Roohi
 *
 */
public class WorkerThreadPool {

	public static ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(500);

	public static ThreadFactory workerThreadFactory;

	public static CustomWorkerThreadPool executorPool;

	
	public void runWorkerThreadPool() {
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
		// workerThreadFactory = new WorkerThreadFactory("Worker");
		workerThreadFactory = new AffinityThreadFactory("Worker", AffinityStrategies.DIFFERENT_CORE);

		executorPool = new CustomWorkerThreadPool(1, 1, 10, TimeUnit.SECONDS, queue, workerThreadFactory, rejectionHandler);
		// executorPool.prestartAllCoreThreads();
	}
}

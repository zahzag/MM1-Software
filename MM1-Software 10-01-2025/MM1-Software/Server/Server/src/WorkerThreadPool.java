package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

import net.openhft.affinity.*;
import org.apache.commons.collections4.bag.SynchronizedSortedBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.synth.SynthSpinnerUI;

import static Server.Job.logger;

//import net.openhft.affinity.AffinityStrategies;
//import net.openhft.affinity.AffinityThreadFactory;

/**
 * This class implements a Worker Thread Pool.
 *
 * @author ayman zahir
 *
 */
public class WorkerThreadPool {

	public static ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1000);

	public static ThreadFactory workerThreadFactory;

	public static CustomWorkerThreadPool executorPool;


	public void runWorkerThreadPool() {
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

			int cpuCore = 2; // CPU core to pin to
			//pinToCpuCore(cpuCore);

			//workerThreadFactory = new WorkerThreadFactory("Worker");
		/// ////////for server 1 //////////////////
		workerThreadFactory = new AffinityThreadFactory("Worker",AffinityStrategies.DIFFERENT_CORE );
		executorPool = new CustomWorkerThreadPool(1, 1, 10, TimeUnit.SECONDS, queue, workerThreadFactory, rejectionHandler);

		/// ////// for server 2-- don't work /////////////////////
/*
		AffinityStrategy bindToCpu2 = new AffinityStrategy() {
            @Override
            public boolean matches(int cpuId, int cpuId2) {
                return cpuId == 2;
            }
        };
		workerThreadFactory = new AffinityThreadFactory("Worker",bindToCpu2 );
*/

		/// //////////////////////////////////////////////
		//executorPool.prestartAllCoreThreads();

	}
	private static void pinToCpuCore(int cpuCore) {
		try {
			String command = String.format("sudo cset shield --exec --threads --pid %d -- bash -c 'taskset -cp %d $PPID'", ProcessHandle.current().pid(), cpuCore);
			Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = errorReader.readLine()) != null) {
				System.err.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



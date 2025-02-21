package Server;

/**
 * This class implements afterExecute() method. It captures the
 * state of system after the job has departed and accordingly increments
 * the state counter inside the HashMap.
 * @author Roohi 
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class CustomWorkerThreadPool extends ThreadPoolExecutor {


	public CustomWorkerThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (t != null) {
			System.out.println("Task encountered the following exception(CustomWorkerThread): " + t.getMessage());
		} else {
/*			long time = System.nanoTime();
			Server.leaveSystem.add(time);*/
			
			// Perform afterExecute() logic
			int state = WorkerThreadPool.queue.size();
			// Record's the highest state reached.
			if (state >= Server.highest_state) {
				Server.highest_state = state;
			}
			if(state<=1000){
				Server.hmap.compute(state, (k, v) -> v + 1);
			}else
				System.out.println("OverFlow");
			
		}
	}
	protected void beforeExecute(Thread t,Runnable r) {
		super.beforeExecute(t,r);
/*		long time = System.nanoTime();
		Server.enterExecution.add(time);*/
	}

}

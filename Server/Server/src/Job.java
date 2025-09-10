package Server;

import com.sun.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

import java.security.NoSuchAlgorithmException;

/** This class represents a job. To each job a time stamp and a repeat value is assigned. The
 * time stamp stands for the arrival of the job at the server. The repeat value is an exponentially
 * distributed integer. This number decides the workload of each job. The calc() implements the processing
 * logic of the job.
 * @author zahzag
 */

public class Job implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(Job.class);
	public long timeStamp;
	public int repeat;

	private JobData data = new JobData();
	private long endTimeCurrentRequest;
	private long startTimeStampServiceTime;
	private long endTimeStampServiceTime;
	private long calcTime;
	private long packetLength;
	private long responseTime;
	private long startTimeCpuTime;
	private long endTimeCpuTime;

	private long cpuTime;

	public Job(long timeStamp, int repeat) {
		this.timeStamp = timeStamp;
		this.repeat = repeat;
	}

	// @Override
	public void run() {
		try {
			calc(this.repeat);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		endTimeCurrentRequest = System.nanoTime();
		responseTime = endTimeCurrentRequest - this.timeStamp;//millisecond
		data.setResponseTime(responseTime);
		Server.jobDataQueue.offer(data);

	}

	private void calc(int repeat) throws NoSuchAlgorithmException {
		//		startTimeStampServiceTime = System.nanoTime();

		//check the cpu id used to process the job
		int cpuCore = CpuCoreID.CLibrary.INSTANCE.sched_getcpu();
		logger.info("Job CPU Core: " + cpuCore);


		/*******start computing cpu time ************/
		ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
		if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {

		//computing execution time
		startTimeStampServiceTime = System.nanoTime();

			// Start cpuTime in nanoseconds
			 startTimeCpuTime = threadMXBean.getCurrentThreadCpuTime();

			/** job */

		 for (long i = 1; i <= repeat; i++) {
				//  double temp =Math.sin(i) * Math.cos(i) / Math.pow(i,i);
				Math.pow(Math.sin(i)*Math.cos(i)/Math.pow(i,i),i); //repeat = 1M * 1.6
				//Math.pow(Math.pow(Math.sin(i) * Math.cos(i), Math.pow(Math.sin(i),Math.cos(i))*Math.tan(i*i)),Math.pow(Math.pow(Math.pow(i,i),Math.sqrt(i)),Math.pow(Math.pow(i,i),Math.sqrt(i)))); //repeat = 300000 * 1.33

	  	 }
			// Get end CPU time
			endTimeCpuTime = threadMXBean.getCurrentThreadCpuTime(); // In nanoseconds

		//end computing execution time
		endTimeStampServiceTime = System.nanoTime();
		/**this else is if the system couldn't compte the cpu time */
		} else {
			System.out.println("Thread CPU time measurement is not supported on this JVM.");
		}

		/*******stop computing cpu time ************/

		//execution time
		calcTime = (endTimeStampServiceTime - startTimeStampServiceTime); //nanoseconds
		//System.out.println("executionTime : " +calcTime/1_000_000.0 + " ms");

		// Calculate and display CPU time
		cpuTime = endTimeCpuTime - startTimeCpuTime;
		//System.out.println("CPU Time for Job: " + cpuTime /1_000_000.0 + " ms");

		data.setCalcTime(calcTime);
		data.setCpuTime(cpuTime);
		packetLength = repeat;
		data.setPacketLength(packetLength);

	}

}




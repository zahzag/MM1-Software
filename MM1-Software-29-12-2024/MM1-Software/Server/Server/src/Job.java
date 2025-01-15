package Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

/** This class represents a job. To each job a time stamp and a repeat value is assigned. The
 * time stamp stands for the arrival of the job at the server. The repeat value is an exponentially
 * distributed integer. This number decides the workload of each job. The calc() implements the processing
 * logic of the job.
 * @author Roohi,Kirschner
 * */

public class Job implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Job.class);
    public long timeStamp;
	public int repeat;
	
	private JobData data = new JobData();
	private long endTimeCurrentRequest;
	private long startTimeStampServiceTime;
	private long endTimeStampServiceTime;
	private long calcTime;
	private long packetLength;
	private long responseTime;

	
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
			startTimeStampServiceTime = System.nanoTime();

//			for(int k = 0; k < this.repeat; k++) {
//				Math.pow(Math.pow(Math.cbrt(Math.pow(100,20)), Math.cbrt(Math.pow(55,75))), Math.pow( Math.cbrt(Math.pow(55,75)),  Math.cbrt(Math.pow(55,75))));
//				//Math.pow(Math.pow(10,10),Math.pow(10,10));
//			}
			//check the cpu id used to process the job
			int cpuCore = CpuCoreID.CLibrary.INSTANCE.sched_getcpu();
			log.info("Job CPU Core: " + cpuCore);

			double result =0.0;
			for (long i = 1; i < 10000000L *repeat; i++) {
				result +=1/(Math.pow(i,2));
			}

			endTimeStampServiceTime = System.nanoTime();
			calcTime = (endTimeStampServiceTime - startTimeStampServiceTime);//nanoseconds

			data.setCalcTime(calcTime);
			packetLength = repeat;
			data.setPacketLength(packetLength);

		}


	}



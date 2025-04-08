package Server;

/**
 * This class represents a ResetJob. ResetJob's are received before and after the arrival of Job packets.
 *  
 * @author Ayman Zahir
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

public class ResetJob extends Job {

	private boolean log = false;
	Process p;
	private final Logger logger = LoggerFactory.getLogger(ResetJob.class);

	public ResetJob(long timeStamp, int repeat, boolean log) {
		super(timeStamp, repeat);
		this.log = log;
	}

	// @Override
	public void run() {
		/*
		 * This if block represents first ResetJob. First initialize all variables to
		 * null. Then log FreqStats.
		 */
		if (this.log) {
			System.out.println("***************************************************************");
			LocalTime time01 = LocalTime.now();
			System.out.println("Time at start of Reset job number 1 : " + time01);

			System.out.println("Initializing HashMap in ResetJob");
			for (int i = 0; i <= 1000; i++) {
				Server.hmap.put(i, 0);
			}

			if(Server.hmap.containsValue(null) || Server.hmap.isEmpty() ){
				System.out.println("Initializing HashMap in ResetJob Second Time ");
				for (int i = 0; i <= 1000; i++) {
					Server.hmap.putIfAbsent(i, 0); //to avoid null values if hmap not initialized well
				}
			}
			//logger.info(""+Server.hmap);
			LocalTime time011 = LocalTime.now();
			System.out.println("Time at end of Reset job number 1 : " + time011);
			System.out.println("***************************************************************");

			/*
			 * Wait for first Job to arrive after ResetJob. Aim is to capture
			 * Freqstats only after first job has arrived.
			 */
			try {
				Thread.sleep(1100);
			} catch (InterruptedException ioe) {
				ioe.printStackTrace();
			}
			// Capture FreqStats
			try {
				ProcessBuilder pb = new ProcessBuilder("Server/FreqStatINTEL");
				//ProcessBuilder pb = new ProcessBuilder("/home/rootie/ServerMultiThreads/src/Server/FreqStatAMD");
				p = pb.start();
				LocalTime time02 = LocalTime.now();
				System.out.println("Logging Actually Started : " + time02);
			} catch (Throwable t) {
				// TODO Auto-generated catch block
				t.printStackTrace();
			}

		}
		/*
		 * This else block represents last ResetJob. First capture FreqStats. Then calculate
		 * Metrics and initialize variables to null.
		 */
		else {
			try {
				ProcessBuilder pb = new ProcessBuilder("Server/FreqStatINTEL");
				//ProcessBuilder pb = new ProcessBuilder("/home/rooter/ServerMultiThreads/src/Server/FreqStatAMD");
				p = pb.start();
			} catch (Throwable t1) {
				// TODO Auto-generated catch block
			t1.printStackTrace();
			}
			LocalTime time2 = LocalTime.now();
			System.out.println("Logging Finished : " + time2);
			System.out.println("***************************************************************");
			LocalTime time01 = LocalTime.now();
			System.out.println("Time at start of Reset job number 2 : " + time01);

			Metrics.calculate();// Do calculation

			Server.counter = 0;
			Server.jobDataQueue.clear();

			Metrics.MeanRspTime = 0;
			Metrics.MeanPacketLength = 0;
			Metrics.MeanSrvcTime = 0;
			Metrics.RspTime = 0;
			Metrics.PacketLength = 0;
			Metrics.SrvcTime = 0;
			
			
/*			Server.enterSystem.clear();
			Server.enterExecution.clear();
			Server.leaveSystem.clear();*/
			
			
			Server.highest_state = 0;
			System.out.println("Initializing HashMap in ResetJob");
			for (int i = 0; i <= 1000; i++) {
				Server.hmap.put(i, 0);
			}

			LocalTime time011 = LocalTime.now();
			System.out.println("Time at end of Reset job number 2: " + time011);
			System.out.println("***************************************************************");
			WorkerThreadPool.executorPool.shutdown();
			Server.RUN = 0;
			/**
			 * ensure the full system exiting when jobs are served
			 */
			//System.out.println("after while ");
			System.exit(0);
		}

	}
}

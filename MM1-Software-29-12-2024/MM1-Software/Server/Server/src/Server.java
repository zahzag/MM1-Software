package Server;
import net.openhft.affinity.AffinityLock;

import java.io.IOException;
import java.net.UnknownHostException;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Server with the main class for starting it.
 * 
 * @author Kirschner,Roohi
 *
 */
public class Server {

	static final int RUN = 1;
	static LinkedBlockingQueue<JobData> jobDataQueue;
	static long counter;
	public static int highest_state = 0;
	public static HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
	private Thread serverThread, resetServer;
	static WorkerThreadPool workerThreadPool;
	
/*	public static List<Long> enterSystem = new ArrayList<Long>();
	public static List<Long> enterExecution = new ArrayList<Long>();
	public static List<Long> leaveSystem = new ArrayList<Long>();*/




	public static double lambda;


	/** Initialization of the server running on port and its threads */
	public Server(int port) throws UnknownHostException, IOException {

		jobDataQueue = new LinkedBlockingQueue<JobData>();
		serverThread = new Thread(new ServerThread(port));
		resetServer = new Thread(new ResetListener());
		workerThreadPool = new WorkerThreadPool();

		serverThread.start();
		resetServer.start();
		workerThreadPool.runWorkerThreadPool();
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		//lambda = Double.parseDouble(args[0]);
		lambda = 10;
		//check cpu availability
		System.out.println(AffinityLock.dumpLocks());
		new Server(9999);

	}
}

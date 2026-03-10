package Server;
import net.openhft.affinity.AffinityLock;

import java.io.IOException;
import java.net.UnknownHostException;
//import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
//import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Server with the main class for starting it.
 * 
 * @author zahzag
 *
 */
public class Server {

	static int RUN = 1;
	static LinkedBlockingQueue<JobData> jobDataQueue;
	static long counter;
	public static int highest_state = 0;
	public static HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
	private Thread serverThread, resetServer;
	static WorkerThreadPool workerThreadPool;
	

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


		lambda = Double.parseDouble(args[0]);

		//check cpu availability
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		// Get the number of CPU cores
		System.out.println("Available processors: " + availableProcessors);
		try { System.out.println(AffinityLock.dumpLocks()); } catch (Throwable t) { System.out.println("Affinity info unavailable: " + t.getMessage()); }
		new Server(9999);
	}
}

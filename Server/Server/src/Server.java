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
/**
 * Entry point. Reads the arrival rate (lambda) from args, logs CPU availability
 * and affinity info, then starts the server on port 9999.
 *
 * @param args args[0] - the arrival rate lambda (double)
 * @throws UnknownHostException if the local host cannot be resolved
 * @throws IOException if an I/O error occurs while starting the server
 */
	public static void main(String[] args) throws UnknownHostException, IOException {


		lambda = Double.parseDouble(args[0]);

		//check cpu availability
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		// Get the number of CPU cores
		System.out.println("Available processors: " + availableProcessors);
		
		// Log CPU affinity lock state if available
		try { System.out.println(AffinityLock.dumpLocks()); } catch (Throwable t) { System.out.println("Affinity info unavailable: " + t.getMessage()); }
		
		// Start the server on the default port
		new Server(9999);
	}
}

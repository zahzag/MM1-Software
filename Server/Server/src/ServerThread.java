package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This class implements the thread that waits for incoming jobs and gives them to executorPool.
 * Also captures the state of the system before the job enter's the system.
 * @author Ayman Zahir
 */
public class ServerThread implements Runnable {

	private int state;
	
	private int state_before_entering;

	private DatagramSocket datagramSocket;

	private byte[] buffer;

	private DatagramPacket packet;
	private static final Logger log = LoggerFactory.getLogger(ServerThread.class);


	public ServerThread(int port) {

		try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.state = Server.RUN;

	}

	// @Override
	public void run() {
		System.out.println("Server running...");
		while (this.state == Server.RUN) {

			buffer = new byte[128];
			packet = new DatagramPacket(buffer, buffer.length);
			/* waiting for incoming jobs */
			try {
				datagramSocket.receive(packet);

			} catch (IOException e) {
				e.printStackTrace();
			}
			int packetLen = packet.getLength();
			int offSet = packet.getOffset();
			byte[] packetContents = new byte[packetLen];
			System.arraycopy(buffer, offSet, packetContents, 0, packetLen);

			String content = new String(packetContents);
			int n = Integer.parseInt(content);
			System.out.println("Packet Received: " + content);
			Job currentJob = new Job(System.nanoTime(), n);

			state_before_entering = WorkerThreadPool.executorPool.getActiveCount() + WorkerThreadPool.executorPool.getQueue().size();


			if (state_before_entering >= Server.highest_state) {
				Server.highest_state = state_before_entering;
			}
			if(state_before_entering<=1000){
				Server.hmap.putIfAbsent(state_before_entering,0);
				Server.hmap.compute(state_before_entering, (k, v) -> v + 1);

			}else
				System.out.println("OverFlow");

			WorkerThreadPool.executorPool.execute(currentJob);

//			long time = System.nanoTime();
//			Server.enterSystem.add(time);
			Server.counter++;

			//check the cpu id used by thread
			int cpuCore = CpuCoreID.CLibrary.INSTANCE.sched_getcpu();
			log.info("Thread CPU Core: " + cpuCore);
		}
	}
}

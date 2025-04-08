package Server;
/**
 * Listens for Reset packets. Packages each packet into Resetjob class and add's to the executorPool's queue.
 * Sets the log flag to true or false based on the time of it's arrival.
 * @author Ayman Zahir
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class ResetListener implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ResetListener.class);
	private DatagramSocket resetSocket;
	private int resetPort = 9950;
	//private int resetPort = 9920;
	private byte[] resetBuffer;
	private DatagramPacket resetPacket;
	private int state;
	//private String ipaddr = "10.0.0.2";
	private String ipaddr = "127.0.0.1";

	/*
	*public ResetListener() {
			try {
				resetSocket = new DatagramSocket(resetPort);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			this.state = Server.RUN;
		}
	*
	* */
	public ResetListener() {
		try {
			resetSocket = new DatagramSocket(resetPort, InetAddress.getByName(ipaddr));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
            throw new RuntimeException("on server address IP "+ e);
        }
        this.state = Server.RUN;
	}

	public void run() {
		System.out.println("ResetListener running...");

		while (this.state == Server.RUN) {
			System.out.println(ipaddr);
			resetBuffer = new byte[128];
			resetPacket = new DatagramPacket(resetBuffer, resetBuffer.length);

			try {
				resetSocket.receive(resetPacket);
				//System.out.println("packet recieved");
			} catch (IOException e) {
				e.printStackTrace();
			}
			int packetLen = resetPacket.getLength();
			int offSet = resetPacket.getOffset();
			byte[] packetContents = new byte[packetLen];
			System.arraycopy(resetBuffer, offSet, packetContents, 0, packetLen);
			String content = new String(packetContents);
			//System.out.println("Packet Received in ResetListener: " + content);

			if (content.equals("RESET1")) {
				System.out.println("RESET JOB 1 ADDED in the start of the queue");
//				Server.enterSystem.add((long) 0);
				WorkerThreadPool.executorPool.execute(new ResetJob(System.nanoTime(), 0, true));
				int i=0;
				while(WorkerThreadPool.executorPool == null && i<10){
					log.info("WorkerThreadPool is null");
					WorkerThreadPool.executorPool.execute(new ResetJob(System.nanoTime(), 0, true));
					i++;
				}

			} else {

				System.out.println("RESET JOB 2 ADDED in the end of the queue");
//				Server.enterSystem.add((long) 0);
				WorkerThreadPool.executorPool.execute(new ResetJob(System.nanoTime(), 0, false));
				int i=0;
				while(WorkerThreadPool.executorPool == null && i<10){
					log.info("WorkerThreadPool is null");
					WorkerThreadPool.executorPool.execute(new ResetJob(System.nanoTime(), 0, false));
					i++;
				}
			}

		}

		}



}


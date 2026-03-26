package Server;

/**
 * This class calculates both measured and modeled metric's such as SteadyStateProbabilities,
 * MeanResponseTime, Power and Utilization etc. Also store's them into files.
 * @author zahzag
 */
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class Metrics {

	public static double MeanRspTime=0;
	public static double MeanSrvcTime=0;
	public static double MeanPacketLength=0;
	public static long RspTime=0;
	public static long SrvcTime=0;
	public static long PacketLength=0;
	public static double p_idle=0;
	public static double p_loaded=0;
	public static long job_count=0;
	public static long pool_job_count;
	public static double measured_lambda=0;
	public static double mean_service_rate=0;
	public static double mean_response_time=0;
	public static double utilization=0;
	public static double U=0;
	public static double mean_service_time=0;
	public static int state_total = 0;
	public static double k = 0;
	public static double MRT = 0;
	public static long cpuTime=0;
	public static double MeanCpuTime=0;
	public static double Mes_interArrivaleTime=0;
	public static double MeanMes_interArrivaleTime=0;
	public static double MeanWaintingTime=0;
	public static double waitingTime=0;


	public static void calculate() {
		// new text file
		FileWriter writer;
		File file;
		file = new File("SteadyStateProbability.txt");
		String filepath = "workbook.xlsx";
		Workbook wb = null;
		try {
			if (new File(filepath).exists()) {
				System.out.println("excel found");
                try {
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try (InputStream excelFile = new FileInputStream(filepath)) {
					wb = new XSSFWorkbook(excelFile);
				}
			} else {
				System.out.println("excel not found");
				wb = new XSSFWorkbook();
			}
		} catch (IOException e) {
			e.printStackTrace();
			wb = new XSSFWorkbook(); // Fallback
		}

		Sheet sheet;

		if (wb.getSheet("Test_Sheet") != null) {
			sheet = wb.getSheet("Test_Sheet");
		} else {
			sheet = wb.createSheet("Test_Sheet");
			Row rowTitles = sheet.createRow(0);

			rowTitles.createCell(0).setCellValue("CPU Utilization [%]");
			rowTitles.createCell(1).setCellValue("Sampling Rate [\u00B5s]");
			rowTitles.createCell(2).setCellValue("Threshold [%]");
			rowTitles.createCell(3).setCellValue("Modeled Arrival Rate [jobs/s]");
			rowTitles.createCell(4).setCellValue("Measured Arrival Rate [jobs/s]");
			rowTitles.createCell(5).setCellValue("Measured Mean Service Time [ms]");
			rowTitles.createCell(6).setCellValue("Measured Mean Service Rate [jobs/s]");
			rowTitles.createCell(7).setCellValue("Modeled MRT (k/\u03BB) [ms]");
			rowTitles.createCell(8).setCellValue("Modeled MRT (W+S) [ms]");
			rowTitles.createCell(9).setCellValue("Measured MRT [ms]");
			rowTitles.createCell(10).setCellValue("Measured Mean Interarrival Time [ms]");
			rowTitles.createCell(11).setCellValue("Modeled Utilization [%]");
			rowTitles.createCell(12).setCellValue("Modeled Busy Probability (1-\u03C0\u2080)");
			rowTitles.createCell(13).setCellValue("Measured Utilization [%]");
			rowTitles.createCell(14).setCellValue("Highest State");
			rowTitles.createCell(15).setCellValue("Measured Mean CPU Time [ms]");
			rowTitles.createCell(16).setCellValue("Measured Mean CORE Frequency [KHz]");
			rowTitles.createCell(17).setCellValue("Modeled CORE Power [W]");
			rowTitles.createCell(18).setCellValue("Measured CORE Power [W]");
			
		}
		// Time needed to create Excel file.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			System.out.println("Job Counter in Metrics class : " + Server.counter);
			if (Server.counter > 0) {
				calculateData();
				modelled_data();

				// Prints the State and the number of times it occurred. And
				// calculates the sum of all occurrences.
				for (int i = 0; i <= Server.highest_state; i++) {
					state_total = Server.hmap.get(i) + state_total;
				}
				System.out.println("Highest State reach by the system: " + Server.highest_state);

				// Calculates Steady State Probability for each state.
				for (int j = 0; j <= Server.highest_state; j++) {
                    k= (j*((double) (Server.hmap.get(j)) / state_total))+k;

				}

				System.out.println("Mean Number of jobs K: "+k);
				MRT = (k/measured_lambda)*1000;
				System.out.println("Modelled MRT(k/lambda) : " + MRT);

				U = 100-(((double) (Server.hmap.get(0)) / state_total) * 100);
				System.out.println("Modelled Utilization(1-Pi_0) : " + U);

				// write Steady State Probabilities in text file
				writer = new FileWriter(file, true);
				for (int j = 0; j <= Server.highest_state; j++) {
					writer.write("Pi_" + j + ": " + ((double) (Server.hmap.get(j)) / state_total) * 100);
					writer.write(System.getProperty("line.separator"));
				}
				writer.write("Utilization (1-Pi_" + 0 + ") : " +  U);
				writer.write("\n-------------------------------------------------------------------");

				writer.flush();
				writer.close();

				// write metric's in excel file
				int rowNumber = 1;
				boolean test = true;

				while (test) {

					Row row = sheet.getRow(rowNumber);

					if (row == null) {
						row = sheet.createRow(rowNumber);
						row.createCell(3).setCellValue(Server.lambda);
						row.createCell(4).setCellValue(measured_lambda);
						row.createCell(5).setCellValue(MeanSrvcTime/1000000);
						row.createCell(6).setCellValue(mean_service_rate);
						row.createCell(7).setCellValue(MRT);
						row.createCell(8).setCellValue((MeanWaintingTime+MeanSrvcTime)/1000000);
						row.createCell(9).setCellValue(MeanRspTime/1000000);
						row.createCell(10).setCellValue(MeanMes_interArrivaleTime);
						row.createCell(11).setCellValue(utilization);
						row.createCell(12).setCellValue(U);
						row.createCell(14).setCellValue(Server.highest_state);
						row.createCell(15).setCellValue(MeanCpuTime/1000000);


						test = false;

					} else {

						rowNumber++;
					}
				}

				FileOutputStream fileOut = new FileOutputStream("workbook.xlsx");
				wb.write(fileOut);
				fileOut.close();
				System.out.println("Exiting program...");

			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static String executeCommand(String command) {
		StringBuilder output = new StringBuilder();

		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("bash", "-c", command);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

			process.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return output.toString();
	}
	private static void calculateData() {

		JobData data = null;
		boolean empty = false;
		int jobConter=0;
		while (!empty) {

			if (Server.jobDataQueue.peek() != null) {
				try {
					data = Server.jobDataQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				RspTime = RspTime + data.getResponseTime();// nanoseconds
				SrvcTime = SrvcTime + data.getCalcTime();// nanoseconds
				PacketLength = PacketLength + data.getPacketLength();
				cpuTime = cpuTime + data.getCpuTime();//nanosecands
				if(jobConter>0) {
					Mes_interArrivaleTime = Mes_interArrivaleTime + data.getInterArrivaleTime();
					System.out.println(data.getInterArrivaleTime());
					waitingTime = waitingTime+data.getWaintingTime();
				}
				jobConter=jobConter+1;
			} else {
				MeanRspTime = (double)(RspTime / Server.counter);
				MeanSrvcTime = (double)(SrvcTime/ Server.counter);//the measured MST
				MeanPacketLength = (double)(PacketLength / Server.counter);
				MeanCpuTime = (double)(cpuTime/Server.counter);
				MeanMes_interArrivaleTime=(double)(Mes_interArrivaleTime/Server.counter); //nano seconds
				MeanMes_interArrivaleTime=MeanMes_interArrivaleTime/1000000000; // seconds
				MeanWaintingTime=(double)waitingTime/Server.counter;
				empty = true;
			}
		}


	}

	private static void modelled_data() {

		job_count = Server.counter;
		System.out.println("No. of Jobs Served : " + job_count);
		System.out.println("Modelled Lambda : " + Server.lambda);
		measured_lambda = job_count/600.0;//set according to the duration of the test run otherwise calculations will be wrong : 600.0 // 1800 is 30 minutes
		System.out.println("Measured Lambda : " + measured_lambda);
		mean_service_time = MeanSrvcTime/1000000;//Nanosecond to Milliseconds
		System.out.println("Measured Mean Service Time : " + mean_service_time + " Milliseconds");
		mean_service_rate = 1000000000 / MeanSrvcTime;//Milliseconds to seconds
		System.out.println("Modelled Service rate in Seconds: " + mean_service_rate + " Jobs/sec");
		mean_response_time = (1/mean_service_rate)/(1-(measured_lambda/mean_service_rate))*1000;
		System.out.println("Modelled Mean Response Time : "+ mean_response_time + " Miliseconds");
		System.out.println("Measured Mean Response Time: " + MeanRspTime/1000000 + " Miliseconds");
		utilization = (measured_lambda / mean_service_rate) * 100;
		System.out.println("Modelled Utilization : " + utilization);
		System.out.println("Server counter : " + Server.counter);



		System.out.println("Measured cpu time "+ MeanCpuTime/1_000_000.0);
}
}

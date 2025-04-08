package Server;

/**
 * This class calculates both measured and modeled metric's such as SteadyStateProbabilities,
 * MeanResponseTime, Power and Utilization etc. Also store's them into files.
 * @author Ayman zahir
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

	public static double MeanRspTime;
	public static double MeanSrvcTime;
	public static double MeanPacketLength;
	public static long RspTime;
	public static long SrvcTime;
	public static long PacketLength;
	public static double p_idle;
	public static double p_loaded;
	public static long job_count;
	public static long pool_job_count;
	public static double measured_lambda;
	public static double mean_service_rate;
	public static double mean_response_time;
	public static double m_power;
	public static double utilization;
	public static double U;
	public static double mean_service_time;
	public static int state_total = 0;
	public static double k = 0;
	public static double MRT = 0;
	public static long cpuTime;
	public static double MeanCpuTime;
//	public static double Service_Time_pool_avg;
//	public static double MRT_pool_avg;


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

			rowTitles.createCell(0).setCellValue("Confidence Metrics");
			rowTitles.createCell(1).setCellValue("Mod. Lambda");
			rowTitles.createCell(2).setCellValue("Mes. Lambda");
			rowTitles.createCell(3).setCellValue("Mes. MST");
			rowTitles.createCell(4).setCellValue("Mes. MSR");
			rowTitles.createCell(5).setCellValue("Mod. MRT");
			rowTitles.createCell(6).setCellValue("MRT=k/lambda");
			rowTitles.createCell(7).setCellValue("Mes. MRT");
			rowTitles.createCell(8).setCellValue("Mod. Power");
			rowTitles.createCell(9).setCellValue("Mes. Power");
			rowTitles.createCell(10).setCellValue("Mod. U");
			rowTitles.createCell(11).setCellValue("1-Pi_0");
			rowTitles.createCell(12).setCellValue("Mes. U");
			rowTitles.createCell(13).setCellValue("Avg. Freq");
			rowTitles.createCell(14).setCellValue("State");
			rowTitles.createCell(15).setCellValue("Mes MCpuTime");
			rowTitles.createCell(16).setCellValue("Mes frequency");
			rowTitles.createCell(17).setCellValue("Mes Average Power");
			rowTitles.createCell(18).setCellValue("Mes Total Power");
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
					//System.out.println("No. of times in state " + i + ": " + Server.hmap.get(i));
					state_total = Server.hmap.get(i) + state_total;
				}
				//System.out.println("Total frequency of all states: " + state_total);
				System.out.println("Highest State reach by the system: " + Server.highest_state);

				// Calculates Steady State Probability for each state.
				for (int j = 0; j <= Server.highest_state; j++) {
					//System.out.println("Pi_" + j + ": " + ((double) (Server.hmap.get(j)) / state_total) * 100);

						k= (j*((double) (Server.hmap.get(j)) / state_total))+k;

				}
//				Job.logger.info("higher stat : "+Server.highest_state);
//				Job.logger.info("stat total : "+state_total);
//				Job.logger.info("k : "+k);
//				Job.logger.info(""+Server.hmap);
				System.out.println("Mean Number of jobs K: "+k);
				MRT = (k/measured_lambda)*1000;
				System.out.println("Modelled MRT(k/lambda) : " + MRT);

				U = 100-(((double) (Server.hmap.get(0)) / state_total) * 100);
				System.out.println("Modelled Utilization(1-Pi_0) : " + U);
/*				pool_job_count = WorkerThreadPool.executorPool.getCompletedTaskCount();
				System.out.println("PoolTaskCount : " + (pool_job_count-1));*/
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
						row.createCell(1).setCellValue(Server.lambda);
						row.createCell(2).setCellValue(measured_lambda);
						row.createCell(3).setCellValue(MeanSrvcTime/1000000);
						row.createCell(4).setCellValue(mean_service_rate);
						row.createCell(5).setCellValue(mean_response_time);
						row.createCell(6).setCellValue(MRT);
						row.createCell(7).setCellValue(MeanRspTime/1000000);
						row.createCell(8).setCellValue(m_power);
						row.createCell(10).setCellValue(utilization);
						row.createCell(11).setCellValue(U);
						row.createCell(14).setCellValue(Server.highest_state);
						row.createCell(15).setCellValue(MeanCpuTime/1000000);
						//row.createCell(16).setCellValue(Server.highest_state);

						test = false;

					} else {

						rowNumber++;
					}
				}

				FileOutputStream fileOut = new FileOutputStream("workbook.xlsx");
				wb.write(fileOut);
				fileOut.close();
				//wb.close();
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

			} else {
				MeanRspTime = (double)(RspTime / Server.counter);
				MeanSrvcTime = (double)(SrvcTime/ Server.counter);
				MeanPacketLength = (double)(PacketLength / Server.counter);
				MeanCpuTime = (double)(cpuTime/Server.counter);
				empty = true;
			}
		}

/*		//POOL
		 List<Long> MRT_pool = new ArrayList<Long>();
		 List<Long> Service_Time_pool = new ArrayList<Long>();


		 System.out.println("Size of Server.leaveSystem.size():  "+Server.leaveSystem.size());
		 System.out.println("Size of Server.enterSystem.size():  "+Server.enterSystem.size());
		 System.out.println("Size of Server.enterExecution.size():  "+Server.enterExecution.size());

		for (int i=1; i<Server.enterSystem.size()-1; i++) {
			MRT_pool.add((Server.leaveSystem.get(i)-Server.enterSystem.get(i)));
			//System.out.println(" Mean Response Time : "+ (Server.leaveSystem.get(i)-Server.enterSystem.get(i)) + " Miliseconds");
			Service_Time_pool.add((Server.leaveSystem.get(i)-Server.enterExecution.get(i)));
			//System.out.println(" Mean Service Time : " + (Server.leaveSystem.get(i)-Server.enterExecution.get(i)) + " Milliseconds");
		}


		//System.out.println("Size of MRT_pool.size():  "+MRT_pool.size());
		//System.out.println("Size of Service_Time_pool.size():  "+Service_Time_pool.size());


		long MRT_sum=0;
		long Service_Time_pool_sum=0;
		for(int i=0;i<=MRT_pool.size()-1;i++){
			MRT_sum += MRT_pool.get(i);
			//System.out.println("SUM Mean Response Time : "+ MRT_sum + " Miliseconds");
		}
		for(int i=0;i<=Service_Time_pool.size()-1;i++){
			Service_Time_pool_sum += Service_Time_pool.get(i);
			//System.out.println("SUM Mean Service Time : " + Service_Time_pool_sum + " Milliseconds");
		}
		MRT_pool_avg = (double)(MRT_sum/Server.counter);
		Service_Time_pool_avg = (double)(Service_Time_pool_sum/Server.counter);*/


	}

	private static void modelled_data() {
		//Performance AMD
			//p_idle = 108.034;
			//p_loaded = 121.159;
		//Powersave AMD
			//p_idle = 107.33;
			//p_loaded = 112.07;
		//Ondemand AMD
			//p_idle = 77.33;
			//p_loaded = 121.53;
		//Performance INTEL
			//p_idle = 108.034;
			//p_loaded = 121.159;
		//Powersave INTEL
			//p_idle = 107.33;
			//p_loaded = 112.07;
		//Ondemand INTEL
			p_idle = 106.29;
			p_loaded = 117.28;

		job_count = Server.counter;
		System.out.println("No. of Jobs Served : " + job_count);
		System.out.println("Modelled Lambda : " + Server.lambda);
		measured_lambda = job_count/600.0;//set according to the duration of the test run otherwise calculations will be wrong : 600.0
		System.out.println("Measured Lambda : " + measured_lambda);
		mean_service_time = MeanSrvcTime/1000000;//Nanosecond to Milliseconds
		System.out.println("Measured Mean Service Time : " + mean_service_time + " Milliseconds");
		mean_service_rate = 1000000000 / MeanSrvcTime;//Milliseconds to seconds
		System.out.println("Modelled Service rate in Seconds: " + mean_service_rate + " Jobs/sec");
		mean_response_time = (1/mean_service_rate)/(1-(measured_lambda/mean_service_rate))*1000;
		System.out.println("Modelled Mean Response Time : "+ mean_response_time + " Miliseconds");
		System.out.println("Measured Mean Response Time: " + MeanRspTime/1000000 + " Miliseconds");
		m_power = ((1 - (measured_lambda / mean_service_rate)) * p_idle) + ((measured_lambda / mean_service_rate) * p_loaded);
		System.out.println("Modelled Power Consumption : " + m_power);
		utilization = (measured_lambda / mean_service_rate) * 100;
		System.out.println("Modelled Utilization : " + utilization);

		System.out.println("Measured cpu time "+ MeanCpuTime/1_000_000.0);
/*
		System.out.println("-----POOL VALUES--------");
		double mean_service_time_pool = Service_Time_pool_avg/1000000;//Milliseconds
		System.out.println("Measured Mean Service Time : " + mean_service_time_pool + " Milliseconds");
		double service_rate_pool = 1000000000 / Service_Time_pool_avg;//Milliseconds to seconds
		System.out.println("Modelled Service rate in Seconds: " + service_rate_pool + " Jobs/sec");
		double mean_response_time_pool = (1/service_rate_pool)/(1-(measured_lambda/service_rate_pool))*1000;
        System.out.println("Modelled Mean Response Time : "+ mean_response_time_pool + " Miliseconds");
		System.out.println("Measured Mean Response Time: " + MRT_pool_avg/1000000 + " Miliseconds");*/
	}
}

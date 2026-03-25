#!/bin.bash
data_dir="data/sampling_rate_rpi2"
mkdir data $data_dir
#change sampling rate for ondemand governor from 10ms to 100ms by 10ms each time
for ((sampling_rate=40000;sampling_rate<=100000;sampling_rate=sampling_rate+10000));do
	#change sampling rate for ondemand governor
	echo $sampling_rate | sudo tee /sys/devices/system/cpu/cpufreq/ondemand/sampling_rate
	mkdir "$data_dir"/"$sampling_rate"
	for ((treshold=75;treshold<=95;treshold=treshold+5));do

		echo "Simulation delta : $sampling_rate and mu : $treshold "
		#change the treshold for ondemand governor to swith to higher frequency
		echo $treshold | sudo tee /sys/devices/system/cpu/cpufreq/ondemand/up_threshold
		mkdir "$data_dir"/"$sampling_rate"/treshold "$data_dir"/"$sampling_rate"/treshold/$treshold
		#run the simulation
		echo "run_ondemand"
		nohup sudo bash run_ondemand.sh 3 10 600000 2400000 "../$data_dir/$sampling_rate/treshold/$treshold" &
		simulationID=$!
		wait $simulationID
		cd build/
		#move collected files to specific treshold
		mv workbook.xlsx ../"$data_dir/$sampling_rate/treshold/$treshold"  #mv core_frequency.log  core_power.log  cpu3.log  nohup.out  power_log.txt  Repeat.csv  SteadyStateProbability.txt  workbook.xlsx ../data/sampling_rate3/"$sampling_rate"/treshold/$treshold
		cd ../
		mv nohup.out "$data_dir/$sampling_rate/treshold/$treshold"
		#remove log data  
		#rm  nohup.out
	done
	echo "============================"

done

#!/bin/bash	
source scripts/helpers.sh
cpu=$1
runs=$2
min_freq=$3
max_freq=$4
governor="ondemand"


#ondemand ranges
#incrementer=300000

#for ((freq=$min_freq;freq<=$max_freq;freq=freq+incrementer));do

    #ondemand ranges : to take the last rand 1.7Ghz -> 2.1Ghz
 #   if [[ $freq == 1700000 ]]; then
  #    incrementer=400000
   # fi

  #set cpu freq and governor
 #ondemande full
 configure_cpu_performance $cpu $governor $min_freq $max_freq
 #ondemande ranges
 #configure_cpu_performance $cpu $governor $freq $((freq+incrementer))

  #for lamda in $(seq 1 1 3); do #lamda 1 , 4 and 7
  #ondemand ranges
  #read increased_lamda max_lamda <<< $(increase_lamda $((freq+incrementer)) )
  #ondemand full
  read increased_lamda max_lamda <<< $(increase_lamda $((max_freq)) )

  for lamda in $(seq -f "%.2f" $increased_lamda $increased_lamda $max_lamda); do
  #kill any java process to be sure
  sudo kill -9 $(pgrep java)

        # run the tests $runs time
        for ((i = 1 ; i <= $runs; i++)); do
          #assign ip addr to server
         sudo ip addr add 10.0.0.2/24 dev enp0s31f6
          cd build
          export CLASSPATH="../Server/Server/lib/*:."

          printf "running server... frequency : $freq -> lamda: $lamda -> test : $i  \n"
          rm -f cpu3.log

              #measuring core power
              rm -f core_power.log
              sudo turbostat --cpu 3 --interval 1 --show CorWatt --quiet --Summary -o core_power.log &
              sleep 1s
              TURBOSTAT_PID=$!  # Capture turbostat process ID
              echo "Started measuring CPU core power (PID: $TURBOSTAT_PID) \n"

              #mesuring average frequency ""turbostat"
              rm -f core_frequency.log
              #sudo turbostat --cpu 3 --interval 1 --show Avg_MHz --quiet --Summary -o core_frequency.log &
              #sleep 1s
              #TURBOSTAT_Freq_PID=$!  # Capture turbostat process ID
              #echo "Started measuring CPU core frequency (PID: $TURBOSTAT_Freq_PID) \n"

              #measuring average frequency ""manually""
              while true ; do
              	cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq >> core_frequency.log
              	sleep 1
              done &
              MANUAL_FREQ_PID=$!
              echo "Started measuring CPU core frequency (PID: $MANUAL_FREQ_PID) \n"

              #powerstat
              rm -f power_log.txt
              sudo powerstat -fz 1 > power_log.txt &
              POWER_STATE_ID=$!

          java Server.Server $lamda &
          Java_PID=$!

          taskset -c 8 java client.LoadGenerator $lamda 600000 1000000  & # lamda  ; duration = 600000 ; repeat

          sleep 7s # wait until client start sending requests (7 secondes)

          # Run mpstat in the background, redirecting its output to the temporary file # Create a temporary file to store the output
          mpstat -P 3 1 >> cpu3.log &
          Mpstat_PID=$!

          wait $Java_PID

              #Stop turbostat after Java program finishes
              kill $TURBOSTAT_PID
              #kill $TURBOSTAT_Freq_PID
              kill -9 $MANUAL_FREQ_PID

              sleep 1  # Allow turbostat to finish writing

          kill -SIGINT $Mpstat_PID
          wait "$Mpstat_PID" 2>/dev/null  # Ensure it fully stops

              # Compute total energy consumed by CPU core
              TOTAL_ENERGY=0
              COUNT=0

              while read PWR; do

                 TOTAL_ENERGY=$(echo "$TOTAL_ENERGY + $PWR" | bc)  # Sum power over time
                 COUNT=$((COUNT + 1))

              done < <(tail -n +2 core_power.log)  # Process file in a subshell

              # Compute total energy in Joules (J)
              TOTAL_ENERGY=$(echo "$TOTAL_ENERGY * 1" | bc)
              AVG_CORE_POWER=$(echo "$TOTAL_ENERGY / $COUNT" | bc -l)

              # compute average frequency used by CPU core ""turbostat""
              #TOTAL_FREQ=0
              #COUNT=0
              
              #while read FRQ; do

               #  TOTAL_FREQ=$(echo "$TOTAL_FREQ + $FRQ" | bc)  # Sum freq over time
               #  COUNT=$((COUNT + 1))

              #done < <(tail -n +2 core_frequency.log)  # Process file in a subshell
               # Compute total freq and avg
              #TOTAL_FREQ=$(echo "$TOTAL_FREQ * 1000_000" | bc) # from Mhz to Hz
              #AVG_CORE_FREQ=$(echo "$TOTAL_FREQ / $COUNT" | bc -l)

              AVG_CORE_FREQ=$(awk '{sum+=$1} END {if (NR > 0) print sum/NR}' core_frequency.log) #freq with KHz

              #powerstat
              kill $POWER_STATE_ID
              avg_freq=$(awk '/GHz/ {sum+=$14; count++} END {if (count > 0) print sum/count * 1000_000}' power_log.txt) # convert from Ghz to Khz

              #add the mesured power to excel file
              python3 ../scripts/add_to_excel.py "workbook.xlsx" 18 $AVG_CORE_POWER
              python3 ../scripts/add_to_excel.py "workbook.xlsx" 19 $TOTAL_ENERGY

          #----------------------------------------------------------------------
          #add the mesured frequency to excel file
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 17 $AVG_CORE_FREQ
          #average freq powerstat
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 20 $avg_freq

          #----------------------------------------------------------------------
          #add measured utillization cpu3.log

          #utilization=$(awk '/Average/ && $3 != "%usr" {print $3}' "cpu3.log")
          utilization=$(tail -n 5 "cpu3.log" | awk '/Average/ && $3 != "%usr" {print $3; exit}')
          printf "utilization : $utilization \n"
          #add measured utillization to excel file
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 13 "$utilization"
          #----------------------------------------------------------------------

          printf "system sleeping for 2 min \n"
          sleep 2m
        done
	
         # to avoid overflow , if the uilization exceeds 99.5% , then don't increse lamda more
        # float_utilization=$(echo "$utilization" | awk '{print $1 + 0}')
        # if (( $(echo "$float_utilization >= 99.5" | bc -l))); then
         #   break #avoid lamda increasing and go to the next frequency
         #fi
#  done
done
# shut down the server
#sudo shutdown now

printf " \n\n "





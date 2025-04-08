#!/bin/bash	
source scripts/helpers.sh
cpu=$1
runs=$2
min_freq=$3
max_freq=$4
governor="userspace"

for ((freq=$min_freq;freq<=$max_freq;freq=freq+100000));do

# run the tests $runs time
  configure_cpu_performance $cpu $governor $freq $freq
  read increased_lamda max_lamda <<< $(increase_lamda $freq )
  #for lamda in $(seq 1 1 3); do #lamda 1 , 4 and 7
  for lamda in $(seq -f "%.2f" $incresed_lamda $increased_lamda $max_lamda); do
  #kill any java process to be sure
  sudo kill -9 $(pgrep java)

        for ((i = 1 ; i <= $runs; i++)); do
          #assign ip addr to server
          sudo ip addr add 10.0.0.2/24 dev enp0s31f6
          cd build
          export CLASSPATH="../Server/Server/lib/*:."

          printf "running server... frequency : $freq -> lamda: $lamda -> test : $i  \n"
          rm -f cpu3.log
              rm -f core_power.log
              sudo turbostat --cpu 3 --interval 1 --show CorWatt --quiet --Summary -o core_power.log &
              sleep 1s
              TURBOSTAT_PID=$!  # Capture turbostat process ID
              echo "Started measuring CPU core power (PID: $TURBOSTAT_PID) \n"

          java Server.Server $lamda &
          Java_PID=$!

          taskset -c 8 java client.LoadGenerator $lamda 600000 1000000  & # lamda  ; duration = 600000 ; repeat

          sleep 7s # wait until client start sending requests (7 secondes)

          # Run mpstat in the background, redirecting its output to the temporary file # Create a temporary file to store the output
          mpstat -P 3 1 >> cpu3.log &
          Mpstat_PID=$!

          wait $Java_PID

               # Stop turbostat after Java program finishes
              kill $TURBOSTAT_PID
              sleep 1  # Allow turbostat to finish writing

          kill -SIGINT $Mpstat_PID
          wait "$Mpstat_PID" 2>/dev/null  # Ensure it fully stops

              # Compute total energy consumed by CPU cores
              TOTAL_ENERGY=0
              COUNT=0
              while read PWR; do

                 TOTAL_ENERGY=$(echo "$TOTAL_ENERGY + $PWR" | bc)  # Sum power over time
                 COUNT=$((COUNT + 1))

              done < <(tail -n +2 core_power.log)  # Process file in a subshell

              # Compute total energy in Joules (J)
              TOTAL_ENERGY=$(echo "$TOTAL_ENERGY * 1" | bc)
              AVG_CORE_POWER=$(echo "$TOTAL_ENERGY / $COUNT" | bc -l)
              #add the mesured power to excel file
              python3 ../scripts/add_to_excel.py "workbook.xlsx" 18 $AVG_CORE_POWER
              python3 ../scripts/add_to_excel.py "workbook.xlsx" 19 $TOTAL_ENERGY

          #----------------------------------------------------------------------
          #add the mesured frequency to excel file
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 17 $freq
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
  done

done
# shut down the server
#sudo shutdown now

printf " \n\n "





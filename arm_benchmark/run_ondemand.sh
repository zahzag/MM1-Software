#!/bin/bash	
source scripts/helpers.sh
source scripts/fast_power_sampler.sh

cpu=$1
runs=$2
min_freq=$3
max_freq=$4
governor="ondemand"
CAPACITANCE_COEFF="0.0000011479"    # Effective capacitance (F) - tuned for RPi
data_dir=${5:-"data/sampling_rate_rpi2/10000/treshold/95"} # default data directory if not provided as argument
number_cores=$(($(nproc)+1)) # default to 4 if not provided as argument ; +1 represent the idle core since nproc don't count it
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
  #just to start from lamda = 5.16


  # Check if processes exist first, then kill
  pgrep java >/dev/null && sudo kill -9 $(pgrep java) || echo "No Java processes found"

        # run the tests $runs time
        for ((i = 1 ; i <= $runs; i++)); do
          #assign ip addr to server
         sudo ip addr add 10.0.0.2/24 dev eth0 >/dev/null || echo "adress already assigned" 
          cd build
          export CLASSPATH="../Server/Server/lib/*:."

          printf "running server... frequency : $freq -> lamda: "$lamda" -> test : $i  \n"

        #measuring core power - turbostat
		      #rm -f core_power.log
		       #core 3
          #  sudo turbostat --cpu 3 --interval 1 --show CorWatt --quiet --Summary -o turbostat_core_power.log &
          #   sleep 1s
          #   TURBOSTAT_PID=$!  # Capture turbostat process ID
          #   echo "Started measuring CPU core power (PID: $TURBOSTAT_PID) \n"
	     #measuring core power -powerstat
          # echo "Starting powerstat monitoring..."
          # sudo powerstat -R $POWERSTAT_INTERVAL $DURATION_SEC > "core_power.log" 2>&1 &
          # POWERSTAT_PID=$!
          # sleep 1s
	     #measuring core power - RAPL

              #mesuring average frequency ""turbostat"
              #sudo turbostat --cpu 3 --interval 1 --show Avg_MHz --quiet --Summary -o core_frequency.log &
              #sleep 1s
              #TURBOSTAT_Freq_PID=$!  # Capture turbostat process ID
              #echo "Started measuring CPU core frequency (PID: $TURBOSTAT_Freq_PID) \n"

              #measuring average frequency ""manually""
              mpstat -P ALL 1 > cpu_utilization_"$lamda"_$i.log & 
              MPSTAT_CPU_PID=$!
              while true ; do
                timestamp_full=$(date '+%Y-%m-%d %H:%M:%S')
                freq=$(cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq)
                voltage=$(vcgencmd measure_volts core | sed 's/[^0-9.]//g')
                echo "$timestamp_full,$freq,$voltage" >> core_freq_voltage_"$lamda"_$i.log
                # cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq >> core_frequency.log
                    
                    sleep 0.1
              done &
              MANUAL_FREQ_PID=$!
              echo "Started measuring CPU core frequency (PID: $MANUAL_FREQ_PID) \n"

              #powerstat
              sudo powerstat -fz 1 > cpu_freq_powerstat_"$lamda"_$i.log &
              POWER_STATE_ID=$!

          java Server.Server "$lamda" &
          Java_PID=$!

          taskset -c 2 java client.LoadGenerator "$lamda" 600000 1000000  & # lamda  ; duration = 600000 ; repeat
          # bash ../scripts/fast_power_sampler.sh start 600 10 3

          sleep 7s # wait until client start sending requests (7 secondes)

          # Run mpstat in the background, redirecting its output to the temporary file # Create a temporary file to store the output
          mpstat -P 3 1 >> core_utilization_"$lamda"_$i.log &
          Mpstat_PID=$!

         wait $Java_PID
        #  bash ../scripts/fast_power_sampler.sh stop "workbook.xlsx" "19" "20" "21" "22"         
              #Stop turbostat after Java program finishes
              kill $TURBOSTAT_PID
              kill $POWERSTAT_PID
              #kill $TURBOSTAT_Freq_PID
              kill -9 $MANUAL_FREQ_PID
              
              sleep 1  # Allow turbostat to finish writing
         
          kill -SIGINT $MPSTAT_CPU_PID
          wait "$MPSTAT_CPU_PID" 2>/dev/null  # Ensure it fully stops

          kill -SIGINT $Mpstat_PID
          wait "$Mpstat_PID" 2>/dev/null  # Ensure it fully stops
          #----------------------------------------------------------------------
          #add measured utillization core_utilization_"$lamda"_$i.log

          #utilization=$(awk '/Average/ && $3 != "%usr" {print $3}' "core_utilization_"$lamda"_$i.log")
          utilization=$(tail -n 5 "core_utilization_"$lamda"_$i.log" | awk '/Average/ && $3 != "%usr" {print $3; exit}')
          #add measured utillization to excel file
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 14 "$utilization"
          #----------------------------------------------------------------------
              # Compute total energy consumed by CPU core
               TURBOSTAT_TOTAL_ENERGY=0
               TURBOSTAT_COUNT=0
               POWERSTAT_TOTAL_ENERGY=0
               POWERSTAT_COUNT=0
               
              #turbostate
		      # while read PWR; do

		      #   TURBOSTAT_TOTAL_ENERGY=$(echo "$TURBOSTAT_TOTAL_ENERGY + $PWR" | bc)  # Sum power over time
		      #    TURBOSTAT_COUNT=$((TURBOSTAT_COUNT + 1))

		      # done < <(tail -n +2 turbostat_core_power.log)  # Process file in a subshell

	      # Compute total energy in Joules (J)
              # TURBOSTAT_TOTAL_ENERGY=$(echo "$TURBOSTAT_TOTAL_ENERGY * 1" | bc)
              # TURBOSTAT_AVG_CORE_POWER=$(echo "$TURBOSTAT_TOTAL_ENERGY / $TURBOSTAT_COUNT" | bc -l)

	      #powerstat
              # read POWERSTAT_COUNT POWERSTAT_TOTAL_ENERGY <<< $(awk '/^[0-9]{2}:[0-9]{2}:[0-9]{2}/ {sum += $NF; count++} END {print count, sum}' core_power.log)

              # # Compute total energy in Joules (J)
              # POWERSTAT_TOTAL_ENERGY=$(echo "$POWERSTAT_TOTAL_ENERGY * 1" | bc)
              # POWERSTAT_AVG_CORE_POWER=$(echo "$POWERSTAT_TOTAL_ENERGY / $POWERSTAT_COUNT" | bc -l)

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

              # AVG_CORE_FREQ=$(awk '{sum+=$1} END {if (NR > 0) print sum/NR}' core_frequency.log) #freq with KHz
              # read AVG_CORE_FREQ AVG_CORE_VOLTAGE <<< $(awk -F, '{sum+=$2; sum2+=$3} END {if (NR>0) print sum/NR, sum2/NR; else print 0, 0}' core_freq_voltage_"$lamda"_$i.log )
              # NB_SAMPLES=$(wc -l < core_freq_voltage_"$lamda"_$i.log)
              # AVG_CORE_FREQ_DECIMAL=$(printf "%.0f" "$AVG_CORE_FREQ")
              # p_dynamic_core=$(echo "$AVG_CORE_VOLTAGE^2 * $AVG_CORE_FREQ_DECIMAL * $CAPACITANCE_COEFF" | bc -l) 
              # p_dynamic_core=$(printf "%.6f" "$p_dynamic_core")
              # energy_dynamic_core=$(echo "$p_dynamic_core*$NB_SAMPLES" | bc -l) # for number of samples seconds
              #=====modeled power for M/M/1 using idle power from linear interpolation and loaded power from f,v and coefficient 
              read AVG_CORE_FREQ AVG_CORE_VOLTAGE NB_SAMPLES<<< $(awk -F, '
              {
                  if (NF >= 3 && $2 > 0 && $3 > 0) {
                      sum += $2; 
                      sum2 += $3; 
                      count++
                  }
              } 
              END {
                  if (count > 0) 
                      printf "%.0f %.6f %.0f", sum/count, sum2/count , count; 
                  else 
                      print "0 0"
              }' core_freq_voltage_"$lamda"_$i.log)
                          
              # Validate inputs
              if [[ -z "$AVG_CORE_FREQ" || "$AVG_CORE_FREQ" == "0" ]]; then
                  echo "Error: Could not read frequency data"
                  exit 1
              fi

              if [[ -z "$AVG_CORE_VOLTAGE" || "$AVG_CORE_VOLTAGE" == "0" ]]; then
                  echo "Error: Could not read voltage data"
                  exit 1
              fi

              if [[ -z "$utilization" ]]; then
                  echo "Warning: Could not read utilization data, using 0%"
                  utilization="0.00"
              fi
                          
            echo "number of samples : $NB_SAMPLES"
            # Calculate idle power
            # #robert power 
            # cpu_p_idle=$(idle_power "$AVG_CORE_FREQ")
            # echo "Estimated Idle Power at ${AVG_CORE_FREQ} KHz: ${cpu_p_idle} W"
            ##ayman power
            cpu_p_idle_ayman=$(interpolate_cpu_idle_power "$AVG_CORE_FREQ")
            core_p_idle_ayman=$(echo "$cpu_p_idle_ayman / 4" | bc -l)

            # Calculate loaded power
            cpu_p_loaded=$(interpolate_cpu_loaded_power "$AVG_CORE_FREQ" )
            core_p_loaded=$(echo "$cpu_p_loaded / 4" | bc -l)
            echo "Estimated core Loaded Power at ${AVG_CORE_FREQ} kHz: ${core_p_loaded} W"

            cpu_utilization=$(tail -n 5 "cpu_utilization_"$lamda"_$i.log" | awk '/Average/ && $3 != "%usr" {print $3; exit}')

            # Calculate modeled power using M/M/1 formula
            if [[ -n "$utilization" && "$utilization" != "0.00" && "$utilization" != "" ]]; then
                #robert power
                rho=$(echo "scale=6; $utilization / 100" | bc -l)
                one_minus_rho=$(echo "scale=6; 1 - $rho" | bc -l)
                # modeled_power=$(echo "scale=6; $cpu_p_idle + $rho * $cpu_p_loaded" | bc -l)
                # modeled_energy=$(echo "$modeled_power * $NB_SAMPLES" | bc -l) # for number of samples seconds
                # printf "Whole CPU Modeled Power at %s Hz and %s%% Utilization: %s W\n" "$AVG_CORE_FREQ" "$cpu_utilization" "$modeled_power"
                # core_modeled_power=$(echo "scale=6; $modeled_power / $number_cores" | bc -l)
                # core_modeled_energy=$(echo "$modeled_energy / $number_cores" | bc -l) # for number of samples seconds

                ##ayman power
                modeled_power_ayman=$(echo "scale=6; $one_minus_rho*$core_p_idle_ayman + $rho*$core_p_loaded" | bc -l)
                modeled_energy_ayman=$(echo "$modeled_power_ayman * $NB_SAMPLES" | bc -l) # for number of samples seconds
                printf "Core Modeled Power (Ayman) at %s Hz and %s%% Utilization: %s W\n" "$AVG_CORE_FREQ" "$utilization" "$modeled_power_ayman"
                cpu_modeled_power_ayman=$(echo "scale=6; $modeled_power_ayman * $number_cores" | bc -l)
                cpu_modeled_energy_ayman=$(echo "$modeled_energy_ayman * $number_cores" | bc -l) # for number of samples seconds   
            else
                echo "Warning: No valid utilization data available"
            fi

                #robert 
            #   #add to excel file **modeled** power and energy for whole cpu
            #   python3 ../scripts/add_to_excel.py "workbook.xlsx" 19 $modeled_power
            #   python3 ../scripts/add_to_excel.py "workbook.xlsx" 20 $modeled_energy

             #add to excel file **modeled** power and energy for one core
            #   python3 ../scripts/add_to_excel.py "workbook.xlsx" 21 $core_modeled_power
            #   python3 ../scripts/add_to_excel.py "workbook.xlsx" 22 $core_modeled_energy
              
              
                #ayman
                #add to excel file **modeled** power and energy for one core
                python3 ../scripts/add_to_excel.py "workbook.xlsx" 23 $modeled_power_ayman
                python3 ../scripts/add_to_excel.py "workbook.xlsx" 24 $modeled_energy_ayman
                #add to excel file **modeled** power and energy for whole core
                python3 ../scripts/add_to_excel.py "workbook.xlsx" 26 $cpu_modeled_power_ayman
                python3 ../scripts/add_to_excel.py "workbook.xlsx" 27 $cpu_modeled_energy_ayman
	      # #add the mesured power to excel file turbostat
        #       python3 ../scripts/add_to_excel.py "workbook.xlsx" 21 $TURBOSTAT_AVG_CORE_POWER
        #       python3 ../scripts/add_to_excel.py "workbook.xlsx" 22 $TURBOSTAT_TOTAL_ENERGY

        #powerstat
        kill $POWER_STATE_ID
        avg_freq=$(awk '/GHz/ {sum+=$14; count++} END {if (count > 0) print sum/count * 1000_000}' cpu_freq_powerstat_"$lamda"_$i.log) # convert from Ghz to Khz

          #----------------------------------------------------------------------
          #add the mesured frequency to excel file
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 18 $AVG_CORE_FREQ
          #average freq powerstat
          python3 ../scripts/add_to_excel.py "workbook.xlsx" 15 $avg_freq


          mv core_utilization_"$lamda"_$i.log cpu_utilization_"$lamda"_$i.log cpu_freq_powerstat_"$lamda"_$i.log core_freq_voltage_"$lamda"_$i.log "$data_dir"
          mv Repeat.csv "$data_dir"/Repeat_"$lamda"_$i.csv
          mv SteadyStateProbability.txt "$data_dir"/SteadyStateProbability_"$lamda"_$i.txt
          printf "system sleeping for 1 min \n"
          sleep 1m
        #run done 
        done
	
         # to avoid overflow , if the uilization exceeds 99.5% , then don't increse lamda more
        # float_utilization=$(echo "$utilization" | awk '{print $1 + 0}')
        # if (( $(echo "$float_utilization >= 99.5" | bc -l))); then
         #   break #avoid lamda increasing and go to the next frequency
         #fi
#  lambda done
done
# shut down the server
#sudo shutdown now

printf " \n\n "





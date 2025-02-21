#!/bin/bash

run_system() {

  cd build

  export CLASSPATH="../Server/Server/lib/*:."

  java Server.Server &

  taskset -c 8 java client.LoadGenerator

}

configure_cpu_performance() {
  cpu="$1"
  frequency="$2"
  governor="userspace"

  echo "setting cpu : $cpu to frequency : $frequency "Hz" , governor : $governor "

  sudo cpupower --cpu "$cpu" frequency-set -g userspace -d $frequency -u $frequency
  sudo cpufreq-set --cpu "$cpu" -f $frequency
  #cpupower --cpu $cpu frequency-info
}

increase_lamda() {
# return the lamda increment and the max reached lamda 
case "$1" in 
	"2100000")
	echo "0.86 7.74";;
	"2000000")
	echo "0.82 7.42";;
	"1900000")
	echo "0.75 6.76";;
	"1800000")
	echo "0.71 6.36";;
	"1700000")
	echo "0.67 6.06" ;;
	"1600000")
	echo "0.62 5.60" ;;
	"1500000")
	echo "0.58 5.25" ;;
	"1400000")
	echo "0.55 4.93" ;;
	"1300000")
	echo "0.51 4.54";;
  "1200000")
  echo "0.49 4.43" ;;
  "1100000")
  echo "0.43 3.90" ;;
  "1000000")
  echo "0.38 3.42";;
  "900000")
  echo "0.35 3.19";;
  "800000")
  echo "0.32 2.86";;
	*)
	echo "unknown frequency" ;;
esac
}

compute_cpu_utilization() {
  local cpu="$1"
  local java_PID="$2"
  temp_file=$(mktemp)  # Create a temporary file for storing mpstat output

  # Start mpstat in the background and write to the temp file
  mpstat -P "$cpu" 1 > "$temp_file" &
  Mpstat_PID=$!

  echo "mpstat started with PID: $Mpstat_PID"

  # Wait for the Java process to finish
  wait "$java_PID"

  echo "Java process finished. Stopping mpstat..."

  # Gracefully stop mpstat
  kill "$Mpstat_PID"
  wait "$Mpstat_PID" 2>/dev/null  # Ensure it fully stops

  # Extract the average CPU utilization
  utilization=$(awk '/Average/ && $3 != "%usr" {print $3}' "$temp_file")

  # Save the utilization value to cpu3.log
  echo "$utilization" >> cpu3.log

  # Remove the temporary file
  rm "$temp_file"

  # Add the measured utilization to the Excel file
  python3 ../scripts/add_to_excel.py "workbook.xlsx" 13 "$utilization"

  echo "CPU utilization ($utilization%) saved to cpu3.log and workbook.xlsx"
}


update_excel_last_row() {
    local input_file="$1"
    local column_num="$2"
    local new_value="$3"
    local temp_csv="temp.csv"
    local temp_csv_new="temp_new.csv"
    local temp_output_file="temp_output.xlsx"
    local sheet_name="Test_Sheet"

    if ! command -v ssconvert &>/dev/null; then
        echo "Error: ssconvert is not installed. Install it with 'sudo apt-get install gnumeric'."
        return 1
    fi

    if ! command -v libreoffice &>/dev/null; then  # Check for libreoffice
        echo "Error: libreoffice is not installed. Install it (e.g., 'sudo apt-get install libreoffice')."
        return 1
    fi

    # ... (CSV conversion and update logic - same as before) ...

    # Convert back to Excel (using libreoffice for sheet name control)
    libreoffice --headless --convert-to xlsx --outdir . "$temp_csv_new"  # Convert to Excel

    # Rename to the correct output file
    mv "$temp_csv_new.xlsx" "$temp_output_file"

    # Overwrite original file
    mv "$temp_output_file" "$input_file"

    rm -f "$temp_csv" "$temp_csv_new" "$temp_output_file"

    echo "Updated column $column_num in the last row of '$input_file' with value '$new_value', sheet name is '$sheet_name'."
}

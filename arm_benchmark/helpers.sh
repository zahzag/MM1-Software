#!/bin/bash

set -euo pipefail

# Applies governor and frequency bounds for a selected CPU set.
configure_cpu_performance() {
  local cpu="$1"
  local governor="$2"
  local min_frequency="$3"
  local max_frequency="$4"

  echo "setting cpu: $cpu, governor: $governor, freq: $min_frequency -> $max_frequency"
  sudo cpupower --cpu "$cpu" frequency-set -g "$governor" -d "$min_frequency" -u "$max_frequency"
}

# Returns lambda sweep step and max lambda based on max CPU frequency.
increase_lamda() {
  case "$1" in
    2400000) echo "0.89 8.90" ;;
    2100000) echo "0.86 7.74" ;;
    2000000) echo "0.82 7.42" ;;
    1900000) echo "0.75 6.76" ;;
    1800000) echo "0.71 6.36" ;;
    1700000) echo "0.67 6.06" ;;
    1600000) echo "0.62 5.60" ;;
    1500000) echo "0.58 5.25" ;;
    1400000) echo "0.55 4.93" ;;
    1300000) echo "0.51 4.54" ;;
    1200000) echo "0.49 4.43" ;;
    1100000) echo "0.43 3.90" ;;
    1000000) echo "0.38 3.42" ;;
    900000)  echo "0.35 3.19" ;;
    800000)  echo "0.32 2.86" ;;
    700000)  echo "0.28 2.52" ;;
    600000)  echo "0.25 2.25" ;;
    *)
      echo "unknown frequency"
      return 1
      ;;
  esac
}

# Reads ARM core voltage from vcgencmd output.
read_cpu_voltage_vcgencmd() {
  local voltage
  voltage=$(vcgencmd measure_volts core 2>/dev/null | sed -E 's/[^0-9.]//g')

  if [[ -z "$voltage" ]]; then
    echo "N/A"
    return
  fi

  echo "$voltage"
}

# Piecewise-linear model for whole-CPU idle power as a function of frequency.
interpolate_cpu_idle_power() {
  local freq_hz="$1"
  freq_hz=$(printf "%.0f" "$freq_hz")

  awk -v f="$freq_hz" '
  BEGIN {
    fq[1]=600000;  pw[1]=2.125;
    fq[2]=1500000; pw[2]=3.500;
    fq[3]=2400000; pw[3]=6.500;
    n=3;

    if (f <= fq[1]) { printf "%.6f", pw[1]; exit }
    if (f >= fq[n]) { printf "%.6f", pw[n]; exit }

    for (i=1; i<n; i++) {
      if (f >= fq[i] && f <= fq[i+1]) {
        power = pw[i] + (f - fq[i]) * (pw[i+1] - pw[i]) / (fq[i+1] - fq[i]);
        printf "%.6f", power;
        exit
      }
    }

    printf "%.6f", pw[1]
  }'
}

# Dynamic power model for whole CPU: P = Ceff * V^2 * f.
compute_cpu_p_dynamic() {
  local freq_khz="$1"
  local volt="$2"
  local ceff="0.00000113759" # Effective switching capacitance (F) - estimated from measurements

  freq_khz=$(printf "%.0f" "$freq_khz")
  if [[ -z "$freq_khz" || -z "$volt" || "$volt" == "N/A" ]]; then
    echo "0"
    return
  fi

  bc -l <<< "scale=10; $ceff * $volt^2 * $freq_khz"
}

# Core-level modeled power derived from whole-CPU idle/dynamic components.
modeled_power() {
  local core_freq="$1"
  local core_volt="$2"
  local core_util="$3"
  local number_of_cores="$4"

  local cpu_idle_power
  local cpu_dynamic_power
  cpu_idle_power=$(interpolate_cpu_idle_power "$core_freq")
  cpu_dynamic_power=$(compute_cpu_p_dynamic "$core_freq" "$core_volt")

  local core_idle_power
  local core_dynamic_power
  core_idle_power=$(echo "$cpu_idle_power / $number_of_cores" | bc -l)
  core_dynamic_power=$(echo "$cpu_dynamic_power / $number_of_cores" | bc -l)

  echo "$(echo "$core_idle_power + ($core_util / 100) * $core_dynamic_power" | bc -l)"
}

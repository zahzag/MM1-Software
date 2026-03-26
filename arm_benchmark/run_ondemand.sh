#!/bin/bash

set -euo pipefail

RUNNER_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$RUNNER_DIR/.." && pwd)"
ADD_TO_EXCEL_SCRIPT="$RUNNER_DIR/add_to_excel.py"

source "$RUNNER_DIR/helpers.sh"
source "$RUNNER_DIR/lib/common.sh"

# Default runtime configuration (can be overridden by env vars or CLI options).
CORE="${CORE:-3}"
RUNS="${RUNS:-1}"
MIN_FREQ="${MIN_FREQ:-600000}"
MAX_FREQ="${MAX_FREQ:-2400000}"
SAMPLING_RATE="${SAMPLING_RATE:-40000}"
THRESHOLD="${THRESHOLD:-95}"
NUM_CPUS="${NUM_CPUS:-$(( $(nproc) + 1 ))}"
GOVERNOR="ondemand"
CLIENT_DURATION_MS="${CLIENT_DURATION_MS:-600000}"
CLIENT_REPEAT="${CLIENT_REPEAT:-1000000}"
WARMUP_SEC="${WARMUP_SEC:-7}"
SLEEP_BETWEEN_RUNS="${SLEEP_BETWEEN_RUNS:-60}"
SERVER_IFACE="${SERVER_IFACE:-eth0}"

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

Options:
  --core N
  --runs N
  --min-freq HZ
  --max-freq HZ
  --sampling-rate US
  --threshold PERCENT
  --num-cpus N
  --iface NAME
  --help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --core) CORE="$2"; shift 2 ;;
    --runs) RUNS="$2"; shift 2 ;;
    --min-freq) MIN_FREQ="$2"; shift 2 ;;
    --max-freq) MAX_FREQ="$2"; shift 2 ;;
    --sampling-rate) SAMPLING_RATE="$2"; shift 2 ;;
    --threshold) THRESHOLD="$2"; shift 2 ;;
    --num-cpus) NUM_CPUS="$2"; shift 2 ;;
    --iface) SERVER_IFACE="$2"; shift 2 ;;
    --help) usage; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

require_commands bc awk mpstat taskset python3 java sudo cpupower ip vcgencmd || exit 1

# Validate local dependencies early so long runs do not fail late.
if [[ ! -f "$ADD_TO_EXCEL_SCRIPT" ]]; then
  echo "[ERROR] add_to_excel script not found: $ADD_TO_EXCEL_SCRIPT" >&2
  exit 1
fi

BENCHMARK_DATE="${BENCHMARK_DATE:-$(date +%Y%m%d)}"
OUTPUT_DIR="$(ensure_output_dir "$SAMPLING_RATE" "$THRESHOLD" "$BENCHMARK_DATE")"
RUN_LOG="$OUTPUT_DIR/nohup.log"
# Mirror stdout/stderr into a persistent run log.
exec > >(tee -a "$RUN_LOG") 2>&1

echo "[INFO] Output directory: $OUTPUT_DIR"
echo "[INFO] Configuration: core=$CORE runs=$RUNS min_freq=$MIN_FREQ max_freq=$MAX_FREQ sampling_rate=$SAMPLING_RATE threshold=$THRESHOLD"

cleanup_pids=()
cleanup() {
  for pid in "${cleanup_pids[@]:-}"; do
    safe_kill "$pid"
  done
}
# Always clean background samplers/processes on exit or interruption.
trap cleanup EXIT

echo "[INFO] Preparing ARM CPU governor state"
# Set target core to ondemand governor with specified min/max frequencies.
configure_cpu_performance "$CORE" "$GOVERNOR" "$MIN_FREQ" "$MAX_FREQ"

# Read lambda step depending on target core max freqeuency to ensure same utilization levels.
read lambda_step lambda_max <<< "$(increase_lamda "$MAX_FREQ")"
if [[ ! "$lambda_step" =~ ^[0-9]+(\.[0-9]+)?$ ]] || [[ ! "$lambda_max" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
  echo "[ERROR] increase_lamda returned invalid values for max_freq=$MAX_FREQ" >&2
  exit 1
fi

# Sweep lambda values derived from max frequency calibration.
for lambda in $(seq -f "%.2f" "$lambda_step" "$lambda_step" "$lambda_max"); do
  util=$(echo "scale=2; ($lambda / $lambda_max) * 100" | bc)
  # Empirical guardrail: this specific point overloads and causes unstable queue behavior.
  if (( $(echo "$lambda == $lambda_max" | bc -l) )); then
    lambda=$(echo "$lambda_max - 0.5" | bc -l)
  fi
  echo "[INFO] Starting lambda=$lambda (target util=$util%)"

  pkill -9 java 2>/dev/null || true

  for ((i = 1; i <= RUNS; i++)); do
    # Apply ondemand control knobs for the current experiment point.
    echo "$SAMPLING_RATE" | sudo tee /sys/devices/system/cpu/cpufreq/ondemand/sampling_rate >/dev/null
    echo "$THRESHOLD" | sudo tee /sys/devices/system/cpu/cpufreq/ondemand/up_threshold >/dev/null

    echo "[INFO] Iteration $i/$RUNS"
    ensure_server_ip "$SERVER_IFACE"

    pushd "$ROOT_DIR/build" >/dev/null
    export CLASSPATH="../Server/Server/lib/*:."
    # Log file names with clear schema 
    cpu_power_log="cpu_power_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"
    core_freq_log="core_frequency_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"
    cpu_cores_freq_log="cpu_cores_frequency_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"
    core_util_log="core_utilization_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"
    cpu_cores_util_log="cpu_cores_utilization_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"
    cpu_cores_voltage_log="cpu_cores_voltage_${util}_${SAMPLING_RATE}_${THRESHOLD}_${i}.log"

    # RPi setup: measured package power is unavailable; keep x86-compatible log name.
    echo "timestamp,power_w" > "$cpu_power_log"

    (
      while true; do
        # High-rate frequency snapshots: target core and all cores.
        echo "$(date +%s%N),$(cat /sys/devices/system/cpu/cpu${CORE}/cpufreq/scaling_cur_freq)" >> "$core_freq_log"
        freqs="$(date +%s%N)"
        for cpu in $(seq 0 $((NUM_CPUS - 1))); do
          freq=$(cat /sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_cur_freq 2>/dev/null || echo "0")
          freqs="${freqs},${freq}"
        done
        echo "$freqs" >> "$cpu_cores_freq_log"
        sleep 0.1
      done
    ) &
    MANUAL_FREQ_PID=$!
    cleanup_pids+=("$MANUAL_FREQ_PID")

    (
      while true; do
        # ARM voltage source: vcgencmd (single value), expanded to x86-compatible CSV shape.
        voltage=$(read_cpu_voltage_vcgencmd)
        timestamp=$(date +%s%N)
        line="$timestamp"

        # Keep the same voltage file shape as x86: timestamp,cpu0..cpuN,avg_voltage.
        for _cpu in $(seq 0 $((NUM_CPUS - 1))); do
          line="${line},${voltage}"
        done
        line="${line},${voltage}"

        echo "$line" >> "$cpu_cores_voltage_log"
        echo "$timestamp,None" >> "$cpu_power_log"
        sleep 0.1
      done
    ) &
    VOLTAGE_PID=$!
    cleanup_pids+=("$VOLTAGE_PID")

    # Server running
    java Server.Server "$lambda" &
    JAVA_PID=$!

    # Client workload: fixed duration/repeat to keep runs comparable.
    taskset -c 2 java client.LoadGenerator "$lambda" "$CLIENT_DURATION_MS" "$CLIENT_REPEAT" >/dev/null 2>&1 &
    CLIENT_PID=$!

    sleep "$WARMUP_SEC"

    # High-rate utilization snapshots: target core and all cores.
    mpstat -P "$CORE" 1 >> "$core_util_log" &
    MPSTAT_CORE_PID=$!
    cleanup_pids+=("$MPSTAT_CORE_PID")

    mpstat -P ALL 1 >> "$cpu_cores_util_log" &
    MPSTAT_ALL_PID=$!
    cleanup_pids+=("$MPSTAT_ALL_PID")

    #wait for client and server to finish before killing samplers and post-processing logs
    safe_wait "$JAVA_PID"
    safe_wait "$CLIENT_PID"

    safe_kill "$MANUAL_FREQ_PID"
    safe_kill "$VOLTAGE_PID"
    safe_kill "$MPSTAT_CORE_PID"
    safe_kill "$MPSTAT_ALL_PID"

    # Post-process logs to extract average frequency, voltage, utilization and power for the target core.
    avg_core_freq=$(awk -F',' '{sum+=$2} END {if (NR > 0) print sum/NR; else print 0}' "$core_freq_log")
    core_voltage_col=$((CORE + 2))
    avg_core_voltage=$(awk -F',' -v col="$core_voltage_col" '{total+=$col; count++} END {if (count > 0) print total/count; else print 0}' "$cpu_cores_voltage_log")
    # mpstat column convention used here: take %usr/%system aggregate column.
    utilization=$(awk '/^[0-9]/ && $3 ~ /^[0-9]/ {sum += $3; count++} END {if (count > 0) printf "%.2f", sum/count; else print "0.00"}' "$core_util_log")

    # Modeled core power from frequency, voltage and measured utilization.
    avg_core_modeled_power=$(modeled_power "$avg_core_freq" "$avg_core_voltage" "$utilization" "$NUM_CPUS")

    # Excel schema mapping (kept stable for downstream analysis scripts).
    # Measured power is unavailable on RPi in this setup, so it is stored as None.
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 1 "$util"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 2 "$SAMPLING_RATE"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 3 "$THRESHOLD"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 14 "$utilization"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 17 "$avg_core_freq"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 18 "$avg_core_modeled_power"
    python3 "$ADD_TO_EXCEL_SCRIPT" workbook.xlsx 19 "None"
    pkill -9 mpstat 2>/dev/null || true
    popd >/dev/null

    echo "[INFO] Iteration $i completed. Sleeping ${SLEEP_BETWEEN_RUNS}s"
    sleep "$SLEEP_BETWEEN_RUNS"
  done
done

echo "[INFO] Archiving logs for sampling_rate=$SAMPLING_RATE threshold=$THRESHOLD"
# Move generated logs from build/ to the benchmark output folder.
archive_configuration_logs "$CORE" "$SAMPLING_RATE" "$THRESHOLD" "$OUTPUT_DIR"

# Restore CPU governor to a known state after the benchmark.
configure_cpu_performance all ondemand $MAX_FREQ $MAX_FREQ

echo "[INFO] Benchmark completed successfully"
echo "[INFO] Logs stored in: $OUTPUT_DIR"

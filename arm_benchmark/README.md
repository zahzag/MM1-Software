# arm_benchmark

Clean ARM benchmark scripts aligned with the x86 benchmark structure.

## What is different on ARM

- Frequency is read from sysfs (`/sys/devices/system/cpu/cpuX/cpufreq/scaling_cur_freq`).
- Core and full CPU utilization are measured with `mpstat`.
- Voltage is read using `vcgencmd measure_volts core`.
- Measured power is not collected (powerstat is not used on RPi in this flow), so Excel measured-power cell is set to `None`.
- Modeled power is computed using the same algorithm as x86:
  - CPU idle power interpolation from frequency
  - CPU dynamic power from `Ceff * V^2 * f`
  - Convert to core power by dividing by number of cores
  - Core modeled power: `core_idle + rho * core_dynamic`, where `rho = core_utilization / 100`

## Files

- `arm_benchmark/run_ondemand_new.sh`: run one static configuration.
- `arm_benchmark/run_threshold_sweep.sh`: run all sampling-rate/threshold combinations.
- `arm_benchmark/helpers.sh`: clean ARM helper functions.
- `arm_benchmark/lib/common.sh`: shared runner utilities and log archiving.
- `arm_benchmark/add_to_excel.py`: local Excel update utility.

## Output path

Results are stored under:

`data/arm_benchmark/<date>/sr/<sampling_rate>/threshold/<threshold>/`

Each directory contains logs and `nohup.log`.

## Single run

```bash
cd arm_benchmark
nohup ./run_ondemand_new.sh \
  --core 3 \
  --runs 1 \
  --min-freq 600000 \
  --max-freq 2400000 \
  --sampling-rate 40000 \
  --threshold 95 &
```

## Sweep run

```bash
cd arm_benchmark
nohup ./run_threshold_sweep.sh \
  --core 3 \
  --runs 1 \
  --min-freq 600000 \
  --max-freq 2400000 \
  --sampling-rates "40000,50000,60000,70000,80000,90000,100000" \
  --thresholds "75,80,85,90,95" &
```

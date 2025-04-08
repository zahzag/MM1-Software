
# MM1-Software
the implementation of paper " Modelling performance and power consumption of utilization-based DVFS using M-M-1 queues" 
# client
The client-server java implementation represent the M/M/1-FCFS queuing system. the client role is to send jobs to server exponentially, using the variable lamda , repeat and duration .  lamda is the mean arrival rate (jobs/sec ) , repeat is the service rate (jobs/sec) and duration or the size of arrival queue , the duration tha the client send jobs to the server (sec)
# server
The server role is to accept jobs and handl one by one using single cpu core , and compute the performance metrics such as service time , service rate , mean response time , utilization caused by executed workload and many other metrics 

# hardware preparation 
to prepare the hardware for the implementation , we should , fix the server ip address , shiled the cpu-core  ,disable Simultaneaous multi-threading (SMT) , disable turbo-boost , and hyper threading  ,pin the process on it, set the cpu governor to “userspace “ ( to not change frequency ) ,fix the frequency for each test , compute mean consumed power , and choose the mean arrival rate that should not unstabilize the system ( the cpu utilieation exceed 100% ) .


# fix the server ip : 
the client send the jobs to the server using the ip address 10.0.0.2 / 9990 and 10.0.0.2 /9950 , so before starting the server , the user should set the ethernet card to use the 10.0.0.2 adress , and allow the lisetening on ports 9990 and 9950 
set server address 
### sudo ip addr add 10.0.0.2/24 dev enp0s31f6 , where enp0s31f6 is the ethernet card name 
## allow listening on 9990 and 9950 ports 
### sudo ufw allow 9999/tcp 
### sudo ufw allow 9950/tcp 
### sudo ufw enable
### check firewall status: sudo ufw status
# cpu shielding 
shielding CPU depends to each operating system , when the CPU is shielded , the OS will not use it , so it will be totally free
## for fedora : 
### 1.	sudo nano /etc/default/grub 
#### •	add on the line : GRUB_CMDLINE_LINUX = “isolcpus=3 ” 
#### •	when the cpuID = 3
### 2.	sudo grub2_mkconfig -o /boot/grub2/grub.cfg 
### 3.	reboot 
## For ubuntu :
### 1.	sudo nano /etc/default/grub 
#### •	add on the line:   GRUB_CMDLINE_LINUX = “isolcpus=3” 
#### •	when the cpuID = 3
### 2.	sudo update-grub 
### 3.	reboot
## For Fedora & ubuntu 
### •	Shield cpu 3 : Sudo cset shiled --cpu 3
### •	Unshiled cpus: sudo cset shield –reset
### •	Show shielded cpus : sudo cset shield --shield -v 

### NB: 
by using cset shield , the server can’t use the cpu 3 directly , you should pin the cpu to the server using “taskset -c 3 Server.Server” , but when we pin the cpu to the server , the thread used to handl jobs and the threads used to listen to client requests will both be executed oncpu 3 , and this is not a fully cpu isolation , which mean using cset shiled is not a good idea for this simulation


# CPU Pinning 
To pin process to a cpu core we will use the “taskset” tool,
## •	Pin server to cpu 3 
### o	taskset -c 3 java Server.Server
## •	pin client to another cpu core to ensure that it will not disturb the server process 
### o	taskset -c 8 java client.LoadGenrator 

## NB : 
pinning the server process to cpu 3 meand that all the java process will use just cpu 3 , which mean the both , listening thread and handling jobs thread will use the same core , which lead to non isolation of jobs handling , to ensure that cpu 3 will be used just for jobs handling , there is a java library named “Affinity” on server that look for a shielded cpu and pin it directly to jobs handling . there is no need to pin cpu core for server .

## Simultaneous multi-threading (SMT)

SMT is a function of power systems servers that allows multiple logicale CPUs share physical core , for Intel  is called hyper-threading (HT) . for our experiments the MM1-FCFS queuing system should have just one server , which mean one core and one thread each time , so to avoid parallelism , we should disable hyper-threading 
### For Fedora 
### •	echo off | sudo tee /sys/devices/system/cpu/smt/control
### 1.	Or to disable it permanently
#### 1.	sudo nano /etc/default/grub 
##### i.	add on the line:   GRUB_CMDLINE_LINUX = “isolcpus=3 noht ” 
##### ii.	when noht is no hyper-threading
#### 2.	sudo grub2_mkconfig -o /boot/grub2/grub.cfg 
#### 3.	reboot 
### For ubuntu
#### •	echo off | sudo tee /sys/devices/system/cpu/smt/control
#### 3.	Or to disable it permanently
##### 1.	sudo nano /etc/default/grub 
###### i.	add on the line:   GRUB_CMDLINE_LINUX = “isolcpus=3 noht” 
###### ii.	when noht is no hyper-threading
##### 2.	sudo update-grub 
##### 3.	reboot 

# Turbo boost 

Turbo Boost is an Intel technology that dynamically increases the clock speed of a processor when the workload demands it, as long as the processor is operating below its power, current, and temperature limits. For our experiments, the CPU should not increase the cpu clock speed dynamically or go to the highest frequency .

## For Fedora & ubuntu : 

### •	Disable turbo boost :

#### echo 1 | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo

#### Or : we can disable the turbo state permanently form BIOS
##### 1.	Reboot and access to BIOS configuration
##### 2.	Navigate to “Performance” section 
##### 3.	Uncheck the “enable intel turboBoost”option 
##### 4.	Click apply and exit 

### •	Enbale  turbo boost :

#### echo 0  | sudo tee /sys/devices/system/cpu/intel_pstate/no_turbo
#### Or : we can enable the turbo state permanently form BIOS
##### 1.	Reboot and access to BIOS configuration
#####  2.	Navigate to “Performance” section 
#####  3.	Check the “enable intel turboBoost”option 
##### 4.	Click apply and exit 


# Intel P-State

Intel pstate is a the voltage-frequency control states used in modern linux kernels to manage CPU frequency and power – states . it works in conjunction with the CPU’s internal governor to optimize performance and power efficiency . On our experiment , the intel – pstate should not play a role in changing frequency . 

## For Fedora & ubuntu 
### •	Disable intel_pstate:
#### 1.	sudo nano /sys/devices/system/cpu/intel_pstate/status
#### 2.	set status to “passive”
### •	Enable instel_pstate :
#### 1.	sudo nano /sys/devices/system/cpu/intel_pstate/status
#### 2.	set status to “active”

## NB :
if we disable intel_pstate from BIOS , we will not have access to monitor cpu frequency or change it or even change the governor .Hence, the only way to disable intel_pstate it to set it to “passive” .

# Intel Speed step 

Is a technology that allows the CPU to dynamically adjust CPU clock speed and voltage to balance performance and power consumption , it switch dynamically between frequencies and voltage based on CPU load . On our experiment , SpeedStep should not play a role in changing frequency .

## Disable SpeedStep from BIOS 
### 1.	Reboot and access to BIOS configuration
### 2.	Navigate to “Performance” section 
### 3.	Uncheck the “enable intel SpeedStep” option 
### 4.	Click apply and exit 

# CPU Governors

CPU governors are software mechanisms that control cpu frequency-scaling and voltage in response to workload . especially In systems with dynamic frequency scaling, there is many cpu governors : Performance , powersave, ondemand, conservative , schedutil , userspace and interactive . on this experiment  , we focus on userspace , because the frequency should be fix , to have the same Mean-Service-Rate on all tests  for each frequency.

## For Fedora & ubuntu :
### Cpupower-gui
#### •	Install cpupower-gui
##### o	Fedora : Sudo dnf install cpupower-gui
##### o	Ubuntu : Install cpupower-gui : sudo apt-get install cpupower-gui 
#### •	Lunching : cpupower-gui
#### •	choose the cpu 3 and change the governot to userspace , then choose the frequency 

### Cpupower
#### •	Change governor: sudo cpupower --cpu 3 frequency-set -g userspace
#### •	Change frequency intervale : sudo cpupower --cpu 3 frequency-set -d 1.2Ghz -u 1.2Ghz 
#### •	Show cpu current frequency : 
##### o	sudo cpupower --cpu 3 frequency-info 
##### o	Or : current governor : cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor
##### o	Current frequency:  cat /sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_frequency


# Connect client  with server 

The client and server should be on the same network , so like this , the client will find the server using his IP address , and to ensure a fast communication , we use the cable RJ45 as a bridge between them . 

## Server : 
### •	The server IP address is 10.0.0.2/24 
### •	To set the network to be on this network: 
#### o	sudo ip addr add 10.0.0.2/24 dev enp1s0 
#### o	sudo ip link set enp1s0 up
#### o	Where enp1s0 is the network adapter name
### •	The server listening ports are : 9999 and 9950
### •	To allow listening on those ports we should allow them from firewall
#### o	sudo ufw allow 9999/tcp 
#### o	sudo ufw allow 9999/udp
#### o	sudo ufw allow 9950/tcp
#### o	sudo ufw allow 9950/udp
#### o	enable firewall: sudo ufw enable 
#### o	check ufw status : sudo ufw status

## Client :

the client also should be in the same network , let’s give it 10.0.0.1/24
### •	sudo ip addr add 10.0.0.1/24 dev eno1
### •	sudo ip link set eno1 up
### •	where eno1 is the network adapter name
### •	To allow sending from those ports we should allow them from firewall
#### o	sudo ufw allow 9999/tcp 
#### o	sudo ufw allow 9999/udp
#### o	sudo ufw allow 9950/tcp
#### o	sudo ufw allow 9950/udp
#### o	enable firewall: sudo ufw enable 
#### o	check ufw status : sudo ufw status

## connection client-server:
### test connection from server: ping 10.0.0.1/24 
### test connection from client: ping 10.0.0.2/24

# CPU utilization 
Cpu utilization indicates the amount of load handled by individual core to handl the process , on our situation , is the load reached by cpu core to handle jobs 
mpstat
To monitor the utilization of cpu 3 while server running ( handling jobs ) , we use the “mpstat” tool , we we focus one “%usr” parameter 
Monitor the cpu core 3 utilization each 1 second and collect results on cpu3 file 

## Fedora:
### •	Install mpstat : 
#### o	sudo yum install sysstat
#### o	sudo systemctl enable sysstat && sudo systemctl start sysstat
### •	mpstat -P 3 1 > cpu3.log

## ubuntu: 
### •	Install mpstat : 
#### o	sudo apt install sysstat
#### o	sudo systemctl enable sysstat && sudo systemctl start sysstat
### •	mpstat -P 3 1 > cpu3.log

At the end of server jobs handling , the mpstat calculate the average cpu core utilization on this time intervale , then we took this result and add to excel file using a python script

# CPU Power consumption
The CPU power consumption is the consumed power by CPU core 3 while the server handling jobs 
There are several tools that monito CPU power consumption , like powerstat , turbostat ..
## Powerstat
Powerstat measures the power consumption of a laptop using the ACPI battery information. The output is like vmstat but also shows power consumption statistics.

### Fedora 
#### •	Install powerstat : 
##### o	sudo dnf install epel-release
##### o	sudo dnf install powerstat
#### •	powerstat -cDHRf 1
### Ubuntu
#### •	Install powerstat : 
##### o	sudo apt install powerstat
#### •	powerstat -cDHRf 1 

The problem with powerstat is that shows the power consumption of all CPU , not core by core , instead of turbostat that can gives the cpu core power consumption and also the whole cpu power consumption

## Turbostat
turbostat is a powerful tool provided by Intel to monitor CPU frequency, power consumption, and other performance metrics. It is particularly useful for analyzing the behavior of Intel CPUs,

### Fedora
#### •	Install turbostat:
##### o	sudo dnf install kernel-tools
#### •	sudo turbostat --cpu 3 --interval 1 --show CorWatt --quiet --Summary -o core_power.log 

### Ubuntu
#### •	Install turbostat:
##### o	sudo apt install linux-tools-common linux-tools-generic
#### •	sudo turbostat --cpu 3 --interval 1 --show CorWatt --quiet --Summary -o core_power.log 

we specifie the option “CorWatt” to show just the cpu Core 3 consumed power on Watt , the power consumption is monitored each second , 
turbostat don’t gives the average power consumption , to compute the average power consumption of cpu 3  we have to compute it manually , this is why we collecte every power consumption values on core_power file . and after we used a small function to compute the average and the totale energy , and another python script to add those values to excel file 

# Job

The job class define the structure of work job arriving from client . The calc method contains the CPU intensive calculation which is repeated as per the size of the Job . but should not reach the 100% utilization , it can reach the peak and go down directly , but not take a more than 1 second on the peak . 
start simulation 

before starting the simulation we should compile the server and client code to .class , specifying the class path of each package .
NB : the working directory should be ./MM1-Software 

## Server : 
### •	Compiling server :
#### o	javac -cp “Server/Server/lib/*:.” Server/Server/src/*.java -d build/
### •	Running server : cd build 
#### o Java Server.Server 5
#####	Where 5 is the jobs arrival rate (lamda)

## Client 
### •	Compiling client :
#### o	Javac -cp “Server/Server/lib/*:.” clien/src/*.java -d build/
### •	Running client :
#### o	Java client.LoadGenrator 5 600000 1000000
##### Where 5 is lamda ( mean arrival rate ) 
##### And 600000 is the duration of sending jobs to server (10 min)
##### And 1000000 is the repeat parameter which distributed exponentially between 1M and 1.6M to ensure the exponential jobs handling. Big repeat  lead to big workload 

NB : the jobs arrival rate for each frequency should be always less than the service rate of this frequency , if is not , the system will fall on overflow , 

To compute the maximum arrival rate for each frequency , we lunch the system for the first time for each frequency , and take the mean service rate for each frequency , then we compute the service rate when the cpu utilization is 10% , 20%, 30%, … ,90% using the mathematical relation : arrival_rate = cpu_core_utilization*mean_service_rate . 
After collecting arrival_rate values that we use 10%, 20%,... of cpucore using a chosen frequency , we collect them on an array and run for each frequency his arrival rate values ( lamdas) .
After running the simulation , the client start sending jobs with specified mean arrival rate exponentially . the server accept each job with his length , and save it on a queue . by respecting the aspect of FCFS , the server start handling each job one by one .

When the jobs queue become empty , the server compute metrics like Mean service rate ,Mean response time , highest reached state, cpu time , execution time ,etc ; and save those results on a excel file named "wookbook.xlsx. 

# NB :
if we didn’t disable SPeedStep , the cpu will always change his frequency even if we use the governor ”userspace” and we fix the frequency , it will be changed always 

# Contribution
Feel free to contribute more resources or suggest updates by opening a pull request or issue in this repository.

# Author 
-- ZAHIR Ayman

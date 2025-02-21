#!/bin/bash
source scripts/helpers.sh
increased_lamda=0
for((freq=2100000 ; freq >= 800000 ; freq=freq-100000)) ; do

read increased_lamda max_lamda <<< $(increase_lamda $freq )
echo "---$increased_lamda --- $max_lamda"
	#i=0
	for lamda in $(seq $increased_lamda $increased_lamda $max_lamda); do 
	echo $lamda
	#i=$i+1
	done 
echo "-------------"
done

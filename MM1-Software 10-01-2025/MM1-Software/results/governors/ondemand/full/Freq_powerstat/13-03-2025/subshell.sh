      TOTAL_FREQ=0
              COUNT=0
              
              while read FRQ; do

                 TOTAL_FREQ=$(echo "$TOTAL_FREQ + $FRQ" | bc)  # Sum freq over time
                 COUNT=$((COUNT + 1))

              done < <(tail -n +2 core_frequency.log)  # Process file in a subshell
               # Compute total freq and avg
              TOTAL_FREQ=$(echo "$TOTAL_FREQ * 1000" | bc)
              AVG_CORE_FREQ=$(echo "$TOTAL_FREQ / $COUNT" | bc -l)
echo $AVG_CORE_FREQ

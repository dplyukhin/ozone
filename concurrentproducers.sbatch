#!/bin/bash

###############################################################################
#
#SBATCH --time=02:00:00                  # Job run time (hh:mm:ss)
#SBATCH --nodes=3                        # Number of nodes
#SBATCH --cpus-per-task=4                # Number of cores per node
#SBATCH --output=output-%A-%a.txt              # Name of batch job output file
#SBATCH --array=0-16%1                          # Percent specifies one batch at a time
#
###############################################################################

useOzoneValues=(false true)
requestsPerSecondValues=(20 40 60 80 100 120 140 160)

i=0

echo "task id: $SLURM_ARRAY_TASK_ID"
for _useOzone in "${useOzoneValues[@]}"; do
  for _requestsPerSecond in "${requestsPerSecondValues[@]}"; do
    echo "Current index: $i"
    if [ "$i" -eq "$SLURM_ARRAY_TASK_ID" ]; then
      echo "Found index: $i"
      requestsPerSecond=$_requestsPerSecond
      useOzone=$_useOzone
      echo "useOzone: $useOzone, requestsPerSecond: $requestsPerSecond"
    fi
    i=$((i+1))
  done
done

systemArgs="-DrequestsPerSecond=$requestsPerSecond -DserverHost=node0"

echo "For i=$SLURM_ARRAY_TASK_ID, args=$systemArgs"

srun --mpi=none ./concurrentproducers.sh $useOzone $systemArgs


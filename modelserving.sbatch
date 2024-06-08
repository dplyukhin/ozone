#!/bin/bash

###############################################################################
#
#SBATCH --time=02:00:00                  # Job run time (hh:mm:ss)
#SBATCH --nodes=6                        # Number of nodes
#SBATCH --cpus-per-task=4                # Number of cores per node
#SBATCH --output=output-%A-%a.txt              # Name of batch job output file
#SBATCH --array=0-19%1                          # Percent specifies one batch at a time
##SBATCH --error=errors-%A-%a.txt              # Name of batch job error file
##SBATCH --mail-user=daniilp2@illinois.edu  # Send email notifications
##SBATCH --mail-type=BEGIN,END           # Type of email notifications to send
#
###############################################################################

batchSize=10
useOzoneValues=(false true)
requestsPerSecondValues=(25 50 75 100 125 150 175 225 275 350) # Skip rates that map to the same effective request rate

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

systemArgs="-DbatchSize=$batchSize -DrequestsPerSecond=$requestsPerSecond -DuseOzone=$useOzone -Djava.awt.headless=true -DclientHost=node0 -DbatcherHost=node1 -Dworker1Host=node2 -Dworker2Host=node3 -Dmodel1Host=node4 -Dmodel2Host=node5"

echo "For i=$SLURM_ARRAY_TASK_ID, args=$systemArgs"

srun --mpi=none ./modelserving.sh $systemArgs


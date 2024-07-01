#!/bin/bash

###############################################################################
#
#SBATCH --time=02:00:00                  # Job run time (hh:mm:ss)
#SBATCH --nodes=6                        # Number of nodes
#SBATCH --cpus-per-task=4                # Number of cores per node
#SBATCH --output=output-%A-%a.txt              # Name of batch job output file
#SBATCH --array=0-9%1                          # Percent specifies one batch at a time
#
###############################################################################

batchSize=10
requestsPerSecondValues=(25 50 75 100 125 150 175 225 275 350) # Skip rates that map to the same effective request rate

requestsPerSecond=${requestsPerSecondValues[SLURM_ARRAY_TASK_ID]}
systemArgs="-DbatchSize=$batchSize -DrequestsPerSecond=$requestsPerSecond -Djava.awt.headless=true"

echo "For i=$SLURM_ARRAY_TASK_ID, args=$systemArgs"

srun --mpi=none ./akkamodelserving.sh $systemArgs
#!/bin/bash

###############################################################################
#
#SBATCH --time=02:00:00                  # Job run time (hh:mm:ss)
#SBATCH --nodes=6                        # Number of nodes
#SBATCH --cpus-per-task=4                # Number of cores per node
#SBATCH --output=output-%A-%a.txt              # Name of batch job output file
#SBATCH --array=0-9%1                          # Percent specifies one batch at a time
##SBATCH --error=errors-%A-%a.txt              # Name of batch job error file
##SBATCH --mail-user=daniilp2@illinois.edu  # Send email notifications
##SBATCH --mail-type=BEGIN,END           # Type of email notifications to send
#
###############################################################################

batchSize=10
requestsPerSecondValues=(25 50 75 100 125 150 175 225 275 350) # Skip rates that map to the same effective request rate

requestsPerSecond=${requestsPerSecondValues[SLURM_ARRAY_TASK_ID]}
systemArgs="-DbatchSize=$batchSize -DrequestsPerSecond=$requestsPerSecond -Djava.awt.headless=true"

echo "For i=$SLURM_ARRAY_TASK_ID, args=$systemArgs"

srun --mpi=none ./akkamodelserving.sh $systemArgs

#srun ./cholesky.sh $benchmark \
#  -Dbench.latency=$latency -Dbench.filename="$filename.csv" \
#  -Dakka.remote.artery.advanced.outbound-message-queue-size=4096 \
#  -Dakka.remote.artery.advanced.outbound-lanes=1
#
#echo "mv output-$SLURM_ARRAY_JOB_ID-$SLURM_ARRAY_TASK_ID.txt output-$filename.txt" >> renaming.sh

#!/bin/bash

###############################################################################
#
#SBATCH --time=02:00:00                  # Job run time (hh:mm:ss)
#SBATCH --nodes=4                        # Number of nodes
#SBATCH --cpus-per-task=4                # Number of cores per node
#SBATCH --output=output-%A-%a.txt              # Name of batch job output file
#SBATCH --array=2%1                          # Percent specifies one batch at a time
#
###############################################################################

if [ "$SLURM_ARRAY_TASK_ID" -eq "0" ]; then
  useOzone=false
else
  useOzone=true
fi
echo "useOzone: $useOzone"

systemArgs="-DserverHost=node0"

srun --mpi=none ./concurrentsend.sh $useOzone $systemArgs


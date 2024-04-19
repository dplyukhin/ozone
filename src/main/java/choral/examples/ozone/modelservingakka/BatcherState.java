package choral.examples.ozone.modelservingakka;

import java.util.ArrayList;

import choral.examples.ozone.modelserving.Config;

public class BatcherState {

    private ArrayList<Integer> currentBatch;
    private ArrayList<Integer> currentBatchWorkerIDs;
    private int previousModel = -1;

    public BatcherState() {
        this.currentBatch = new ArrayList<>();
        this.currentBatchWorkerIDs = new ArrayList<>();
    }

    public synchronized void newImage(int imgID, int workerID) {
        currentBatch.add(imgID);
        currentBatchWorkerIDs.add(workerID);
    }

    public synchronized BatchIDs getBatchIfFull() {
        if (currentBatch.size() == Config.BATCH_SIZE) {
            int[] imgIDs = currentBatch.stream().mapToInt(i -> i).toArray();
            int[] workerIDs = currentBatchWorkerIDs.stream().mapToInt(i -> i).toArray();
            BatchIDs batchIDs = new BatchIDs(imgIDs, workerIDs);
            currentBatch.clear();
            return batchIDs;
        }
        return null;
    }

    public synchronized int chooseModel(int numModels) {
        if (previousModel == -1) {
            previousModel = 0;
            return 0;
        }
        else {
            previousModel = (previousModel + 1) % numModels;
            return previousModel;
        }
    }
}

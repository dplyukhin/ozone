package choral.examples.ozone.modelserving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatcherState {

    private ArrayList<Integer> currentBatch;
    private int previousModel = -1;

    public BatcherState() {
        this.currentBatch = new ArrayList<>();
    }

    public synchronized void newImage(int imgID) {
        currentBatch.add(imgID);
    }

    public synchronized BatchIDs getBatchIfFull() {
        if (currentBatch.size() == Config.BATCH_SIZE) {
            BatchIDs batchIDs = new BatchIDs(currentBatch.stream().mapToInt(i -> i).toArray());
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

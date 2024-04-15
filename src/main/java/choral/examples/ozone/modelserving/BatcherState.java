package choral.examples.ozone.modelserving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatcherState {

    private Map<BatchID, Integer> imagesPerBatch;
    private int previousModel = -1;

    public BatcherState() {
        this.imagesPerBatch = new HashMap<>();
    }

    public void newImage(BatchID batchID) {
        if (!imagesPerBatch.containsKey(batchID)) {
            imagesPerBatch.put(batchID, 0);
        }
        imagesPerBatch.put(batchID, imagesPerBatch.get(batchID) + 1);
    }

    public boolean isBatchFull(BatchID batchID) {
        return imagesPerBatch.get(batchID) == Config.BATCH_SIZE;
    }

    public void clearBatch(BatchID batchID) {
        imagesPerBatch.remove(batchID);
    }

    public int chooseModel(int numModels) {
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

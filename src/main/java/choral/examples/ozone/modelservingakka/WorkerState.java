package choral.examples.ozone.modelservingakka;


import java.util.HashMap;
import java.util.Map;

import choral.examples.ozone.modelserving.Image;
import choral.examples.ozone.modelserving.ProcessedImages;

public class WorkerState {

    private int workerID;
    private Map<Integer, Image> processedImages;

    public WorkerState(int workerID) {
        this.processedImages = new HashMap<>();
        this.workerID = workerID;
    }

    public Image preprocess(Image img) {
        return img;
    }

    public synchronized void store(int imgID, Image img) {
        processedImages.put(imgID, img);
    }

    public synchronized boolean canDumpBatch(BatchIDs batchIDs) {
        int[] imgIDs = batchIDs.getBatchIDs();
        int[] workerIDs = batchIDs.getWorkerIDs();
        for (int i = 0; i < imgIDs.length; i++) {
            if (workerIDs[i] != this.workerID) {
                continue;
            }
            if (!processedImages.containsKey(imgIDs[i])) {
                return false;
            }
        }
        return true;
    }

    public synchronized ProcessedImages dumpBatch(BatchIDs batchIDs) {
        int batchSize = batchIDs.getBatchIDs().length;
        Image[] batch = new Image[batchSize];
        int[] imgIDs = batchIDs.getBatchIDs();
        for (int i = 0; i < batchSize; i++) {
            batch[i] = processedImages.remove(imgIDs[i]);
        }
        return new ProcessedImages(batch, imgIDs);
    }
}

package choral.examples.ozone.modelserving;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkerState {

    private Map<Integer, Image> processedImages;

    public WorkerState() {
        this.processedImages = new HashMap<>();
    }

    public Image preprocess(Image img) {
        return img;
    }

    public synchronized void store(int imgID, Image img) {
        processedImages.put(imgID, img);
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

package choral.examples.ozone.modelserving;

import java.awt.Image;
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
        Image[] batch = new Image[batchIDs.getBatchIDs().length];
        for (int i = 0; i < batchIDs.getBatchIDs().length; i++) {
            batch[i] = processedImages.remove(batchIDs.getBatchIDs()[i]);
        }
        return new ProcessedImages(batch);
    }
}

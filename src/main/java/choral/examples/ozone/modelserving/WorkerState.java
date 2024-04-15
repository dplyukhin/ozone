package choral.examples.ozone.modelserving;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkerState {

    private Map<BatchID, ArrayList<Image>> processedBatches;

    public WorkerState() {
        this.processedBatches = new HashMap<>();
    }

    public Image preprocess(Image img) {
        return img;
    }

    public void store(BatchID batchID, Image img) {
        if (!processedBatches.containsKey(batchID)) {
            processedBatches.put(batchID, new ArrayList<>());
        }
        processedBatches.get(batchID).add(img);
    }
}

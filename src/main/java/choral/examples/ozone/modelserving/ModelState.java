package choral.examples.ozone.modelserving;

import java.awt.Image;
import java.util.ArrayList;

public class ModelState {
    public Predictions classify(ArrayList< Image > batch) {
        try {
            // Simulate the time it takes to process a batch.
            // On Apple M3, I measured a 15 ms startup time and roughly 3 ms per image.
            Thread.sleep(15 + 3 * Config.BATCH_SIZE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Predictions(null);
    }
}

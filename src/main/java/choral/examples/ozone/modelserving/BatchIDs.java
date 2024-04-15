package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class BatchIDs implements Serializable {
    private final int[] imgIDs;

    public BatchIDs(int[] imgIDs) {
        this.imgIDs = imgIDs;
    }

    public int[] getBatchIDs() {
        return imgIDs;
    }

    public String toString() {
        return "BatchIDs{" +
                "imgIDs=" + java.util.Arrays.toString(imgIDs) +
                '}';
    }
}

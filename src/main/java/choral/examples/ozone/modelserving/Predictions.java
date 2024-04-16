package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class Predictions implements Serializable {
    private final String[] predictions;
    private final int[] imgIDs;

    public Predictions(String[] predictions, int[] imgIDs) {
        this.predictions = predictions;
        this.imgIDs = imgIDs;
    }

    public String[] getPredictions() {
        return predictions;
    }

    public int[] getImgIDs() {
        return imgIDs;
    }

    public String toString() {
        return "Predictions{" +
                "predictions=" + java.util.Arrays.toString(predictions) +
                ", imgIDs=" + java.util.Arrays.toString(imgIDs) +
                '}';
    }
}

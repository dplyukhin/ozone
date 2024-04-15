package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class Predictions implements Serializable {
    private final String[] predictions;

    public Predictions(String[] predictions) {
        this.predictions = predictions;
    }

    public String[] getPredictions() {
        return predictions;
    }

    public String toString() {
        return "Predictions{" +
                "predictions=" + java.util.Arrays.toString(predictions) +
                '}';
    }
}

package choral.examples.ozone.modelserving;

public class Predictions {
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

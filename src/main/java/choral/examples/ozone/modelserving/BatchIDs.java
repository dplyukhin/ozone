package choral.examples.ozone.modelserving;

public class BatchIDs {
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

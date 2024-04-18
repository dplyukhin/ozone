package choral.examples.ozone.modelservingakka;

import java.io.Serializable;

public class BatchIDs implements Serializable {
    private final int[] imgIDs;
    private final int[] workerIDs;

    public BatchIDs(int[] imgIDs, int[] workerIDs) {
        this.imgIDs = imgIDs;
        this.workerIDs = workerIDs;
    }

    public int[] getBatchIDs() {
        return imgIDs;
    }

    public int[] getWorkerIDs() {
        return workerIDs;
    }

    public String toString() {
        return "BatchIDs{" +
                "imgIDs=" + java.util.Arrays.toString(imgIDs) +
                ", workerIDs=" + java.util.Arrays.toString(workerIDs) +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BatchIDs batchIDs = (BatchIDs) obj;
        return java.util.Arrays.equals(imgIDs, batchIDs.imgIDs) && java.util.Arrays.equals(workerIDs, batchIDs.workerIDs);
    }

    @Override
    public int hashCode() {
        int result = java.util.Arrays.hashCode(imgIDs);
        result = 31 * result + java.util.Arrays.hashCode(workerIDs);
        return result;
    }
}

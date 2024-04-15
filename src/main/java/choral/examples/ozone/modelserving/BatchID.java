package choral.examples.ozone.modelserving;

import java.io.Serializable;

public class BatchID implements Serializable {
    final int clientID;
    final int batchNumber;

    public BatchID(int clientID, int batchNumber) {
        this.clientID = clientID;
        this.batchNumber = batchNumber;
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BatchID)) {
            return false;
        }
        BatchID other = (BatchID) obj;
        return clientID == other.clientID && batchNumber == other.batchNumber;
    }

    @Override public int hashCode() {
        return 31 * clientID + batchNumber;
    }

    @Override public String toString() {
        return "BatchID{" + "clientID=" + clientID + ", batchNumber=" + batchNumber + '}';
    }

    public int getClientID() {
        return clientID;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

}
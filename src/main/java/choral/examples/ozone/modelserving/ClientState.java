package choral.examples.ozone.modelserving;

import choral.Log;

public class ClientState {
    int previousWorker = -1;

    public ClientState() {
    }

    public int chooseWorker(int numWorkers) {
        if (previousWorker == -1) {
            previousWorker = 0;
            return 0;
        }
        else {
            previousWorker = (previousWorker + 1) % numWorkers;
            return previousWorker;
        }
    }
}

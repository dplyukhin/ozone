package choral.examples.ozone.concurrentsend;

import java.util.concurrent.CountDownLatch;

import choral.Log;

public class ClientState {
    CountDownLatch keyLatch;
    CountDownLatch txtLatch;

    public ClientState(Integer numIterationsLeft) {
        this.keyLatch = new CountDownLatch(numIterationsLeft);
        this.txtLatch = new CountDownLatch(numIterationsLeft);
    }

    public void consumeKey(String input) {
        keyLatch.countDown();
    }

    public void consumeTxt(String input) {
        txtLatch.countDown();
    }

    public void await() {
        try {
            keyLatch.await();
            txtLatch.await();
        }
        catch (InterruptedException e) {
            Log.debug("Worker interrupted while waiting for all iterations to complete.");
        }
    }
}
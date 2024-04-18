package choral.examples.ozone.modelservingakka;

import java.io.IOException;

import choral.Log;
import choral.examples.ozone.modelserving.BatcherState;
import choral.examples.ozone.modelserving.Config;

public class Batcher {

    public static void debug(String msg) {
        Log.debug("BATCHER: " + msg);
    }

    public static void main(String[] args) {

        debug("Starting!");

        BatcherState state = new BatcherState();

        for (int i = 0; i < Config.WARMUP_ITERATIONS + Config.NUM_REQUESTS; i++) {

            // DO STUFF

        }
    }
}

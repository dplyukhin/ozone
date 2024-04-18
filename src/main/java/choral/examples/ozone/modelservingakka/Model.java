package choral.examples.ozone.modelservingakka;

import choral.Log;
import choral.examples.ozone.modelserving.ModelState;

public class Model {

    private static int modelID = -1;

    public static void debug(String msg) {
        Log.debug("MODEL" + modelID + ": " + msg);
    }

    public static void main(String[] args) {
        // Get the Model ID from the command line arg after --modelID
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--modelID")) {
                modelID = Integer.parseInt(args[i+1]);
            }
        }
        if (modelID == -1) {
            debug("Model ID not provided. Exiting.");
            return;
        }
        else if (modelID != 1 && modelID != 2) {
            debug("Invalid Model ID. Exiting.");
            return;
        }

        debug("Starting!");

        ModelState state = new ModelState();
        if (modelID == 1) {
            // SOMETHING
        }
        else if (modelID == 2) {
            // TODO
        }

        debug("Done.");
    }
}

package choral.examples.ozone.concurrentsend;

import java.util.function.Consumer;

public class OnData@A implements Consumer@A<String> {
    String@A workerName;

    public OnData(String@A workerName) {
        this.workerName = workerName;
    }

    @Override
    public void accept(String@A x) {
        System@A.out.println("Client got "@A + x + " from "@A + workerName);
    }
}

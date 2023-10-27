package choral.examples.futures.hello;

import java.util.function.Consumer;

public class OnData@A implements Consumer@A<String> {
    String@A workerName;

    public OnData(String@A workerName) {
        this.workerName = workerName;
    }

    @Override
    public void accept(String@A x) {
        System@A.out.println("Server got: "@A + x);
    }
}
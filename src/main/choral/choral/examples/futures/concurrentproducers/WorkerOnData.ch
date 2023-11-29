package choral.examples.futures.concurrentproducers;

import java.util.function.Consumer;

public class WorkerOnData@A implements Consumer@A<String> {
    WorkerState@A state;

    public WorkerOnData(WorkerState@A state) {
        this.state = state;
    }

    @Override
    public void accept(String@A x) {
        state.store(x);
    }
}
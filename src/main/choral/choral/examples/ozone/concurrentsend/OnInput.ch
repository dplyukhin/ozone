package choral.examples.ozone.concurrentsend;

import java.util.function.Function;

public class OnInput@A implements Function@A<Integer, String> {
    WorkerState@A state;

    public OnInput(WorkerState@A state) {
        this.state = state;
    }

    @Override
    public String@A apply(Integer@A input) {
        return state.compute(input);
    }
}

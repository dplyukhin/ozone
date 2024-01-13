package choral.examples.ozone.concurrentbuyers;

import java.util.function.Consumer;

public class OnItem@A implements Consumer@A<String> {
    ClientState@A state;

    public OnItem(ClientState@A state) {
        this.state = state;
    }

    @Override
    public void accept(String@A x) {
        state.store(x);
    }
}

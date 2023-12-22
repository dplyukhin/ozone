package choral.examples.futures.concurrentsend;

import java.util.function.Consumer;

public class OnKeyAck@A implements Consumer@A<Void> {
    ServerState@A state;
    Integer@A input;
    public OnKeyAck(ServerState@A state, Integer@A input) {
        this.state = state;
        this.input = input;
    }
    @Override
    public void accept(Void@A x) {
        state.onKeyAck(input);
    }
}
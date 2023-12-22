package choral.examples.futures.concurrentsend;

import java.util.function.Consumer;

public class OnTxtAck@A implements Consumer@A<Void> {
    ServerState@A state;
    Integer@A input;
    public OnTxtAck(ServerState@A state, Integer@A input) {
        this.state = state;
        this.input = input;
    }
    @Override
    public void accept(Void@A x) {
        state.onTxtAck(input);
    }
}
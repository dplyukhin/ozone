package choral.examples.ozone.concurrentsend;

import java.util.function.Consumer;

public class OnClientKey implements Consumer<String> {

    ClientState state_c;

    public OnClientKey(ClientState state_c) {
        this.state_c = state_c;
    }

    @Override public void accept(String x) {
        state_c.consumeKey(x);
    }
}


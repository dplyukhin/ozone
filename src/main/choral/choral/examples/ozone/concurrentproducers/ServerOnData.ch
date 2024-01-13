package choral.examples.ozone.concurrentproducers;

import java.util.function.Function;

public class ServerOnData@A implements Function@A<String,String> {
    ServerState@A state;

    public ServerOnData(ServerState@A state) {
        this.state = state;
    }

    @Override
    public String@A apply(String@A x) {
        return state.compute(x);
    }
}

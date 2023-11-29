package choral.examples.futures.concurrentbuyers;

import java.util.function.Function;

public class OnItemID@A implements Function@A<String,String> {
    ServerState@A state;

    public OnItemID(ServerState@A state) {
        this.state = state;
    }

    @Override
    public String@A apply(String@A itemID) {
        return state.sell(itemID);
    }
}
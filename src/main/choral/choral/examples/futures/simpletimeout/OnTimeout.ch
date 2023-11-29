package choral.examples.futures.simpletimeout;

import java.util.function.Consumer;

public class OnTimeout@Server implements Consumer@Server<Object> {

    String@Server tag;

    public OnTimeout(String@Server tag) {
        this.tag = tag;
    }

    @Override
    public void accept(Object@Server msg) {
        System@Server.out.println("Server timed out while waiting for "@Server + tag);
    }
}
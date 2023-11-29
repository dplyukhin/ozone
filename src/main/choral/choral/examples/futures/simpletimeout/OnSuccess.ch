package choral.examples.futures.simpletimeout;

import java.util.function.Consumer;

public class OnSuccess@Server implements Consumer@Server<String> {

    String@Server tag;

    public OnSuccess(String@Server tag) {
        this.tag = tag;
    }

    @Override
    public void accept(String@Server msg) {
        System@Server.out.println("Server got "@Server + tag + ": "@Server + msg);
    }
}
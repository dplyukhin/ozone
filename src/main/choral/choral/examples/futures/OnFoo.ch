package choral.examples.futures;

import java.util.function.Consumer;

public class OnFoo@Server implements Consumer@Server<String> {
    @Override
    public void accept(String@Server x) {
        System@Server.out.println("Server got: "@Server + x);
    }
}
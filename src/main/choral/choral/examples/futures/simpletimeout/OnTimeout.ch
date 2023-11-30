package choral.examples.futures.simpletimeout;

import java.util.function.Function;
import java.lang.Throwable;

public class OnTimeout@Server<R@X> implements Function@Server<R, String> {

    String@Server tag;

    public OnTimeout(String@Server tag) {
        this.tag = tag;
    }

    @Override
    public String@Server apply(R@Server exn) {
        System@Server.out.println("Server timed out while waiting for "@Server + tag);
        return null@Server;
    }
}
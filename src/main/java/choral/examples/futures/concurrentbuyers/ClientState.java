package choral.examples.futures.concurrentbuyers;

import java.lang.Thread;
import java.util.ArrayList;

public class ClientState {
    ArrayList<String> items;

    public ClientState() {
        this.items = new ArrayList<>();
    }

    public void store(String item) {
        items.add(item);
    }
}
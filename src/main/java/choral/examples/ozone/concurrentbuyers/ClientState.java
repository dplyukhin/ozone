package choral.examples.ozone.concurrentbuyers;

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
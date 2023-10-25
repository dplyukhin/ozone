package choral.runtime;

import java.util.concurrent.ExecutorService;
import java.util.HashMap;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;

public class AsyncChannel_A<T> extends AsyncChannel<T> {

    public AsyncChannel_A( ExecutorService executor, SymChannelImpl<Object> channel ) {
        this.executor = executor;
        this.channel = channel;
        this.futures = new HashMap<>();
        this.messages = new HashMap<>();
        listen();
    }
    
}

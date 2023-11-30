package choral.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.HashMap;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;

public class UnreliableAsyncChannel_A<T> extends UnreliableAsyncChannel<T> {

    public UnreliableAsyncChannel_A( ScheduledExecutorService executor, SymChannelImpl<Object> channel ) {
        this.executor = executor;
        this.channel = channel;
        this.futures = new HashMap<>();
        this.messages = new HashMap<>();
        this.selectionHandler = null;
        this.selections = new ArrayDeque<>();
        listen();
    }
    
}

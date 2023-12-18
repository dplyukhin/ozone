package choral.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayDeque;
import java.util.HashMap;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;

public class DelayableChannel_A<T> extends DelayableChannel<T> {

    public DelayableChannel_A( SymChannelImpl<T> channel, long maxDelayMillis ) {
        this.channel = channel;
        this.maxDelayMillis = maxDelayMillis;
        this.random = new Random();
    }
    
}

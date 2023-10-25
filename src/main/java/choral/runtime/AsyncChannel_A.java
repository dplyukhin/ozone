package choral.runtime;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;

public class AsyncChannel_A<T> extends AsyncChannel<T> {

    public AsyncChannel_A( SymChannelImpl<T> channel ) {
        this.channel = channel;
    }
    
}

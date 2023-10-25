package choral.runtime;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_B;

public class AsyncChannel_B<T> extends AsyncChannel<T> {

    public AsyncChannel_B( SymChannelImpl<T> channel ) {
        this.channel = channel;
    }
    
}

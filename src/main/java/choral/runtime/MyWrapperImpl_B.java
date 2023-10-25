package choral.runtime;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_B;

public class MyWrapperImpl_B<T> extends MyWrapperImpl<T> {

    public MyWrapperImpl_B( SymChannelImpl<T> channel ) {
        this.channel = channel;
    }
    
}

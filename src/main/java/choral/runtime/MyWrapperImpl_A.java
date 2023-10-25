package choral.runtime;

import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;

public class MyWrapperImpl_A<T> extends MyWrapperImpl<T> {

    public MyWrapperImpl_A( SymChannelImpl<T> channel ) {
        this.channel = channel;
    }
    
}

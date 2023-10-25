package choral.runtime;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import choral.channels.SymChannelImpl;
import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;

public class AsyncChannel< T > implements SymSelectChannel_A, SymSelectChannel_B {
    protected SymChannelImpl<T> channel;

 	public < M extends T > M com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b ) {
 		return this.com(line_b, tok_b);
 	}
 
 	public < S extends T > S com( int line_b, Token tok_b ) {
        System.out.println("Receiving!");
 		return channel.com();
 	}
 
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        System.out.println("Sending!");
 		return channel.com( m );
 	}
    
 	public < M extends T > Unit com( CompletableFuture<M> f, int line_a, Unit tok_a, Unit line_b, Unit tok_b ) {
        System.out.println("Sending future!");
        f.thenApply(m -> channel.com(m));
 		return Unit.id;
 	}
 
 	@Override
 	public < M extends Enum< M > > Unit select ( M m ) {
 		return channel.select( m );
 	}
 
 	@Override
 	public < M extends Enum< M > > M select ( Unit u ) {
 		return this.select();
 	}
 
 	@Override
 	public < T extends Enum< T > > T select () {
 		return channel.select();
 	}

}

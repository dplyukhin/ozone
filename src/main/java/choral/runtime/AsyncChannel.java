package choral.runtime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import choral.channels.SymChannelImpl;
import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;
import choral.utils.Pair;

// TODO: If the message can be "null", the futures/messages logic is broken.

public class AsyncChannel< T > implements SymSelectChannel_A, SymSelectChannel_B {
	protected ExecutorService executor;
    protected SymChannelImpl<Object> channel;
	protected HashMap<IntegrityKey, CompletableFuture<T>> futures;
	protected HashMap<IntegrityKey, T> messages;

	protected void listen() {
		executor.submit(() -> {
			System.out.println("Starting listener...");
			while (true) {
				Pair< IntegrityKey, T > msg = channel.com();
				IntegrityKey key = msg.left();
				T payload = msg.right();
				System.out.println("Got message with key" + key);

				synchronized(this) {
					CompletableFuture<T> future = futures.remove(key);
					if (future != null) {
						future.complete(payload);
					}
					else {
						messages.put(key, payload);
					}
				}
			}
		});
	}

 	public < M extends T > CompletableFuture<M> com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b ) {
 		return this.com(line_b, tok_b);
 	}
 
 	public < M extends T > CompletableFuture<M> com( int line_b, Token tok_b ) {
		CompletableFuture<T> future = new CompletableFuture<T>();
		IntegrityKey key = new IntegrityKey(line_b, tok_b);
		System.out.println("Setting up listener for key " + key);

		synchronized(this) {
			T payload = messages.remove(key);
			if (payload != null) {
				future.complete(payload);
			}
			else {
				futures.put(key, future);
			}
		}
 		return (CompletableFuture<M>) future;
 	}
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        return this.com(m, line_a, tok_a);
 	}
    
 
 	public < M extends T > Unit com( M m, int line_a, Token tok_a) {
        System.out.println("Sending message " + line_a + "," + tok_a);
 		return channel.com( new Pair( new IntegrityKey(line_a, tok_a), m ) );
 	}
    
 	public < M extends T > Unit com( CompletableFuture<M> f, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        f.thenApply(m -> this.com(m, line_a, tok_a));
 		return Unit.id;
 	}
 
 	@Override
 	public < M extends Enum< M > > Unit select ( M m ) {
		System.out.println("Selecting " + m);
 		return channel.select( m );
 	}
 
 	@Override
 	public < M extends Enum< M > > M select ( Unit u ) {
 		return this.select();
 	}
 
 	@Override
 	public < T extends Enum< T > > T select () {
		T m = channel.select();
		System.out.println("Got selection " + m);
 		return m;
 	}

}

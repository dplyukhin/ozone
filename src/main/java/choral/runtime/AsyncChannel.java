package choral.runtime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import choral.channels.SymChannelImpl;
import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;

// TODO: If the message can be "null", the futures/messages logic is broken.

public class AsyncChannel< T > implements SymSelectChannel_A, SymSelectChannel_B {
	protected ExecutorService executor;
    protected SymChannelImpl<Object> channel;

	/** 
	 * Whenever the process performs an asynchronous receive, it registers a 
	 * future in this map.
	 * Invariant: If `futures` contains K then `messages` does not contain K.
	 */
	protected HashMap<IntegrityKey, CompletableFuture<T>> futures;
	/** 
	 * Processes can receive messages before they have time to register a future. 
	 * If this happens, the message is stored in this map.
	 */
	protected HashMap<IntegrityKey, T> messages;
	/** 
	 * If the process is blocked and waiting for a selection, this future is non-null. 
	 * Invariant: If this field is non-null then `selections` is empty.
	 */
	protected CompletableFuture< Enum<?> > selectionHandler;
	/** 
	 * A list of selections received along this channel that have not yet been handled. 
	 */
	protected Queue< Enum<?> > selections;

	/** 
	 * Start a task that listens for incoming messages and dispatches them to the
	 * appropriate handler.
	 */
	protected void listen() {
		executor.submit(() -> {
			System.out.println("Starting listener...");
			while (true) {
				Object msg = channel.com();
				System.out.println("Got " + msg);

				if (msg instanceof DataMsg) {
					DataMsg dataMsg = (DataMsg) msg;
					// Case 1: The message is a regular com() message.
					IntegrityKey key = dataMsg.key;
					T payload = (T) dataMsg.payload;
					System.out.println("Got message with key" + key);

					// We use synchronization to maintain the `futures` invariant. 
					CompletableFuture<T> handler = null;
					synchronized(this) {
						System.out.println("Locking 1...");
						handler = futures.remove(key);
						if (handler == null) {
							messages.put(key, payload);
						}
						System.out.println("Unlocking 1...");
					}
					// We invoke `future.complete` outside the synchronization block,
					// because the future's completion may take a long time to run.
					if (handler != null) {
						handler.complete(payload);
					}
				}
				else if (msg instanceof SelectMsg) {
					SelectMsg selectMsg = (SelectMsg) msg;
					Enum<?> selection = selectMsg.selection;

					// Case 2: The message is an enum from a select().
					System.out.println("Got selection " + selection);
					
					// We use synchronization to maintain the `selectionHandler` invariant.
					CompletableFuture< Enum<?> > handler = null;
					synchronized(this) {
						System.out.println("Locking 2...");
						if (selectionHandler != null) {
							handler = selectionHandler;
							selectionHandler = null;
						}
						else {
							selections.add(selection);
						}
						System.out.println("Unlocking 2...");
					}
					// Complete the handler if it was non-null.
					if (handler != null) {
						handler.complete(selection);
					}
				}
				else {
					System.out.println("Unexpected message " + msg + " of type " + msg.getClass());
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

		// Similar to the block in listen(). We need to atomically update `messages`
		// or `futures`.
		T payload = null;
		synchronized(this) {
			System.out.println("Locking 3...");
			payload = messages.remove(key);
			if (payload == null) {
				futures.put(key, future);
			}
			System.out.println("Unlocking 3...");
		}
		// If we already got the message, go ahead and complete the future.
		if (payload != null) {
			future.complete(payload);
		}
 		return (CompletableFuture<M>) future;
 	}
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        return this.com(m, line_a, tok_a);
 	}
    
 
 	public < M extends T > Unit com( M m, int line_a, Token tok_a) {
		IntegrityKey key = new IntegrityKey(line_a, tok_a);
        System.out.println("Sending message " + key);
 		return channel.com( new DataMsg( key, m ) );
 	}
    
 	public < M extends T > Unit com( CompletableFuture<M> f, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        f.thenApply(m -> this.com(m, line_a, tok_a));
 		return Unit.id;
 	}
 
 	@Override
 	public < M extends Enum< M > > Unit select ( M m ) {
		System.out.println("Selecting " + m);
 		return channel.com( new SelectMsg(m) );
 	}
 
 	@Override
 	public < M extends Enum< M > > M select ( Unit u ) {
 		return this.select();
 	}
 
 	@Override
 	public < T extends Enum< T > > T select () {
		CompletableFuture< Enum<?> > future = new CompletableFuture< Enum<?> >();

		// We use synchronization to maintain the `selectionHandler` invariant.
		Enum<?> payload = null;
		synchronized(this) {
						System.out.println("Locking...");
			payload = this.selections.poll();
			if (payload == null) {
				this.selectionHandler = future;
			}
			System.out.println("Unlocking...");
		}
		// If we already got the selection, complete it right away.
		if (payload != null) {
			future.complete(payload);
		}

		try {
			// Block, waiting to receive a selection.
			return (T) future.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
 	}

}

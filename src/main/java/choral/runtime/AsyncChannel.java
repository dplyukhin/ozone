package choral.runtime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;

// TODO: If the message can be "null", the futures/messages logic is broken.

public class AsyncChannel< T > implements SymSelectChannel_A, SymSelectChannel_B {
	protected ScheduledExecutorService executor;
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
			Log.debug("Starting listener...");
			while (true) {
				Object msg = channel.com();
				Log.debug("Got " + msg);

				if (msg instanceof DataMsg) {
					DataMsg dataMsg = (DataMsg) msg;
					// Case 1: The message is a regular com() message.
					IntegrityKey key = dataMsg.key;
					T payload = (T) dataMsg.payload;

					// We use synchronization to maintain the `futures` invariant. 
					CompletableFuture<T> handler = null;
					synchronized(this) {
						handler = futures.remove(key);
						if (handler == null) {
							messages.put(key, payload);
						}
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
					Log.debug("Got selection " + selection);
					
					// We use synchronization to maintain the `selectionHandler` invariant.
					CompletableFuture< Enum<?> > handler = null;
					synchronized(this) {
						if (selectionHandler != null) {
							handler = selectionHandler;
							selectionHandler = null;
						}
						else {
							selections.add(selection);
						}
					}
					// Complete the handler if it was non-null.
					if (handler != null) {
						handler.complete(selection);
					}
				}
				else {
					Log.debug("Unexpected message " + msg + " of type " + msg.getClass());
				}
			}
		});
	}

 	public < M extends T > CompletableFuture<M> com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b ) {
 		return this.comWithTimeout(line_b, tok_b, 0);
 	}
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        return this.com(m, line_a, tok_a);
 	}
 
 	public < M extends T > Unit com( M m, int line_a, Token tok_a) {
		IntegrityKey key = new IntegrityKey(line_a, tok_a);
 		return channel.com( new DataMsg( key, m ) );
 	}
    
 	public < M extends T > Unit com( CompletableFuture<M> f, int line_a, Token tok_a, Unit line_b, Unit tok_b ) {
        f.thenApply(m -> this.com(m, line_a, tok_a));
 		return Unit.id;
 	}

 	public < M extends T > CompletableFuture<M> com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b, long timeout ) {
 		return this.comWithTimeout(line_b, tok_b, timeout);
 	}
 
 	public < M extends T > CompletableFuture<M> comWithTimeout( int line_b, Token tok_b, long delay ) {
		CompletableFuture<T> future = new CompletableFuture<T>();
		IntegrityKey key = new IntegrityKey(line_b, tok_b);
		Log.debug("Setting up listener for key " + key);

		// Similar to the block in listen(). We need to atomically update `messages`
		// or `futures`.
		T payload = null;
		synchronized(this) {
			payload = messages.remove(key);
			if (payload == null) {
				futures.put(key, future);
			}
		}
		// If we already got the message, go ahead and complete the future.
		if (payload != null) {
			future.complete(payload);
		}
		// Otherwise the message hasn't arrived yet; schedule the timeout if there is one.
		else if (delay > 0) {
			executor.schedule(() -> {
				future.completeExceptionally(new TimeoutException("Communication with key " + key + " timed out"));
			}, delay, TimeUnit.MILLISECONDS);
		}

 		return (CompletableFuture<M>) future;
 	}
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b, Unit timeout ) {
        return this.com(m, line_a, tok_a);
 	}
 
 	@Override
 	public < M extends Enum< M > > Unit select ( M m ) {
		Log.debug("Selecting " + m);
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
			payload = this.selections.poll();
			if (payload == null) {
				this.selectionHandler = future;
			}
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

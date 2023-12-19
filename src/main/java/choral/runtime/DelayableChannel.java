package choral.runtime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

/** An channel with simulated delays for benchmarking purposes. */
public class DelayableChannel< T > implements SymChannel_A< T >, SymChannel_B< T > {
	/** The longest possible delay, in milliseconds. */
	protected SymChannelImpl<T> channel;
	protected long maxDelayMillis;
	protected Random random;
 
	@Override
 	public < M extends T > Unit com( M m ) {
		return channel.com(m);
 	}

	@Override
	public < M extends T > M com ( Unit m ) {
		return this.com();
	}

	@Override
	public < M extends T > M com () {
		// Simulate a delayed message by delayed the thread, which is blocking.
		try {
			Thread.sleep(random.nextLong(maxDelayMillis));
		}
		catch (InterruptedException exn) {
			Log.debug("Interrupted while waiting for delayed message.");
		}
		return channel.com();
	}

	 @Override
 	public < M extends Enum< M > > Unit select ( M m ) {
 		return channel.select(m);
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

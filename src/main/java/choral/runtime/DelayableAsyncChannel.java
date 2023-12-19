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

import choral.channels.SymChannelImpl;
import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;

/** An channel with simulated delays for benchmarking purposes. */
public class DelayableAsyncChannel< T > extends AsyncChannelImpl< T > {
	/** The longest possible delay, in milliseconds. */
	protected long maxDelayMillis;
	protected Random random;

	public DelayableAsyncChannel( ScheduledExecutorService executor, SymChannelImpl<Object> channel, long maxDelayMillis ) {
		super(executor, channel);
        this.maxDelayMillis = maxDelayMillis;
        this.random = new Random();
    }
 
	@Override
 	public < M extends T > Unit com( M m, int line_a, Token tok_a) {
		IntegrityKey key = new IntegrityKey(line_a, tok_a);

		long delay = random.nextLong(maxDelayMillis);

		this.executor.schedule(() -> {
			channel.com( new DataMsg( key, m ) );
		}, delay, TimeUnit.MILLISECONDS);

		return Unit.id;
 	}

}

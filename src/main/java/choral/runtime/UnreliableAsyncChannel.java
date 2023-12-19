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

/** An unreliable channel for testing purposes. */
public class UnreliableAsyncChannel< T > extends AsyncChannel< T > {
	/** Counts how many messages this channel has been asked to send. */
	private int messageCount;
 
	@Override
 	public < M extends T > Unit com( M m, int line_a, Token tok_a) {
		IntegrityKey key = new IntegrityKey(line_a, tok_a);
		messageCount++;
		// Skip sending every other message.
		if (messageCount % 2 == 1) {
			Log.debug("Dropping message " + m + " with key " + key);
			return Unit.id;
		}
		else {
			return channel.com( new DataMsg( key, m ) );
		}
 	}

}

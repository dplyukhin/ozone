package choral.runtime;

import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.lang.Unit;

/** An unreliable channel for testing purposes. */
public class UnreliableAsyncChannel< T > extends AsyncChannelImpl< T > {
	/** Counts how many messages this channel has been asked to send. */
	private int messageCount;

    public UnreliableAsyncChannel( ScheduledExecutorService executor, SymChannelImpl<Object> channel ) {
		super(executor, channel);
    }
 
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

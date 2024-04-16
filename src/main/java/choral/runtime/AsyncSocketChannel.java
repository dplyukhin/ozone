package choral.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import choral.runtime.Serializers.ChoralSerializer;

/** 
 * A SRMW socket channel, parameterized by a serializer.
 */
public class AsyncSocketChannel implements SymChannelImpl< Object >, SymChannel_A< Object >, SymChannel_B< Object > {

	private final SocketChannel channel;
    private final ChoralSerializer< Object, ByteBuffer > serializer;

	public AsyncSocketChannel( ChoralSerializer< Object, ByteBuffer > serializer, SocketChannel channel ) {
		this.channel = channel;
        this.serializer = serializer;
	}

	public static AsyncSocketChannel connect( ChoralSerializer< Object, ByteBuffer > serializer, String hostname, int portNumber ) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			channel.configureBlocking( true );
			return new AsyncSocketChannel( serializer, channel );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public < T extends Object > T com( Unit u ) {
		return this.com();
	}

	@Override
	public < T extends Object > T com() {
		// This method doesn't need synchronization because we assumed only a single thread will read here.
        try {
			Log.debug("Waiting for transmission length...");
			ByteBuffer buffer = ByteBuffer.allocate( 4 );

			int bytesRead = 0;
			while( bytesRead < 4 ) { 
				bytesRead += channel.read( buffer );
			}

			buffer.flip();
			int transmissionLength = buffer.getInt();
			Log.debug("Receiving " + transmissionLength + " bytes...");
			buffer = ByteBuffer.allocate( transmissionLength );

			// Read from the channel, counting bytes as we go, until we have read the entire transmission.
			bytesRead = 0;
			while( bytesRead < transmissionLength ) {
				bytesRead += channel.read( buffer );
			}

			buffer.flip();
			Log.debug("Done reading.");
			return (T) this.serializer.toObject( buffer );
        } catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
        }
	}

	@Override
	public < T extends Object > Unit com( T m ) {
		// This method uses synchronization to prevent concurrent writes from being interleaved.
        try {
			ByteBuffer buf = this.serializer.fromObject( m );
			// TODO: We make an extra copy here, because we need to prepend the length of the transmission.
			// We could make this unnecessary by (a) prepending the length at the serializer itself, or
			// (b) changing the serializer API to produce an output stream.
			ByteBuffer buf2 = ByteBuffer.allocate( 4 + buf.limit() );
			buf2.putInt( buf.limit() );
			buf2.put( buf );
			buf2.flip();
            synchronized (this) {
                int n = channel.write( buf2 );
				Log.debug("Sent " + buf.limit() + " bytes");
            }
            return Unit.id;
		} catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
		}
	}

	@Override
	public < T extends Enum< T > > Unit select( T m ) {
		return this.com( m );
	}

	@Override
	public < T extends Enum< T > > T select( Unit m ) {
		return select();
	}

	@Override
	public < T extends Enum< T > > T select() {
		return this.com();
	}

}

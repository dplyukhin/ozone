package choral.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.channels.SymDataChannelImpl;
import choral.lang.Unit;
import choral.runtime.ChoralByteChannel.SymByteChannelImpl;
import choral.runtime.Serializers.ChoralSerializer;

/** 
 * A SRMW socket channel, parameterized by a serializer.
 */
public class AsyncSocketChannel implements SymChannelImpl< Object > {

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
		// This method doesn't need synchronization, because only a single thread will read here.
        try {
            int transmissionLength = recvTransmissionLength();
            ByteBuffer recv = ByteBuffer.allocate( transmissionLength );
            channel.read( recv );
            Object obj = this.serializer.toObject( recv );
            return (T) obj;
        } catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
        }
	}

	@Override
	public < T extends Object > Unit com( T m ) {
		// This method uses synchronization to prevent concurrent writes from being interleaved.
        try {
			Log.debug("Sending " + m);
			ByteBuffer buf = this.serializer.fromObject( m );
			// TODO: We make an extra copy here, because we need to prepend the length of the transmission.
			// We could make this unnecessary by (a) prepending the length at the serializer itself, or
			// (b) changing the serializer API to produce an output stream.
			ByteBuffer buf2 = ByteBuffer.allocate( 4 + buf.limit() );
			buf2.putInt( buf.limit() );
			buf2.put( buf );
            synchronized (this) {
                channel.write( buf2 );
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

	private int recvTransmissionLength() throws IOException {
		DataInputStream dis = new DataInputStream( channel.socket().getInputStream() );
		return dis.readInt();
	}

}

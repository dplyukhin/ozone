package choral.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import choral.channels.SymChannelImpl;
import choral.channels.SymDataChannelImpl;
import choral.lang.Unit;
import choral.runtime.ChoralByteChannel.SymByteChannelImpl;
import choral.runtime.Serializers.ChoralSerializer;

/** 
 * A socket channel that supports concurrent read/write. Assumes that all reads are
 * only performed by a single thread.
 */
public class AsyncSocketByteChannel implements SymChannelImpl< Object > {

	private final SocketChannel channel;
    private final ChoralSerializer< Object, ByteBuffer > serializer;

	public AsyncSocketByteChannel( ChoralSerializer< Object, ByteBuffer > serializer, SocketChannel channel ) {
		this.channel = channel;
        this.serializer = serializer;
	}

	public static AsyncSocketByteChannel connect( ChoralSerializer< Object, ByteBuffer > serializer, String hostname, int portNumber ) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			channel.configureBlocking( true );
			return new AsyncSocketByteChannel( serializer, channel );
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
        try {
            System.out.println("Getting transmission length...");
            // No need for synchronization, because only a single thread will read here.
            int transmissionLength = this.recvTransmissionLength();
            ByteBuffer recv = ByteBuffer.allocate( transmissionLength );
            System.out.println("Awaiting transmission...");
            channel.read( recv );
            System.out.println("Got it!");
            String str = new String( recv.array(), StandardCharsets.UTF_8 );
            System.out.println(str);
            Object obj = this.serializer.toObject(recv);
            System.out.println("Deserialized it!");
            return (T) obj;
        } catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
        }
	}

	@Override
	public < T extends Object > Unit com( T m ) {
        try {
            synchronized (this) {
                System.out.println("Locking 5...");
                // Use synchronization to prevent concurrent writes from being interleaved.
                ByteBuffer buf = this.serializer.fromObject(m);
                this.sendTransmissionLength( buf.limit() );
                channel.write( buf );
                System.out.println("Unlocking 5...");
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

	public int recvTransmissionLength() throws IOException {
		DataInputStream dis = new DataInputStream( channel.socket().getInputStream() );
		return dis.readInt();
	}

	public void sendTransmissionLength( int length ) throws IOException {
		DataOutputStream dos = new DataOutputStream( channel.socket().getOutputStream() );
		dos.writeInt( length );
	}

}

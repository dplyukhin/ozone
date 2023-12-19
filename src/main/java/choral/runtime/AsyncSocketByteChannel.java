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
            // No need for synchronization, because only a single thread will read here.
            int transmissionLength = this.recvTransmissionLength();
            ByteBuffer recv = ByteBuffer.allocate( transmissionLength );
            channel.read( recv );
            Object obj = deserializeObject(recv); //this.serializer.toObject(recv);
            return (T) obj;
        } catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
        }
	}

	@Override
	public < T extends Object > Unit com( T m ) {
        try {
            synchronized (this) {
                // Use synchronization to prevent concurrent writes from being interleaved.
                Log.debug("Sending " + m);
                ByteBuffer buf = serializeObject((Serializable) m); //this.serializer.fromObject(m);
                //Log.debug("Encoded and decoded as:" + serializer.toObject(buf));
                //Log.debug("Encoded as " + Arrays.toString(buf.array()) + " of length " + buf.limit());
                this.sendTransmissionLength( buf.limit() );
                channel.write( buf );
            }
            return Unit.id;
		} catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
		}
	}

    public static ByteBuffer serializeObject(Serializable obj) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);

			// Serialize the object
			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			ByteBuffer buffer = ByteBuffer.wrap(byteStream.toByteArray());

			// Close the streams
			objectOutputStream.close();
			byteStream.close();

			return buffer;
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
    }

	public static Object deserializeObject(ByteBuffer buffer) {
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer.array());
			ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);

			// Use the ObjectInputStream to deserialize the object
			Object deserializedObject = objectInputStream.readObject();

			// Close the streams
			objectInputStream.close();
			byteStream.close();

			return deserializedObject;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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

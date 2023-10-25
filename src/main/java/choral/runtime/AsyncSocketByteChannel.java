package choral.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import choral.channels.SymDataChannelImpl;
import choral.lang.Unit;
import choral.runtime.ChoralByteChannel.SymByteChannelImpl;

/** 
 * A socket channel that supports concurrent read/write. Assumes that all reads are
 * only performed by a single thread.
 */
public class AsyncSocketByteChannel implements SymDataChannelImpl< ByteBuffer > {

	private final SocketChannel channel;

	public AsyncSocketByteChannel( SocketChannel channel ) {
		this.channel = channel;
	}

	public static AsyncSocketByteChannel connect( String hostname, int portNumber ) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			channel.configureBlocking( true );
			return new AsyncSocketByteChannel( channel );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public < T extends ByteBuffer > T com( Unit u ) {
		return this.com();
	}

	@Override
	public < T extends ByteBuffer > T com() {
        try {
            // No need for synchronization, because only a single thread will read here.
            int transmissionLength = this.recvTransmissionLength();
            ByteBuffer recv = ByteBuffer.allocate( transmissionLength );
            channel.read( recv );
            return (T) recv;
        } catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
        }
	}

	@Override
	public < T extends ByteBuffer > Unit com( T m ) {
        try {
            synchronized (this) {
                // Use synchronization to prevent concurrent writes from being interleaved.
                this.sendTransmissionLength( m.limit() );
                channel.write( m );
            }
            return Unit.id;
		} catch( IOException e ) {
            throw new RuntimeException( e.getMessage() );
		}
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

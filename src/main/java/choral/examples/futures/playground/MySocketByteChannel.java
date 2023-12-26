package choral.examples.futures.playground;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import choral.runtime.Media.BlockingByteChannel;

public class MySocketByteChannel implements BlockingByteChannel {

	private final SocketChannel channel;

	public MySocketByteChannel( SocketChannel channel ) {
		this.channel = channel;
	}

	public static MySocketByteChannel connect( String hostname, int portNumber ) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			channel.configureBlocking( true );
			return new MySocketByteChannel( channel );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int read( ByteBuffer dst ) throws IOException {
		return channel.read( dst );
	}

	@Override
	public int write( ByteBuffer src ) throws IOException {
		return channel.write( src );
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public int recvTransmissionLength() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		channel.read( buf );
		buf.flip();
		return buf.getInt();
	}

	@Override
	public void sendTransmissionLength( int length ) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt( length );
		buf.flip();
		channel.write( buf );
	}

}
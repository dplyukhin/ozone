package choral.examples.ozone.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import choral.channels.SymDataChannelImpl;
import choral.lang.Unit;

public class MySocketByteChannel implements SymDataChannelImpl< ByteBuffer > {

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
	public < T extends ByteBuffer > T com( Unit u ) {
		return this.com();
	}

	@Override
	public < T extends ByteBuffer > T com() {
		try {
			ByteBuffer buffer = ByteBuffer.allocate( 4 );
			channel.read( buffer );
			buffer.flip();
			int transmissionLength = buffer.getInt();
			buffer = ByteBuffer.allocate( transmissionLength );
			channel.read( buffer );
			buffer.flip();
			return (T) buffer;
		} catch( IOException e ) {
			throw new RuntimeException( "Could not read from channel" );
		}
	}

	@Override
	public < T extends ByteBuffer > Unit com( T m ) {
		ByteBuffer buffer = ByteBuffer.allocate( 4 + m.remaining() );
		buffer.putInt( m.remaining() );
		buffer.put( m );
		buffer.flip();
		try {
			channel.write( buffer );
			return Unit.id;
		} catch( IOException e ) {
			throw new RuntimeException( "Could not write to channel" );
		}
	}

}
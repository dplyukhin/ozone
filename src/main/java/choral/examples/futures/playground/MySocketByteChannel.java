package choral.examples.futures.playground;

import java.io.DataInputStream;
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
			channel.configureBlocking( false );
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
			// Read the size of the message
			ByteBuffer buffer = ByteBuffer.allocate( 4 );
			int read = 0;
			while( read < 4 ) {
				read += channel.read( buffer );
			}
			buffer.flip();
			int size = buffer.getInt();

			// Read the message
			buffer = ByteBuffer.allocate( size );
			read = 0;
			while( read < size ) {
				read += channel.read( buffer );
			}
			buffer.flip();
			return ( T ) buffer;
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
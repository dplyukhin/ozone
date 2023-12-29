package choral.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import choral.runtime.Serializers.ChoralSerializer;

public class AsyncServerSocketChannel {

	private final ServerSocketChannel listeningChannel;
    private final ChoralSerializer< Object, ByteBuffer > serializer; 

	private AsyncServerSocketChannel( ChoralSerializer< Object, ByteBuffer > serializer, String hostname, int portNumber ) {
		try {
			this.listeningChannel = ServerSocketChannel.open();
            this.serializer = serializer;
			listeningChannel.socket().bind( new InetSocketAddress( hostname, portNumber ) );
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not initialise listening channel" );
		}
	}

	public static AsyncServerSocketChannel at( ChoralSerializer< Object, ByteBuffer > serializer, String hostname, int portNumber ) {
		return new AsyncServerSocketChannel( serializer, hostname, portNumber );
	}

	public AsyncSocketChannel getNext() throws IOException {
		SocketChannel channel = listeningChannel.accept();
		channel.configureBlocking( true );
		return new AsyncSocketChannel( this.serializer, channel );
	}

	public void close() {
		try {
			listeningChannel.close();
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not properly close the listening channel" );
		}
	}

	public boolean isOpen() {
		return listeningChannel.isOpen();
	}

}

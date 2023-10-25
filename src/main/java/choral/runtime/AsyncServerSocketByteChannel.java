package choral.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AsyncServerSocketByteChannel {

	private final ServerSocketChannel listeningChannel;

	private AsyncServerSocketByteChannel( String hostname, int portNumber ) {
		try {
			this.listeningChannel = ServerSocketChannel.open();
			listeningChannel.socket().bind( new InetSocketAddress( hostname, portNumber ) );
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not initialise listening channel" );
		}
	}

	public static AsyncServerSocketByteChannel at( String hostname, int portNumber ) {
		return new AsyncServerSocketByteChannel( hostname, portNumber );
	}

	public AsyncSocketByteChannel getNext() throws IOException {
		SocketChannel channel = listeningChannel.accept();
		channel.configureBlocking( true );
		return new AsyncSocketByteChannel( channel );
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

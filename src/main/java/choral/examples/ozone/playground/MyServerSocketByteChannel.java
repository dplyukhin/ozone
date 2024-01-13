package choral.examples.ozone.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MyServerSocketByteChannel {

	private final ServerSocketChannel listeningChannel;

	private MyServerSocketByteChannel( String hostname, int portNumber ) {
		try {
			this.listeningChannel = ServerSocketChannel.open();
			listeningChannel.socket().bind( new InetSocketAddress( hostname, portNumber ) );
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not initialise listening channel" );
		}
	}

	public static MyServerSocketByteChannel at( String hostname, int portNumber ) {
		return new MyServerSocketByteChannel( hostname, portNumber );
	}

	public MySocketByteChannel getNext() throws IOException {
		SocketChannel channel = listeningChannel.accept();
		channel.configureBlocking( true );
		return new MySocketByteChannel( channel );
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


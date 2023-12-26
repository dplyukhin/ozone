package choral.examples.futures.playground;

import java.io.IOException;
import java.nio.ByteBuffer;

import choral.runtime.Media.SocketByteChannel;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Client {

    public static void main(String[] args) {
        WrapperByteChannelImpl ch =
            new WrapperByteChannelImpl(
                SocketByteChannel.connect(
                    Server.HOST, Server.PORT
                )
            );

        for (int i = 0; i < 50; i++) {
            System.out.println("Iteration " + i + ": Client starting at time " + System.currentTimeMillis());
            ch.com( ByteBuffer.wrap(new byte[] { 1, 1, 1, 1 }));
            ch.com();
            System.out.println("Iteration " + i + ": Client ending at time " + System.currentTimeMillis());
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
        }
    }
}

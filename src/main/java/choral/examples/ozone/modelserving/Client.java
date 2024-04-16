package choral.examples.ozone.modelserving;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.imageio.ImageIO;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ConcurrentServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client {

    public static void debug(String msg) {
        Log.debug("CLIENT: " + msg);
    }

    public static void main(String[] args) {

        // Read the image filename from args
        if (args.length < 1) {
            debug("Image filename not provided. Exiting.");
            return;
        }
        String filePath = args[0];

        int requestInterval = 1000 / Config.IMAGES_PER_SECOND;

        Image img = null;
        try {
            BufferedImage original = ImageIO.read(new File(filePath));
            java.awt.Image scaled = original.getScaledInstance(224, 224, java.awt.Image.SCALE_DEFAULT);

            BufferedImage bufferedScaled = new BufferedImage(244, 244, BufferedImage.TYPE_INT_ARGB);
            bufferedScaled.getGraphics().drawImage(scaled, 0, 0, null);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedScaled, "PNG", outputStream);
            img = new Image(outputStream.toByteArray());
        } 
        catch (IOException e) {
            debug("Couldn't find image " + filePath + ". Exiting.");
            return;
        }

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        debug("Connecting to other nodes...");

        AsyncServerSocketChannel worker1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_WORKER1
        );
        AsyncServerSocketChannel worker2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_WORKER2
        );
        AsyncServerSocketChannel batcher_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_BATCHER
        );

        try {
            SymChannel_A<Object> chW1 = worker1_listener.getNext();
            debug("Worker 1 connected.");
            SymChannel_A<Object> chW2 = worker2_listener.getNext();
            debug("Worker 2 connected.");

            // Only the synchronous version or the async version will be used, depending
            // on whether USE_OZONE is true or false.
            Object chB;
            if (Config.USE_OZONE) {
                chB = new AsyncChannelImpl<Object>(threadPool, batcher_listener.getNext());
            }
            else {
                chB = batcher_listener.getNext();
            }
            debug("Batcher connected.");

            debug("Waiting for the batcher to be ready...");
            if (Config.USE_OZONE) {
                ((AsyncChannel_A<Object>) chB).select();
            }
            else {
                ((SymChannel_A<Object>) chB).select();
            }
            debug("Client starting!");

            /******************** START **********************/

            ConcurrentHashMap<Integer, Long> startTimes = new ConcurrentHashMap<>();
            ConcurrentHashMap<Integer, Long> endTimes = new ConcurrentHashMap<>();

            ClientState state = new ClientState();
            long requestStart = System.currentTimeMillis();

            for (int i = 0; i < Config.IMAGES_PER_CLIENT; i++) {

                startTimes.put(i, requestStart);

                if (Config.USE_OZONE) {

                    AsyncChannel_A<Object> chB_async = (AsyncChannel_A<Object>) chB;

                    CompletableFuture<Predictions> batch =
                        new ConcurrentServing_Client(chW1, chW2, chB_async)
                            .onImage(img, i, state, new Token(i));

                    if (batch != null) {
                        batch.thenAccept(predictions -> {
                            long end = System.currentTimeMillis();
                            for (int imgID : predictions.getImgIDs()) {
                                endTimes.put(imgID, end);
                            }
                        });
                    }

                }
                else {

                    SymChannel_A<Object> chB_sync = (SymChannel_A<Object>) chB;
                    
                    Predictions batch =
                        new InOrderServing_Client(chW1, chW2, chB_sync)
                            .onImage(img, i, state);

                    if (batch != null) {
                        long end = System.currentTimeMillis();
                        for (int imgID : batch.getImgIDs()) {
                            endTimes.put(imgID, end);
                        }
                    }

                }

                // Compute the time when the next request will start, and
                // sleep until that happens if necessary.
                requestStart += requestInterval;
                long sleepTime = requestStart - System.currentTimeMillis();
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } 
                    catch (InterruptedException e) {
                        debug("Sleep interrupted.");
                    }
                }
            }

            for (int i = 0; i < Config.IMAGES_PER_CLIENT; i++) {
                debug("Image " + i + " latency: " + (endTimes.get(i) - startTimes.get(i)) + " ms");
            }
            

        } 
        catch (IOException e) {
            debug("Error in client, aborting: " + e.getMessage());
        }
        finally {
            threadPool.shutdownNow();
            worker1_listener.close();
            worker2_listener.close();
            batcher_listener.close();
            debug("Client done.");
        }
    }
}

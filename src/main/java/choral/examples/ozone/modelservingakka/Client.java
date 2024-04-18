package choral.examples.ozone.modelservingakka;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import javax.imageio.ImageIO;

import choral.Log;
import choral.examples.ozone.modelserving.ClientState;
import choral.examples.ozone.modelserving.Config;
import choral.examples.ozone.modelserving.Image;

public class Client {

    public static void debug(String msg) {
        Log.debug("CLIENT: " + msg);
    }

    public static Image readImage(String filePath) {
        try {
            BufferedImage original = ImageIO.read(new File(filePath));
            java.awt.Image scaled = original.getScaledInstance(224, 224, java.awt.Image.SCALE_DEFAULT);

            BufferedImage bufferedScaled = new BufferedImage(244, 244, BufferedImage.TYPE_INT_ARGB);
            bufferedScaled.getGraphics().drawImage(scaled, 0, 0, null);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedScaled, "PNG", outputStream);
            return new Image(outputStream.toByteArray());
        } 
        catch (IOException e) {
            debug("Couldn't find image " + filePath + ". Exiting.");
            return null;
        }
    }

    public static void main(String[] args) {

        // Read the image filename from args
        if (args.length < 1) {
            debug("Image filename not provided. Exiting.");
            return;
        }
        String filePath = args[0];
        Image img = readImage(filePath);

        try {

            debug(
                "Starting. Batch size: " + Config.BATCH_SIZE + 
                ", Images per second: " + Config.REQUESTS_PER_SECOND +
                ", use Ozone: " + Config.USE_OZONE
            );

            HashMap<Integer, Long> startTimes = new HashMap<>();
            HashMap<Integer, Long> endTimes = new HashMap<>();

            ClientState state = new ClientState();
            long requestStart = System.currentTimeMillis();
            long benchmarkStart = 0;
            long benchmarkEnd = 0;

            for (int i = 0; i < Config.WARMUP_ITERATIONS + Config.NUM_REQUESTS; i++) {

                if (i == Config.WARMUP_ITERATIONS) {
                    benchmarkStart = System.currentTimeMillis();
                }

                if (i >= Config.WARMUP_ITERATIONS) {
                    startTimes.put(i - Config.WARMUP_ITERATIONS, requestStart);
                }

                // BENCHMARK GOES HERE

                // Compute the time when the next request will start, and
                // sleep until that happens if necessary.
                requestStart += Config.REQUEST_INTERVAL;
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

            /********************** WRITING DATA *************************/

            String suffix = 
                "akka-rate" + Config.EFFECTIVE_REQUEST_RATE + "-batch" + Config.BATCH_SIZE + ".csv";

            String latencyPath = "data/modelserving/latency-" + suffix;
            String throughputPath = "data/modelserving/throughput-" + suffix;

            debug("Writing to " + throughputPath + "...");
            final BufferedWriter throughputWriter = Files.newBufferedWriter(Paths.get(throughputPath), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try (throughputWriter) {
                long throughput = Config.NUM_REQUESTS * 1000 / (benchmarkEnd - benchmarkStart);
                throughputWriter.write(Long.toString(throughput));
                throughputWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            debug("Wrote to " + throughputPath + ".");

            debug("Writing to " + latencyPath + "...");
            final BufferedWriter latencyWriter = Files.newBufferedWriter(Paths.get(latencyPath), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try (latencyWriter) {
                for (int i = 0; i < Config.NUM_REQUESTS; i++) {
                    long latency = endTimes.get(i) - startTimes.get(i);
                    latencyWriter.write(Long.toString(latency));
                    latencyWriter.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            debug("Wrote to " + latencyPath + ".");
            

        } 
        catch (IOException e) {
            debug("Error in client, aborting: " + e.getMessage());
        }
        finally {
            debug("Client done.");
        }
    }
}

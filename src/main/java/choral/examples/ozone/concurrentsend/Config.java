package choral.examples.ozone.concurrentsend;

public class Config {
    public static String SERVER_HOST = System.getProperty("serverHost", "localhost");

    public static final int CLIENT_PORT = 8667;
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int WORKER_MAX_COMPUTE_TIME_MILLIS = 10;
    public static final int NUM_ITERATIONS = 4000;
}

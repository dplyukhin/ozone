package choral.examples.ozone.concurrentproducers;

public class Config {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 10;
    public static final long SERVER_MAX_COMPUTE_TIME_MILLIS = 5;
    public static final long REQUESTS_PER_SECOND = 60;

    public static final long REQUEST_INTERVAL = 1000 / REQUESTS_PER_SECOND;
}

package choral.examples.ozone.concurrentproducers;

public class Config {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 600;
    public static final long SERVER_MAX_COMPUTE_TIME_MILLIS = 5;

    public static final int REQUESTS_PER_SECOND = 
        Integer.parseInt(System.getProperty("requestsPerSecond", "60"));

    public static final long REQUEST_INTERVAL = 1000 / REQUESTS_PER_SECOND;
}

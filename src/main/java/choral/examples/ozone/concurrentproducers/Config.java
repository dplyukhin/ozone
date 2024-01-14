package choral.examples.ozone.concurrentproducers;

public class Config {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 1000;
    public static final int ITERATION_PERIOD_MILLIS = 100;
    public static final long SERVER_MAX_COMPUTE_TIME_MILLIS = 5;
}

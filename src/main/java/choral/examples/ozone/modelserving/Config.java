package choral.examples.ozone.modelserving;

public class Config {
    public static final String HOST = "localhost";

    public static final int CLIENT_FOR_WORKER1 = 8667;
    public static final int CLIENT_FOR_WORKER2 = 8668;
    public static final int CLIENT_FOR_BATCHER = 8669;
    public static final int BATCHER_FOR_MODEL1 = 8670;
    public static final int BATCHER_FOR_MODEL2 = 8671;
    public static final int BATCHER_FOR_WORKER1 = 8672;
    public static final int BATCHER_FOR_WORKER2 = 8673;
    public static final int WORKER1_FOR_MODEL1 = 8674;
    public static final int WORKER1_FOR_MODEL2 = 8675;
    public static final int WORKER2_FOR_MODEL1 = 8676;
    public static final int WORKER2_FOR_MODEL2 = 8677;

    public static final int NUM_REQUESTS = 1000;
    public static final int BATCH_SIZE =
        Integer.parseInt(System.getProperty("batchSize", "10"));
    public static final int REQUESTS_PER_SECOND = 
        Integer.parseInt(System.getProperty("requestsPerSecond", "120"));
    public static final boolean USE_OZONE = 
        Boolean.parseBoolean(System.getProperty("useOzone", "true"));
}

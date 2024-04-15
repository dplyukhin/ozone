package choral.examples.ozone.modelserving;

public class Config {
    public static final String HOST = "localhost";
    public static final int CLIENT_PORT = 8667;
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int BATCHER_PORT = 8670;
    public static final int MODEL1_PORT = 8671;
    public static final int MODEL2_PORT = 8672;
    public static final int BATCH_SIZE = 10;
    public static final int IMAGES_PER_CLIENT = 1000;
    public static final int IMAGES_PER_SECOND = 60;
}

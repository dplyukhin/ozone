package choral;

public class Log {
    public static final boolean DEBUG = false;

    public static void debug(String str) {
        if (DEBUG) {
            System.out.println(str);
        }
    }

    public static void info(String str) {
        System.out.println(str);
    }
}

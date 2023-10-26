package choral.runtime;

public class AsyncMessage {
    public static interface Msg {}

    public static record DataMsg(IntegrityKey key, Object payload) implements Msg {}

    public static record SelectMsg<T extends Enum<T>>(T selection) implements Msg {}
}

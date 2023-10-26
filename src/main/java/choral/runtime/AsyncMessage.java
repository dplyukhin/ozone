package choral.runtime;

import choral.runtime.Serializers.KryoSerializable;

public class AsyncMessage {
    @KryoSerializable
    public static interface Msg {}

    @KryoSerializable
    public static record DataMsg(IntegrityKey key, Object payload) implements Msg {}

    @KryoSerializable
    public static record SelectMsg<T extends Enum<T>>(T selection) implements Msg {}
}

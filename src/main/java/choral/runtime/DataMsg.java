package choral.runtime;

import choral.runtime.Serializers.KryoSerializable;
import java.io.Serializable;

@KryoSerializable
public class DataMsg implements Serializable {
    IntegrityKey key;
    Object payload;

    public DataMsg() {}

    public DataMsg(IntegrityKey key, Object payload) {
        this.key = key;
        this.payload = payload;
    }
}

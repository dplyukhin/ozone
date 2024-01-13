package choral.runtime;

import java.io.Serializable;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class SelectMsg<T extends Enum<T>> implements Serializable {
    T selection;

    public SelectMsg() {}

    public SelectMsg(T selection) {
        this.selection = selection;
    }

    @Override
    public String toString() {
        return "SelectMsg(" + selection + ")";
    }
}

package choral.runtime;

import java.io.Serializable;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Token implements Serializable {
    public int value;

    public Token() {}

    public Token(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Token))
            return false;
        Token that = (Token) o;
        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}

package choral.runtime;

import java.util.Objects;

import choral.runtime.Serializers.KryoSerializable;
import java.io.Serializable;

@KryoSerializable
public class IntegrityKey implements Serializable {
    int line;
    Token token;

    public IntegrityKey() {}

    public IntegrityKey(int line, Token token) {
        this.line = line;
        this.token = token;
    }
    public int line() { return this.line; }
    public Token token() { return this.token; }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof IntegrityKey))
            return false;
        IntegrityKey that = (IntegrityKey) o;
        return this.line == that.line && this.token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, token);
    }
}

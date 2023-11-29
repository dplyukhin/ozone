package choral.runtime;

import java.io.Serializable;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Token implements Serializable {
    public int head;
    public Token tail;

    public Token() {}

    public Token(int head) {
        this.head = head;
        this.tail = null;
    }

    public Token(int head, Token tail) {
        this.head = head;
        this.tail = tail;
    }

    public Token nextToken(int line) {
        return new Token(line, this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Token))
            return false;
        Token that = (Token) o;
        return this.head == that.head && this.tail == that.tail;
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, tail);
    }

    @Override
    public String toString() {
        return "Token(" + head + "," + tail + ")";
    }
}

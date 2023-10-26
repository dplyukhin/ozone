package choral.runtime;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public record IntToken(int value) implements Token {

}

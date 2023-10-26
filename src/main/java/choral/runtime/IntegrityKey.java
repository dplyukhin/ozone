package choral.runtime;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public record IntegrityKey(int line, Token token) {
}

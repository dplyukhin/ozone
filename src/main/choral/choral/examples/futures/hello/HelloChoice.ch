package choral.examples.futures.hello;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public enum HelloChoice@R { 
    YES, 
    NO 
}
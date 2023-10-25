package choral.channels;

import java.util.concurrent.CompletableFuture;
import choral.lang.Unit;

public interface AsyncDiDataChannel_A< T > {
	< S extends T > Unit com( S m );
	< S extends T > Unit com( CompletableFuture<S> m );
}

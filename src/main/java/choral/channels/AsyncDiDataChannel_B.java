package choral.channels;

import java.util.concurrent.CompletableFuture;
import choral.lang.Unit;

public interface AsyncDiDataChannel_B< T > {
	< S extends T > CompletableFuture<S> com( Unit m );
	< S extends T > CompletableFuture<S> com();
}

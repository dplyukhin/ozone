package choral.runtime;

import java.util.concurrent.CompletableFuture;

import choral.channels.SymSelectChannel_A;
import choral.channels.SymSelectChannel_B;
import choral.lang.Unit;

public interface AsyncChannel< T > extends SymSelectChannel_A, SymSelectChannel_B {

 	public < M extends T > CompletableFuture<M> com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b );
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b );
 
 	public < M extends T > Unit com( M m, int line_a, Token tok_a);
    
 	public < M extends T > Unit com( CompletableFuture<M> f, int line_a, Token tok_a, Unit line_b, Unit tok_b );

 	public < M extends T > CompletableFuture<M> com( Unit u, Unit line_a, Unit tok_a, int line_b, Token tok_b, long timeout );
 
 	public < M extends T > CompletableFuture<M> comWithTimeout( int line_b, Token tok_b, long delay );
	
 	public < M extends T > Unit com( M m, int line_a, Token tok_a, Unit line_b, Unit tok_b, Unit timeout );
 
 	@Override
 	public < M extends Enum< M > > Unit select ( M m );
 
 	@Override
 	public < M extends Enum< M > > M select ( Unit u );
 
 	@Override
 	public < T extends Enum< T > > T select ();

}

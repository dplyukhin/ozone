package choral.examples.futures.simpletimeout;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class SimpleTimeout@( Client, Server )< T@X, R@Y > {
	public void go( 
      AsyncChannel@( Client, Server )< String > ch, 
      Token@Client tok_c,
      Token@Server tok_s
   ) { 
      OnSuccess@Server onMsg1 = new OnSuccess@Server("msg1"@Server);
      OnSuccess@Server onMsg2 = new OnSuccess@Server("msg2"@Server);
      OnTimeout@Server noMsg1 = new OnTimeout@Server("msg1"@Server);
      OnTimeout@Server noMsg2 = new OnTimeout@Server("msg2"@Server);

      CompletableFuture@Server<String> msg1 = ch.<String>com( "msg1"@Client, 0@Client, tok_c, 0@Server, tok_s );
      //msg1.thenAccept(onMsg1).exceptionally(noMsg1);

      CompletableFuture@Server<String> msg2 = ch.<String>com( "msg2"@Client, 1@Client, tok_c, 1@Server, tok_s );
      //msg2.thenAccept(onMsg2).exceptionally(noMsg2);
   }
}
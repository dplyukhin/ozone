package choral.examples.futures.simpletimeout;

import java.lang.Throwable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class SimpleTimeout@( Client, Server )< T@X, R@Y > {
	public void go( 
      AsyncChannel@( Client, Server )< String > ch, 
      Token@Client tok_c,
      Token@Server tok_s
   ) { 
      // Define callbacks.
      Consumer@Server<String> onMsg1 = new OnSuccess@Server("msg1"@Server);
      Consumer@Server<String> onMsg2 = new OnSuccess@Server("msg2"@Server);
      Function@Server<Throwable, String> noMsg1 = new OnTimeout@Server<Throwable>("msg1"@Server);
      Function@Server<Throwable, String> noMsg2 = new OnTimeout@Server<Throwable>("msg2"@Server);

      // Client sends a message to the server, which is handled asynchronously and with a timeout.
      CompletableFuture@Server<String> msg1 = ch.<String>com( "msg1"@Client, 0@Client, tok_c, 0@Server, tok_s, 1000@Server );
      msg1.exceptionally(noMsg1).thenAccept(onMsg1);

      // Client sends a second message.
      CompletableFuture@Server<String> msg2 = ch.<String>com( "msg2"@Client, 1@Client, tok_c, 1@Server, tok_s, 1000@Server );
      msg2.exceptionally(noMsg2).thenAccept(onMsg2);
   }
}
package choral.examples.ozone.hello;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import choral.channels.AsyncChannel;
import choral.runtime.Token;

public class HelloRoles@( Client, Server ) {
	public void sayHello(
      AsyncChannel@( Client, Server )< String > ch,
      String@Client msg,
      Token@Client tok_c,
      Token@Server tok_s
   ) {
      CompletableFuture@Server<String> foo = null@Server;
      if (msg == "Hiya!"@Client) {
         ch.< HelloChoice >select( HelloChoice@Client.YES );
         foo = ch.<String>com( msg, 1@Client, tok_c, 1@Server, tok_s );
      }
      else {
         ch.< HelloChoice >select( HelloChoice@Client.NO );
         foo = ch.<String>com( "bogus"@Client, 2@Client, tok_c, 2@Server, tok_s );
      }

      Consumer@Server<String> onFoo = new OnFoo@Server();
      foo.thenAccept(onFoo);
   }
}

package choral.examples.futures;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

class HelloRoles@( Client, Server )< T@X, R@Y > {
	public void sayHello( 
      AsyncChannel@( Client, Server )< String > ch, 
      String@Client msg,
      Token@Client tok_c,
      Token@Server tok_s
   ) { 
      String@Server foo = null@Server;
      if (msg == "Hiya!"@Client) {
         ch.< HelloChoice >select( HelloChoice@Client.YES );
         foo = ch.<String>com( msg, 1@Client, tok_c, 1@Server, tok_s ); 
      }
      else {
         ch.< HelloChoice >select( HelloChoice@Client.NO );
         foo = ch.<String>com( "bogus"@Client, 2@Client, tok_c, 2@Server, tok_s ); 
      }

      System@Server.out.println("Server got: "@Server + foo);
   }
}
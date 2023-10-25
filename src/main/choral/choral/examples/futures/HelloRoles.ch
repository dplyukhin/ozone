package choral.examples.futures;

import choral.channels.DiChannel;
import choral.runtime.MyWrapperImpl;
//import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

class HelloRoles@( Client, Server )< T@X, R@Y > {
	public void sayHello( 
      MyWrapperImpl@( Client, Server )< String > ch, 
      String@Client msg 
   ) { 
      String@Server foo = null@Server;
      if (msg == "Hiya!"@Client) {
         ch.< HelloChoice >select( HelloChoice@Client.YES );
         foo = ch.<String>com( msg, 1@Client, 1@Server ); 
      }
      else {
         ch.< HelloChoice >select( HelloChoice@Client.NO );
         foo = ch.<String>com( "bogus"@Client, 2@Client, 2@Server ); 
      }

      System@Server.out.println("Server got: "@Server + foo);
   }
}
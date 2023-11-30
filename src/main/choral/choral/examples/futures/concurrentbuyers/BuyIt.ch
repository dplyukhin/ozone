package choral.examples.futures.concurrentbuyers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class BuyIt@( Client, Server ) {
   /** Buy an item */
	public void buyItem( 
      AsyncChannel@( Client, Server )< String > ch, 
      ClientState@Client state_c,
      ServerState@Server state_s,
      String@Client itemID,
      Token@Client tok_c,
      Token@Server tok_s
   ) { 
      // Client sends ID to server.
      CompletableFuture@Server<String> futureItemID = ch.<String>com( itemID, 1@Client, tok_c, 1@Server, tok_s );

      // Server sells the item.
      Function@Server<String,String> onItemID = new OnItemID@Server(state_s);
      CompletableFuture@Server<String> futureItem_s = futureItemID.<String>thenApply( onItemID );

      // Server sends back the item.
      CompletableFuture@Client<String> futureItem_c = ch.<String>com( futureItem_s, 2@Server, tok_s, 2@Client, tok_c );

      // Client stores the item.
      Consumer@Client<String> onItem = new OnItem@Client(state_c);
      futureItem_c.thenAccept(onItem);
   }

   /** Buy a sequence of items */
	public void buyItems( 
      AsyncChannel@( Client, Server )< String > ch, 
      ClientState@Client state_c,
      ServerState@Server state_s,
      Iterator@Client< String > itemIDs,
      Token@Client tok_c,
      Token@Server tok_s
   ) { 
      if (itemIDs.hasNext()) {
         ch.< Choice >select( Choice@Client.NEXT );
         buyItem( ch, state_c, state_s, itemIDs.next(), tok_c.nextToken(0@Client), tok_s.nextToken(0@Server) );
         buyItems( ch, state_c, state_s, itemIDs, tok_c.nextToken(1@Client), tok_s.nextToken(1@Server) ); 
      } else { 
         ch.< Choice >select( Choice@Client.DONE ); 
      } 
   }
}
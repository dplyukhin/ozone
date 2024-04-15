package choral.examples.ozone.modelserving;

import choral.runtime.Token;
import choral.channels.SymChannel;

/**
 * Model serving choreography, adapted from 
 * https://github.com/stephanie-wang/ownership-nsdi2021-artifact/tree/main/model-serving
 */
public class ModelServing@( Client, Worker1, Worker2, Batcher, Model1, Model2 ) {

   SymChannel@( Client, Worker1 )< Object > ch_c_w1;
   SymChannel@( Client, Worker2 )< Object > ch_c_w2;

   public ModelServing(
      SymChannel@( Client, Worker1 )< Object > ch_c_w1,
      SymChannel@( Client, Worker2 )< Object > ch_c_w2
   ) {
      this.ch_c_w1 = ch_c_w1;
      this.ch_c_w2 = ch_c_w2;
   }

	public void go( 

      // Local state at each process
      ClientState@Client clientState, 

      // Token at each participant
      Token@Client tok_c, Token@Worker1 tok_w1, Token@Worker2 tok_w2, Token@Batcher tok_b, Token@Model1 tok_m1, Token@Model2 tok_m2

   ) { 
      // Choose which Worker will preprocess the data.
      int@Client worker_id = clientState.chooseWorker(2@Client);
      if (worker_id == 0@Client) {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         System@Client.out.println("Chose Worker 1"@Client);
      }
      else {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         System@Client.out.println("Chose Worker 2"@Client);
      }
   }
}

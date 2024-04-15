package choral.examples.ozone.modelserving;

import choral.runtime.Token;
import choral.channels.SymChannel;
import choral.channels.AsyncChannel;

import java.awt.Image;

/**
 * Model serving choreography, adapted from 
 * https://github.com/stephanie-wang/ownership-nsdi2021-artifact/tree/main/model-serving
 */
public class ModelServing@( Client, Worker1, Worker2, Batcher, Model1, Model2 ) {

   SymChannel@( Client, Worker1 )< Object > ch_c_w1;
   SymChannel@( Client, Worker2 )< Object > ch_c_w2;
   SymChannel@( Client, Batcher )< Object > ch_c_b;
   AsyncChannel@( Batcher, Model1 )< Object > ch_b_m1;
   AsyncChannel@( Batcher, Model2 )< Object > ch_b_m2;

   public ModelServing(
      SymChannel@( Client, Worker1 )< Object > ch_c_w1,
      SymChannel@( Client, Worker2 )< Object > ch_c_w2,
      SymChannel@( Client, Batcher )< Object > ch_c_b,
      AsyncChannel@( Batcher, Model1 )< Object > ch_b_m1,
      AsyncChannel@( Batcher, Model2 )< Object > ch_b_m2
   ) {
      this.ch_c_w1 = ch_c_w1;
      this.ch_c_w2 = ch_c_w2;
      this.ch_c_b  = ch_c_b;
      this.ch_b_m1 = ch_b_m1;
      this.ch_b_m2 = ch_b_m2;
   }

	public void onImage( 
      Image@Client img, BatchID@Client batchID,
      
      // Local state at each process
      ClientState@Client clientState, 
      WorkerState@Worker1 worker1State, 
      WorkerState@Worker2 worker2State, 
      BatcherState@Batcher batcherState, 

      // Token at each participant
      Token@Client tok_c, Token@Worker1 tok_w1, Token@Worker2 tok_w2, Token@Batcher tok_b, Token@Model1 tok_m1, Token@Model2 tok_m2

   ) { 
      // Choose which Worker will preprocess the data.
      int@Client worker_id = clientState.chooseWorker(2@Client);
      if (worker_id == 0@Client) {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         System@Client.out.println("Chose Worker 1"@Client);

         Image@Worker1 img_w = ch_c_w1.< Image >com(img);
         BatchID@Worker1 batchID_w = ch_c_w1.< BatchID >com(batchID);

         Image@Worker1 processed = worker1State.preprocess(img_w);
         worker1State.store(batchID_w, processed);
      }
      else {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         System@Client.out.println("Chose Worker 2"@Client);

         Image@Worker2 img_w = ch_c_w2.< Image >com(img);
         BatchID@Worker2 batchID_w = ch_c_w2.< BatchID >com(batchID);

         Image@Worker2 processed = worker2State.preprocess(img_w);
         worker2State.store(batchID_w, processed);
      }

      // Tell the batcher about it.
      BatchID@Batcher batchID_b = ch_c_b.< BatchID >com(batchID);
      batcherState.newImage(batchID_b);
      
      int@Batcher model_id = batcherState.chooseModel(2@Batcher);
      if (model_id == 0@Batcher) {
         ch_b_m1.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         ch_b_m2.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         System@Batcher.out.println("Sending batch to Model 1"@Batcher);
      }
      else {
         ch_b_m1.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         ch_b_m2.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         System@Batcher.out.println("Sending batch to Model 2"@Batcher);
      }
   }
}

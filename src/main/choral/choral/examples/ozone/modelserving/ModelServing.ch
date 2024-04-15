package choral.examples.ozone.modelserving;

import choral.runtime.Token;
import choral.channels.SymChannel;
import choral.channels.AsyncChannel;

import java.awt.Image;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

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
   SymChannel@( Batcher, Worker1 )< Object > ch_b_w1;
   SymChannel@( Batcher, Worker2 )< Object > ch_b_w2;
   SymChannel@( Model1, Worker1 )< Object > ch_m1_w1;
   SymChannel@( Model1, Worker2 )< Object > ch_m1_w2;
   SymChannel@( Model2, Worker1 )< Object > ch_m2_w1;
   SymChannel@( Model2, Worker2 )< Object > ch_m2_w2;

   public ModelServing(
      SymChannel@( Client, Worker1 )< Object > ch_c_w1,
      SymChannel@( Client, Worker2 )< Object > ch_c_w2,
      SymChannel@( Client, Batcher )< Object > ch_c_b,
      AsyncChannel@( Batcher, Model1 )< Object > ch_b_m1,
      AsyncChannel@( Batcher, Model2 )< Object > ch_b_m2,
      SymChannel@( Batcher, Worker1 )< Object > ch_b_w1,
      SymChannel@( Batcher, Worker2 )< Object > ch_b_w2,
      SymChannel@( Model1, Worker1 )< Object > ch_m1_w1,
      SymChannel@( Model1, Worker2 )< Object > ch_m1_w2,
      SymChannel@( Model2, Worker1 )< Object > ch_m2_w1,
      SymChannel@( Model2, Worker2 )< Object > ch_m2_w2
   ) {
      this.ch_c_w1 = ch_c_w1;
      this.ch_c_w2 = ch_c_w2;
      this.ch_c_b  = ch_c_b;
      this.ch_b_m1 = ch_b_m1;
      this.ch_b_m2 = ch_b_m2;
      this.ch_b_w1 = ch_b_w1;
      this.ch_b_w2 = ch_b_w2;
      this.ch_m1_w1 = ch_m1_w1;
      this.ch_m1_w2 = ch_m1_w2;
      this.ch_m2_w1 = ch_m2_w1;
      this.ch_m2_w2 = ch_m2_w2;
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
      // Choose which Worker will preprocess the data, and send them both the batchID since they'll need it later.
      int@Client worker_id = clientState.chooseWorker(2@Client);
      BatchID@Worker1 batchID_w1 = ch_c_w1.< BatchID >com(batchID);
      BatchID@Worker2 batchID_w2 = ch_c_w2.< BatchID >com(batchID);
      if (worker_id == 0@Client) {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         System@Client.out.println("Chose Worker 1"@Client);

         Image@Worker1 img_w = ch_c_w1.< Image >com(img);

         Image@Worker1 processed = worker1State.preprocess(img_w);
         worker1State.store(batchID_w1, processed);
      }
      else {
         ch_c_w1.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         ch_c_w2.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         System@Client.out.println("Chose Worker 2"@Client);

         Image@Worker2 img_w = ch_c_w2.< Image >com(img);

         Image@Worker2 processed = worker2State.preprocess(img_w);
         worker2State.store(batchID_w2, processed);
      }

      // Tell the batcher about it.
      BatchID@Batcher batchID_b = ch_c_b.< BatchID >com(batchID);
      batcherState.newImage(batchID_b);
      
      int@Batcher model_id = batcherState.chooseModel(2@Batcher);
      if (model_id == 0@Batcher) {
         ch_b_m1.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         ch_b_m2.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         ch_b_w1.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         ch_b_w2.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
         System@Batcher.out.println("Sending batch to Model 1"@Batcher);

         // Send the batch ID to the model
         BatchID@Model1 batchID_m = 
           ch_b_m1.< BatchID >com( batchID_b, 1@Batcher, tok_b, 1@Model1, tok_m1 ).join();

         // The preprocessors send their data to the model
         ArrayList@Model1< Image > batch1 = ch_m1_w1.< ArrayList<Image> >com( worker1State.dumpBatch( batchID_w1 ) );
         ArrayList@Model1< Image > batch2 = ch_m1_w2.< ArrayList<Image> >com( worker2State.dumpBatch( batchID_w2 ) );
      }
      else {
         ch_b_m1.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         ch_b_m2.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         ch_b_w1.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         ch_b_w2.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
         System@Batcher.out.println("Sending batch to Model 2"@Batcher);

         // Send the batch ID to the model
         BatchID@Model2 batchID_m = 
           ch_b_m2.< BatchID >com( batchID_b, 1@Batcher, tok_b, 1@Model2, tok_m2 ).join();

         // The preprocessors send their data to the model
         ArrayList@Model2< Image > batch1 = ch_m2_w1.< ArrayList<Image> >com( worker1State.dumpBatch( batchID_w1 ) );
         ArrayList@Model2< Image > batch2 = ch_m2_w2.< ArrayList<Image> >com( worker2State.dumpBatch( batchID_w2 ) );
      }
   }
}

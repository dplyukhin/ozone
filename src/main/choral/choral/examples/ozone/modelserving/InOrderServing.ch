package choral.examples.ozone.modelserving;

import choral.channels.SymChannel;
import java.util.ArrayList;

/**
 * Model serving choreography, adapted from 
 * https://github.com/stephanie-wang/ownership-nsdi2021-artifact/tree/main/model-serving
 */
public class InOrderServing@( Client, Worker1, Worker2, Batcher, Model1, Model2 ) {

   SymChannel@( Client, Worker1 )< Object > chCW1;
   SymChannel@( Client, Worker2 )< Object > chCW2;
   SymChannel@( Client, Batcher )< Object > chCB;
   SymChannel@( Batcher, Model1 )< Object > chBM1;
   SymChannel@( Batcher, Model2 )< Object > chBM2;
   SymChannel@( Batcher, Worker1 )< Object > chBW1;
   SymChannel@( Batcher, Worker2 )< Object > chBW2;
   SymChannel@( Worker1, Model1 )< Object > chM1W1;
   SymChannel@( Worker2, Model1 )< Object > chM1W2;
   SymChannel@( Worker1, Model2 )< Object > chM2W1;
   SymChannel@( Worker2, Model2 )< Object > chM2W2;

   public InOrderServing(
      SymChannel@( Client, Worker1 )< Object > chCW1,
      SymChannel@( Client, Worker2 )< Object > chCW2,
      SymChannel@( Client, Batcher )< Object > chCB,
      SymChannel@( Batcher, Model1 )< Object > chBM1,
      SymChannel@( Batcher, Model2 )< Object > chBM2,
      SymChannel@( Batcher, Worker1 )< Object > chBW1,
      SymChannel@( Batcher, Worker2 )< Object > chBW2,
      SymChannel@( Worker1, Model1 )< Object > chM1W1,
      SymChannel@( Worker2, Model1 )< Object > chM1W2,
      SymChannel@( Worker1, Model2 )< Object > chM2W1,
      SymChannel@( Worker2, Model2 )< Object > chM2W2
   ) {
      this.chCW1 = chCW1;
      this.chCW2 = chCW2;
      this.chCB  = chCB;
      this.chBM1 = chBM1;
      this.chBM2 = chBM2;
      this.chBW1 = chBW1;
      this.chBW2 = chBW2;
      this.chM1W1 = chM1W1;
      this.chM1W2 = chM1W2;
      this.chM2W1 = chM2W1;
      this.chM2W2 = chM2W2;
   }

	public Predictions@Client onImage( 
      Image@Client img, int@Client imgid,
      
      // Local state at each process
      ClientState@Client clientState, 
      WorkerState@Worker1 worker1State, 
      WorkerState@Worker2 worker2State, 
      BatcherState@Batcher batcherState, 
      ModelState@Model1 model1State,
      ModelState@Model2 model2State
   ) { 
      // Choose which Worker will preprocess the data, and send them both the batchid since they'll need it later.
      int@Client workerID = clientState.chooseWorker(2@Client);
      if (workerID == 0@Client) {
         chCW1.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );
         chCW2.< WorkerChoice >select( WorkerChoice@Client.WORKER1 );

         Image@Worker1 imgW = chCW1.< Image >com(img);
         int@Worker1 imgidW1 = chCW1.< Integer >com(imgid);

         Image@Worker1 processed = worker1State.preprocess(imgW);
         worker1State.store(imgidW1, processed);
      }
      else {
         chCW1.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );
         chCW2.< WorkerChoice >select( WorkerChoice@Client.WORKER2 );

         Image@Worker2 imgW = chCW2.< Image >com(img);
         int@Worker2 imgidW2 = chCW2.< Integer >com(imgid);

         Image@Worker2 processed = worker2State.preprocess(imgW);
         worker2State.store(imgidW2, processed);
      }

      // Tell the batcher about it.
      int@Batcher imgidB = chCB.< Integer >com( imgid );
      batcherState.newImage(imgidB);
      
      BatchIDs@Batcher batchids = batcherState.getBatchIfFull();

      if (batchids != null@Batcher) {
         chBM1.< BatchReady >select( BatchReady@Batcher.READY );
         chBM2.< BatchReady >select( BatchReady@Batcher.READY );
         chCB.< BatchReady >select( BatchReady@Batcher.READY );
         chBW1.< BatchReady >select( BatchReady@Batcher.READY );
         chBW2.< BatchReady >select( BatchReady@Batcher.READY );
 
         int@Batcher modelid = batcherState.chooseModel(2@Batcher);
         if (modelid == 0@Batcher) {
            chBM1.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
            chBM2.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
            chBW1.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );
            chBW2.< ModelChoice >select( ModelChoice@Batcher.MODEL1 );

            // Send the batch IDs to the model and workers
            BatchIDs@Model1 batchidsM = chBM1.< BatchIDs >com( batchids );
            BatchIDs@Worker1 batchidsW1 = chBW1.< BatchIDs >com( batchids );
            BatchIDs@Worker2 batchidsW2 = chBW2.< BatchIDs >com( batchids );

            // The preprocessors send their data to the model
            ProcessedImages@Model1 batch1 = chM1W1.< ProcessedImages >com( worker1State.dumpBatch( batchidsW1 ) );
            ProcessedImages@Model1 batch2 = chM1W2.< ProcessedImages >com( worker2State.dumpBatch( batchidsW2 ) );

            // Model outputs predictions on the data, and sends it to the client through the batcher
            batch1.addAll(batch2);
            Predictions@Model1 predictions = model1State.classify(batch1);
            Predictions@Batcher predictionsB = chBM1.< Predictions >com( predictions );
            return chCB.< Predictions >com( predictionsB );
         }
         else {
            chBM1.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
            chBM2.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
            chBW1.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );
            chBW2.< ModelChoice >select( ModelChoice@Batcher.MODEL2 );

            // Send the batch IDs to the model and workers
            BatchIDs@Model2 batchidsM = chBM2.< BatchIDs >com( batchids );
            BatchIDs@Worker1 batchidsW1 = chBW1.< BatchIDs >com( batchids );
            BatchIDs@Worker2 batchidsW2 = chBW2.< BatchIDs >com( batchids );

            // The preprocessors send their data to the model
            ProcessedImages@Model2 batch1 = chM2W1.< ProcessedImages >com( worker1State.dumpBatch( batchidsW1 ) );
            ProcessedImages@Model2 batch2 = chM2W2.< ProcessedImages >com( worker2State.dumpBatch( batchidsW2 ) );

            // Model outputs predictions on the data, and sends it to the client through the batcher
            batch1.addAll(batch2);
            Predictions@Model2 predictions = model2State.classify(batch1);
            Predictions@Batcher predictionsB = chBM2.< Predictions >com( predictions );
            return chCB.< Predictions >com( predictionsB );
         }
      } 
      else {
         chBM1.< BatchReady >select( BatchReady@Batcher.NOT_READY );
         chBM2.< BatchReady >select( BatchReady@Batcher.NOT_READY );
         chCB.< BatchReady >select( BatchReady@Batcher.NOT_READY );
         chBW1.< BatchReady >select( BatchReady@Batcher.NOT_READY );
         chBW2.< BatchReady >select( BatchReady@Batcher.NOT_READY );
         
         return null@Client;
      }
   }
}

package choral.examples.ozone.modelserving;

import choral.runtime.Token;

/**
 * Model serving choreography, adapted from 
 * https://github.com/stephanie-wang/ownership-nsdi2021-artifact/tree/main/model-serving
 */
public class ModelServing@( Client, Worker1, Worker2, Batcher, Model1, Model2 ) {
	public void go( 
      // // Local state at each process
      ClientState@Client clientState, 
      // WorkerState@Worker1 worker1State,
      // WorkerState@Worker2 worker2State,
      // BatcherState@Batcher batcherState,
      // ModelState@Model1 model1State,
      // ModelState@Model2 model2State,
      // Token at each participant
      Token@Client tok_c, Token@Worker1 tok_w1, Token@Worker2 tok_w2, Token@Batcher tok_b, Token@Model1 tok_m1, Token@Model2 tok_m2
   ) { 
      // Choose which Worker will preprocess the data.
      int@Client worker_id = clientState.chooseWorker(2@Client);
      if (worker_id == 0@Client) {
         // TODO Send selection to each worker
         System@Client.out.println("Chose Worker 1"@Client);
      }
      else {
         // TODO Send selection to each worker
         System@Client.out.println("Chose Worker 2"@Client);
      }
   }
}

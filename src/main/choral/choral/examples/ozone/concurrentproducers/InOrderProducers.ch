package choral.examples.ozone.concurrentproducers;

import choral.channels.SymChannel;

/**
 * In-order version of ConcurrentProducers.
 */
public class InOrderProducers@( Worker1, Worker2, Server ) {
	public void go( 
      SymChannel@( Worker1, Server )< Object > ch1, 
      SymChannel@( Worker2, Server )< Object > ch2, 
      WorkerState@Worker1 state1,
      WorkerState@Worker2 state2,
      ServerState@Server state_s,
      String@Worker1 input1,
      String@Worker2 input2
   ) { 
      // Workers produce data.
      String@Worker1 x1 = state1.produce(input1);
      String@Worker2 x2 = state2.produce(input2);

      // Workers send data to the server, server computes results, and sends results back to the clients.
      String@Server f_x1 = ch1.<String>com( x1 );
      String@Worker1 f_y1_w1 = ch1.<String>com( state_s.compute(f_x1) );
      String@Server f_x2 = ch2.<String>com( x2 );
      String@Worker2 f_y2_w2 = ch2.<String>com( state_s.compute(f_x2) );

      // Producers store the data.
      state1.store(f_y1_w1);
      state2.store(f_y2_w2);
   }
}

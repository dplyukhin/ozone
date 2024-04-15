package choral.examples.ozone.modelserving;

import choral.runtime.Token;

/**
 * Model serving choreography, adapted from 
 * https://github.com/stephanie-wang/ownership-nsdi2021-artifact/tree/main/model-serving
 */
public class ModelServing@( Client, Preprocessor1, Preprocessor2, Batcher, Model1, Model2 ) {
	public void go( 
      // Token at each participant
      Token@Client tok_c, Token@Preprocessor1 tok_p1, Token@Preprocessor2 tok_p2, Token@Batcher tok_b, Token@Model1 tok_m1, Token@Model2 tok_m2
   ) { 
   }
}

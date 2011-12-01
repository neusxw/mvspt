package strategy;

import java.util.Random;

import util.Lambda;
import util.Response;

/**
 * Randomly chooses between cooperate and defect, ignoring the history
 * 
 * @author Theo
 */
public class AlwaysRandom extends Strategy {
  static Random rand = new Random();

  public AlwaysRandom() {
    super();
    lambda = new Lambda(rand.nextDouble());
  }
  
  @Override
  public Response respond() {
    Random rand = new Random();
    double random = rand.nextDouble();
    return (random < 0.5 ? Response.C : Response.D);
  }
  
  @Override
  public String name() {
    return "Always Random";
  }

  @Override
  public String author() {
    return "Theodore Boyd";
  }
}
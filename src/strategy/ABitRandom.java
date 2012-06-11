package strategy;

import util.Lambda;
import util.NashEquilibrium;
import util.Response;

public class ABitRandom extends Strategy {
  public ABitRandom() {
    super();
    lambda = new Lambda(0.1);
  }

  @Override
  public Response respond() {
    if (getRoundsPlayed() != 0) {
      Response myHistoryRound_1 = getLastResponsePair().get(0);
      Response oppHistoryRound_1 = getLastResponsePair().get(1);

      if (myHistoryRound_1 == Response.C && oppHistoryRound_1 == Response.C) {
        lambda.noChange();
      } else if (myHistoryRound_1 == Response.C && oppHistoryRound_1 == Response.D) {
        lambda.incrementValue();
      } else if (myHistoryRound_1 == Response.D && oppHistoryRound_1 == Response.C) {
        lambda.decrementValue();
      } else if (myHistoryRound_1 == Response.D && oppHistoryRound_1 == Response.D) {
        lambda.incrementValue();
      }

    } else {
      lambda.noChange();
    }

    Response r = NashEquilibrium.getEquilibrium(lambda);

    if (r == Response.D) {
      if (NashEquilibrium.getRandomNumber(1, 20) < 5) r = Response.C;
    } else if (r == Response.C) {
      if (NashEquilibrium.getRandomNumber(1, 20) < 5) r = Response.D;
    }

    return r;
  }

  @Override
  public String name() {
    return "A Bit Random";
  }

  @Override
  public String author() {
    return "Xiuyi Fan";
    // Contact email: x.fan09@imperial.ac.uk
  }
}

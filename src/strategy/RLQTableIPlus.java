package strategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import util.GameSettings;
import util.Lambda;
import util.Response;

public class RLQTableIPlus extends Strategy {

  private static double[][] Qtable = new double[105][2];
  private int LastState;
  private int LastAction;
  private int CurrentState;
  private int CurrentAction;
  private double ExplorePercentage;
  private static int Counter;
  private static double LastLambda;
  private static double CoopRatio = GameSettings.RL_coopRatio;
  private static double learningRate = GameSettings.RL_learningRate;
  private static double discountFactor = GameSettings.RL_discountFactor;
  private static double ccLambdaA = GameSettings.RL_ccLambdaA;
  private static double ccLambdaB = GameSettings.RL_ccLambdaB;
  private static double cdLambda = GameSettings.RL_cdLambda;
  private static double dcLambda = GameSettings.RL_dcLambda;
  private static double ddLambda = GameSettings.RL_ddLambda;
  private static double gamma = GameSettings.GAMMA;

  public RLQTableIPlus() {
    super();
    int gammaSwitch = (int) (gamma * 10);
    switch (gammaSwitch) {
      case 0:
        CoopRatio = 0.0;
        learningRate = 0.0;
        discountFactor = 0.0;
        ccLambdaA = 0.0;
        ccLambdaB = 4.0;
        cdLambda = 4.0;
        dcLambda = 10.0;
        ddLambda = 10.0;
        break;
      case 2:
        CoopRatio = 0.0;
        learningRate = 0.5;
        discountFactor = 0.0;
        ccLambdaA = 4.0;
        ccLambdaB = 0.0;
        cdLambda = 0.0;
        dcLambda = 10.0;
        ddLambda = 10.0;
        break;
      case 4:
        CoopRatio = 0.5;
        learningRate = 1.0;
        discountFactor = 0.5;
        ccLambdaA = 10.0;
        ccLambdaB = 4.0;
        cdLambda = 0.0;
        dcLambda = 0.0;
        ddLambda = 10.0;
        break;
      case 6:
        CoopRatio = 0.5;
        learningRate = 0.5;
        discountFactor = 0.5;
        ccLambdaA = 2.0;
        ccLambdaB = 2.0;
        cdLambda = 4.0;
        dcLambda = 0.0;
        ddLambda = 0.0;
        break;
      case 8:
        CoopRatio = 1.0;
        learningRate = 1.0;
        discountFactor = 1.0;
        ccLambdaA = 10.0;
        ccLambdaB = 4.0;
        cdLambda = 10.0;
        dcLambda = 0.0;
        ddLambda = 0.0;
      case 10:
        CoopRatio = 1.0;
        learningRate = 1.0;
        discountFactor = 0.0;
        ccLambdaA = 10.0;
        ccLambdaB = 8.0;
        cdLambda = 0.0;
        dcLambda = 0.0;
        ddLambda = 4.0;
        break;
      default:
        break;
    }
    lambda = new Lambda(CoopRatio);
    initialQ();
    Counter = 0;
    ExplorePercentage = 0;
    LastLambda = CoopRatio;
  }

  @Override
  public Response respond() {

    Response FinalAnswer;
    Response OppLast, MyLast;

    Counter += 1;

    // First round
    if (getRoundsPlayed() == 0) {
      lambda.noChange();
      LastState = (int) (lambda.getValue() * 100);
      LastAction = 0;
      LastLambda = lambda.getValue();
      return Response.C;
    } else {
      OppLast = getLastResponsePair().get(1);
      MyLast = getLastResponsePair().get(0);

      if (MyLast == Response.C && OppLast == Response.C) lambda.decrementValue();
      else if (MyLast == Response.C && OppLast == Response.D) lambda.incrementValue();
      else if (MyLast == Response.D && OppLast == Response.C) lambda.decrementValue();
      else if (MyLast == Response.D && OppLast == Response.D) lambda.incrementValue();

      CurrentState = (int) (lambda.getValue() * 100);
      FinalAnswer = learningResult(OppLast);
      LastLambda = lambda.getValue();

      return FinalAnswer;
    }

  }

  public Response learningResult(Response OppLastState) {
    double Reward;
    int OppLast;

    // Get opponent's last action
    if (OppLastState == Response.C) OppLast = 0;
    else OppLast = 1;

    Reward = getReward(OppLast);
    CurrentAction = getBestAction(CurrentState);
    Qtable[LastState][LastAction] += learningRate * (Reward + discountFactor * (Qtable[CurrentState][CurrentAction] - Qtable[LastState][LastAction]));

    LastState = CurrentState;
    LastAction = CurrentAction;

    if (CurrentAction == 0) return Response.C;
    else return Response.D;

  }

  public int getBestAction(int state) {
    ExplorePercentage = -(5.0 / GameSettings.N) * Counter + 5;

    if (rand.nextInt(100) < ExplorePercentage) {
      return rand.nextInt(2);
    } else {
      if (Qtable[state][0] >= Qtable[state][1]) return 0;
      else return 1;
    }
  }

  public double getReward(int OppLast) {
    double Vlambda = LastLambda;

    // Both cooperate
    if (LastAction == 0 && OppLast == 0) {
      return ccLambdaA * (1 - Vlambda) + ccLambdaB * Vlambda;
    } else if (LastAction == 0 && OppLast == 1) {
      return cdLambda * Vlambda;
    } else if (LastAction == 1 && OppLast == 0) {
      return dcLambda * (1 - Vlambda);
    } else {
      return ddLambda * 1 - Vlambda;
    }
  }

  public static void initialQ() {

    for (int i = 0; i <= 100; ++i) {
      double lambda = i / 100.0;

      Qtable[i][0] = CoopRatio * (ccLambdaA * (1 - lambda) + ccLambdaB * lambda) + (1 - CoopRatio) * (ccLambdaB * lambda);
      Qtable[i][1] = CoopRatio * (dcLambda * (1 - lambda)) + (1 - CoopRatio) * (lambda);
    }
  }

  public void printQtable() {
    try {
      FileWriter file = new FileWriter("Qtable.txt");
      BufferedWriter writer = new BufferedWriter(file);
      for (int i = 0; i <= 100; i++) {
        writer.append(Qtable[i][0] + "\n" + Qtable[i][1] + "\n");
      }
      writer.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }

  }

  @Override
  public String name() {
    return "RL QTable I+";
  }

  @Override
  public String author() {
    return "Theodore Boyd";
  }

}

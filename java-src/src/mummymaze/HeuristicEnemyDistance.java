package mummymaze;

import agent.Heuristic;

public class HeuristicEnemyDistance extends Heuristic<MummyMazeProblem, MummyMazeState> {

    @Override
    public double compute(MummyMazeState state) {
        return state.computeDistanceClosestEnemy();
    }

    @Override
    public String toString() {
        return "Distance from closest enemy";
    }
}

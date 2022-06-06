package mummymaze.heuristics;

import agent.Heuristic;
import mummymaze.MummyMazeProblem;
import mummymaze.MummyMazeState;

public class HeuristicEnemyAndGoalDistance extends Heuristic<MummyMazeProblem, MummyMazeState> {

    @Override
    public double compute(MummyMazeState state) {
        return state.computeDistanceToClosestEnemy() + state.computeDistanceToGoal();
    }

    @Override
    public String toString() {
        return "Distance from closest enemy and goal";
    }

}

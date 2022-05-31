package mummymaze;

import agent.Heuristic;

public class HeuristicExitDistance extends Heuristic<MummyMazeProblem, MummyMazeState> {

    @Override
    public double compute(MummyMazeState state) {
        return state.computeExitDistance();
    }

    @Override
    public String toString() {
        return "Distance to the exit";
    }
}

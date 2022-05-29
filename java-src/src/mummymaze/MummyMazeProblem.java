package mummymaze;

import agent.Action;
import agent.Problem;

import java.util.LinkedList;
import java.util.List;

public class MummyMazeProblem extends Problem<MummyMazeState> {

    protected List<Action> actions;

    public MummyMazeProblem(MummyMazeState initialState) {
        super(initialState);
        actions = new LinkedList<>() {{
            add(new ActionDown());
            add(new ActionUp());
            add(new ActionRight());
            add(new ActionLeft());
        }};
    }

    @Override
    public List<Action<MummyMazeState>> getActions(MummyMazeState state) {
        List<Action<MummyMazeState>> possibleActions = new LinkedList<>();

        //If the hero has died, you can't keep going, no possible actions
        if (state.isHeroDead()){
            state.getMatrix()[0][0] = TileType.TRAP;
            return possibleActions;
        }

        for (Action action : actions)
            if (action.isValid(state)) 
                possibleActions.add(action);
        return possibleActions;
    }

    @Override
    public MummyMazeState getSuccessor(MummyMazeState state, Action action) {
        MummyMazeState successor = state.clone();

        successor.executeActionQuietly(action);
        //action.execute(successor);
        return successor;
    }

    @Override
    public boolean isGoal(MummyMazeState state) {
        return state.isAtGoal();
    }

    @Override
    public double computePathCost(List<Action> path) {
        return path.size();
    }
}

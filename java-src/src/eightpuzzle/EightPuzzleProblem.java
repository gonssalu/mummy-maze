package eightpuzzle;

import agent.Action;
import agent.Problem;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EightPuzzleProblem extends Problem<EightPuzzleState> {

    protected List<Action> actions;
    private final EightPuzzleState goalState;

    public EightPuzzleProblem(EightPuzzleState initialState) {
        super(initialState);
        actions = new LinkedList<Action>() {{
            add(new ActionDown());
            add(new ActionUp());
            add(new ActionRight());
            add(new ActionLeft());
        }};
        goalState = new EightPuzzleState(EightPuzzleState.GOAL_MATRIX);
    }

    @Override
    public List<Action<EightPuzzleState>> getActions(EightPuzzleState state) {
        List<Action<EightPuzzleState>> possibleActions = new LinkedList<>();

        for (Action action : actions) {
            if (action.isValid(state)) {
                possibleActions.add(action);
            }
        }
        return possibleActions;
    }

    @Override
    public EightPuzzleState getSuccessor(EightPuzzleState state, Action action){
        EightPuzzleState successor = state.clone();
        action.execute(successor);
        return successor;
    }

    @Override
    public boolean isGoal(EightPuzzleState state) {
        return goalState.equals(state);
    }

    @Override
    public double computePathCost(List<Action> path) {
        return path.size();
    }

    public EightPuzzleState getGoalState() {
        return goalState;
    }
}

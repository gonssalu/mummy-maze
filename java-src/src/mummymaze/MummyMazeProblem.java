package mummymaze;

import agent.Action;
import agent.Problem;
import mummymaze.actions.*;
import mummymaze.util.TileType;

import java.awt.*;
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
            add(new ActionStay());
        }};
    }

    @Override
    public List<Action<MummyMazeState>> getActions(MummyMazeState state) {
        List<Action<MummyMazeState>> possibleActions = new LinkedList<>();

        //If the hero has died, you can't keep going, no possible actions
        System.out.print(state.hashCode() + " - ");
        if (state.isHeroDead()){
            System.out.println("dead");
            System.out.println(state.toString() + "\n\n\n");
            return possibleActions;
        }

        System.out.println("alive");
        System.out.println(state.toString() + "\n\n\n");
        for (Action action : actions)
            if (action.isValid(state)) 
                possibleActions.add(action);
        return possibleActions;
    }

    @Override
    public MummyMazeState getSuccessor(MummyMazeState state, Action action) {
        MummyMazeState successor = state.clone();

        successor.executeAction(action);
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

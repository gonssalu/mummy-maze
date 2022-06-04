package mummymaze.actions;

import agent.Action;
import mummymaze.MummyMazeState;

public class ActionLeft extends Action<MummyMazeState> {

    public ActionLeft() {
        super(1);
    }

    @Override
    public void execute(MummyMazeState state) {
        state.moveLeft();
        state.setAction(this);
    }

    @Override
    public boolean isValid(MummyMazeState state) {
        return state.canMoveLeft();
    }
}
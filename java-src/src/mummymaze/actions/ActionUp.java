package mummymaze.actions;

import agent.Action;
import mummymaze.MummyMazeState;

public class ActionUp extends Action<MummyMazeState> {

    public ActionUp() {
        super(1);
    }

    @Override
    public void execute(MummyMazeState state) {
        state.moveUp();
        state.setAction(this);
    }

    @Override
    public boolean isValid(MummyMazeState state) {
        return state.canMoveUp();
    }
}

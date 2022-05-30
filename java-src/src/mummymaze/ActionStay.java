package mummymaze;

import agent.Action;

public class ActionStay extends Action<MummyMazeState> {

    public ActionStay() {
        super(1);
    }

    @Override
    public void execute(MummyMazeState state) {
        state.setAction(this);
    }

    @Override
    public boolean isValid(MummyMazeState state) {
        return true;
    }
}

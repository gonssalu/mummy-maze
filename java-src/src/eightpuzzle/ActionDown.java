package eightpuzzle;

import agent.Action;

public class ActionDown extends Action<EightPuzzleState>{

    public ActionDown(){
        super(1);
    }

    @Override
    public void execute(EightPuzzleState state){
        state.moveDown();
        state.setAction(this);
    }

    @Override
    public boolean isValid(EightPuzzleState state){
        return state.canMoveDown();
    }
}
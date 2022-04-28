package gui;

import mummymaze.MummyMazeEvent;
import mummymaze.MummyMazeListener;
import mummymaze.MummyMazeState;
import javax.swing.table.AbstractTableModel;

public class PuzzleTableModel extends AbstractTableModel implements MummyMazeListener {

    private MummyMazeState puzzle;

    public PuzzleTableModel(MummyMazeState puzzle) {
        if(puzzle == null){
            throw new NullPointerException("Puzzle cannot be null");
        }
        this.puzzle = puzzle;
        this.puzzle.addListener(this);
    }

    @Override
    public int getColumnCount() {
        return puzzle.getNumLines();
    }

    @Override
    public int getRowCount() {
        return puzzle.getNumColumns();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return puzzle.getTileValue(row, col);
    }

    @Override
    public void puzzleChanged(MummyMazeEvent pe){
        fireTableDataChanged();
        try{
            Thread.sleep(500);
        }catch(InterruptedException ignore){
        }
    }

    public void setPuzzle(MummyMazeState puzzle){
        if(puzzle == null){
          throw new NullPointerException("Puzzle cannot be null");
        }
        this.puzzle.removeListener(this);
        this.puzzle = puzzle;
        puzzle.addListener(this);
        fireTableDataChanged();
    }
}

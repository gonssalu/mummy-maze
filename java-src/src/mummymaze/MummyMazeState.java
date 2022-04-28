package mummymaze;

import agent.Action;
import agent.State;

import java.util.ArrayList;
import java.util.Arrays;

public class MummyMazeState extends State implements Cloneable {
    private final TileType[][] matrix;
    //Listeners
    private final transient ArrayList<MummyMazeListener> listeners = new ArrayList<>(3);
    private int heroRow;
    private int heroCol;

    public MummyMazeState(TileType[][] matrix) {
        this.matrix = new TileType[matrix.length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];
                if (this.matrix[i][j] == TileType.HERO) {
                    heroRow = i;
                    heroCol = j;
                }
            }
        }
    }

    @Override
    public void executeAction(Action action) {
        action.execute(this);
        firePuzzleChanged();
    }

    public boolean canMoveUp() {
        return (heroCol != 0) &&
                !TileType.blocksVerticalPassage(matrix[heroRow - 1][heroCol]) &&
                !TileType.isDangerous(matrix[heroRow - 2][heroCol]);
    }

    public boolean canMoveDown() {
        return (heroCol != matrix.length - 1) &&
                !TileType.blocksVerticalPassage(matrix[heroRow + 1][heroCol]) &&
                !TileType.isDangerous(matrix[heroRow + 2][heroCol]);
    }

    public boolean canMoveLeft() {
        return (heroRow != 0) &&
                !TileType.blocksHorizontalPassage(matrix[heroRow][heroCol - 1]) &&
                !TileType.isDangerous(matrix[heroRow][heroCol - 2]);
    }

    public boolean canMoveRight() {
        return (heroRow != matrix[0].length - 1) &&
                !TileType.blocksHorizontalPassage(matrix[heroRow][heroCol + 1]) &&
                !TileType.isDangerous(matrix[heroRow][heroCol + 2]);
    }

    /*
     * In the next four methods we don't verify if the actions are valid.
     * This is done in method executeActions in class EightPuzzleProblem.
     * Doing the verification in these methods would imply that a clone of the
     * state was created whether the operation could be executed or not.
     */
    public void moveUp() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        heroRow -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveDown() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        heroRow += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveRight() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        heroCol += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveLeft() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        heroCol -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public double computeTilesOutOfPlace() {
        int h = 0;
        /*for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix.length; j++)
                if (matrix[i][j] != 0 && matrix[i][j] != finalState.matrix[i][j]) h++;*/
        return h;
    }

    public double computeTileDistances() {
        double h = 0;
        /*for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix.length; j++)
                if (this.matrix[i][j] != 0) // Blank is ignored so that the heuristic is admissible
                    h += Math.abs(i - rowsFinalMatrix[this.matrix[i][j]]) + Math.abs(j - colsFinalMatrix[this.matrix[i][j]]);*/
        //TODO
        return h;
    }

    public int getNumRows() {
        return matrix.length;
    }

    public int getNumColumns() {
        return matrix[0].length;
    }

    public TileType getTileValue(int row, int col) {
        if (!isValidPosition(row, col)) {
            throw new IndexOutOfBoundsException("Invalid position!");
        }
        return matrix[row][col];
    }

    public boolean isValidPosition(int row, int column) {
        return row >= 0 && row < matrix.length && column >= 0 && column < matrix[0].length;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MummyMazeState)) {
            return false;
        }

        MummyMazeState o = (MummyMazeState) other;
        if (matrix.length != o.matrix.length) {
            return false;
        }

        return Arrays.deepEquals(matrix, o.matrix);
    }

    @Override
    public int hashCode() {
        return 97 * 7 + Arrays.deepHashCode(this.matrix);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (TileType[] tileRow : matrix) {
            buffer.append('\n');
            for (int j = 0; j < matrix.length; j++) {
                buffer.append(tileRow[j].getIdentifier());
                buffer.append(' ');
            }
        }
        return buffer.toString();
    }

    @Override
    public MummyMazeState clone() {
        return new MummyMazeState(matrix);
    }

    public synchronized void removeListener(MummyMazeListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    public synchronized void addListener(MummyMazeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void firePuzzleChanged() {
        for (MummyMazeListener listener : listeners) {
            listener.puzzleChanged(null);
        }
    }

    public TileType[][] getMatrix() {
        return matrix;
    }
}

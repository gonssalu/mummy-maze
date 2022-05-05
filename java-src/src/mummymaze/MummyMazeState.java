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
    private int exitRow;
    private int exitCol;

    public MummyMazeState(TileType[][] matrix) {
        this.matrix = new TileType[matrix.length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];

                if (this.matrix[i][j] == TileType.HERO) {
                    heroRow = i;
                    heroCol = j;
                }

                if (this.matrix[i][j] == TileType.EXIT) {
                    exitRow = i;
                    exitCol = j;
                }
            }
        }
    }

    @Override
    public void executeAction(Action action) {
        action.execute(this);
        fireMazeChanged();
    }

    public boolean canMoveUp() {
        return (heroCol != 0) &&
                TileType.canVerticallyPass(matrix[heroRow - 1][heroCol]) &&
                TileType.isSafe(matrix[heroRow - 2][heroCol]);
    }

    public boolean canMoveDown() {
        return (heroCol != getNumRows() - 1) &&
                TileType.canVerticallyPass(matrix[heroRow + 1][heroCol]) &&
                TileType.isSafe(matrix[heroRow + 2][heroCol]);
    }

    public boolean canMoveLeft() {
        return (heroRow != 0) &&
                TileType.canHorizontallyPass(matrix[heroRow][heroCol - 1]) &&
                TileType.isSafe(matrix[heroRow][heroCol - 2]);
    }

    public boolean canMoveRight() {
        return (heroRow != getNumColumns() - 1) &&
                TileType.canHorizontallyPass(matrix[heroRow][heroCol + 1]) &&
                TileType.isSafe(matrix[heroRow][heroCol + 2]);
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
        for (TileType[] tileRow : matrix)
            for (TileType tile : tileRow)
                if (TileType.isTileRelevantForHeuristic(tile))
                    h++;
        return h;
    }

    public double computeTileDistances() {
        return Math.abs(heroRow - exitRow) + Math.abs(heroRow - exitCol);
    }

    public int getNumRows() {
        return matrix.length;
    }

    public int getNumColumns() {
        return matrix[0].length;
    }

    public TileType getTileValue(int row, int col) {
        if (!isValidPosition(row, col))
            throw new IndexOutOfBoundsException("Invalid position!");
        return matrix[row][col];
    }

    public boolean isValidPosition(int row, int column) {
        return row >= 0 && row < matrix.length && column >= 0 && column < matrix[0].length;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MummyMazeState o))
            return false;

        if (matrix.length != o.matrix.length)
            return false;

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
        if (listeners != null)
            listeners.remove(l);
    }

    public synchronized void addListener(MummyMazeListener l) {
        if (!listeners.contains(l))
            listeners.add(l);
    }

    public void fireMazeChanged() {
        for (MummyMazeListener listener : listeners)
            listener.puzzleChanged(null);
    }

    public TileType[][] getMatrix() {
        return matrix;
    }
}

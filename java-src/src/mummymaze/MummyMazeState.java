package mummymaze;

import agent.Action;
import agent.State;

import java.util.ArrayList;
import java.util.Arrays;

public class MummyMazeState extends State implements Cloneable {
    final int[] linesfinalMatrix = {0, 0, 0, 1, 1, 1, 2, 2, 2};
    final int[] colsfinalMatrix = {0, 1, 2, 0, 1, 2, 0, 1, 2};
    private final char[][] matrix;
    private int lineBlank;
    private int columnBlank;
    //Listeners
    private transient ArrayList<MummyMazeListener> listeners = new ArrayList<MummyMazeListener>(3);

    public MummyMazeState(char[][] matrix) {
        this.matrix = new char[matrix.length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];
                if (this.matrix[i][j] == 0) {
                    lineBlank = i;
                    columnBlank = j;
                }
            }
        }
    }

    @Override
    public void executeAction(Action action) {
        action.execute(this);
        firePuzzleChanged(null);
    }

    public boolean canMoveUp() {
        return lineBlank != 0;
    }

    public boolean canMoveRight() {
        return columnBlank != matrix.length - 1;
    }

    public boolean canMoveDown() {
        return lineBlank != matrix.length - 1;
    }

    public boolean canMoveLeft() {
        return columnBlank != 0;
    }

    /*
     * In the next four methods we don't verify if the actions are valid.
     * This is done in method executeActions in class EightPuzzleProblem.
     * Doing the verification in these methods would imply that a clone of the
     * state was created whether the operation could be executed or not.
     */
    public void moveUp() {
        matrix[lineBlank][columnBlank] = matrix[--lineBlank][columnBlank];
        matrix[lineBlank][columnBlank] = 0;
    }

    public void moveRight() {
        matrix[lineBlank][columnBlank] = matrix[lineBlank][++columnBlank];
        matrix[lineBlank][columnBlank] = 0;
    }

    public void moveDown() {
        matrix[lineBlank][columnBlank] = matrix[++lineBlank][columnBlank];
        matrix[lineBlank][columnBlank] = 0;
    }

    public void moveLeft() {
        matrix[lineBlank][columnBlank] = matrix[lineBlank][--columnBlank];
        matrix[lineBlank][columnBlank] = 0;
    }

    public double computeTilesOutOfPlace() {
//        int h = 0;
//        for (int i = 0; i < matrix.length; i++)
//            for (int j = 0; j < matrix.length; j++)
//                if (matrix[i][j] != 0 && matrix[i][j] != finalState.matrix[i][j]) h++;
//        return h;
        return 0;
    }

    public double computeTileDistances() {
        double h = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix.length; j++)
                if (this.matrix[i][j] != 0) // Blank is ignored so that the heuristic is admissible
                    h += Math.abs(i - linesfinalMatrix[this.matrix[i][j]]) + Math.abs(j - colsfinalMatrix[this.matrix[i][j]]);
        return h;
    }

    public int getNumLines() {
        return matrix.length;
    }

    public int getNumColumns() {
        return matrix[0].length;
    }

    public int getTileValue(int line, int column) {
        if (!isValidPosition(line, column)) {
            throw new IndexOutOfBoundsException("Invalid position!");
        }
        return matrix[line][column];
    }

    public boolean isValidPosition(int line, int column) {
        return line >= 0 && line < matrix.length && column >= 0 && column < matrix[0].length;
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
        for (int i = 0; i < matrix.length; i++) {
            buffer.append('\n');
            for (int j = 0; j < matrix.length; j++) {
                buffer.append(matrix[i][j]);
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
        if (listeners != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    public synchronized void addListener(MummyMazeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void firePuzzleChanged(MummyMazeEvent pe) {
        for (MummyMazeListener listener : listeners) {
            listener.puzzleChanged(null);
        }
    }

    public char[][] getMatrix() {
        return matrix;
    }
}

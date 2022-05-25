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
    private int wMummyRow;
    private int wMummyCol;
    private int exitRow;
    private int exitCol;
    private boolean isHeroDead;

    public MummyMazeState(TileType[][] matrix) {
        this.matrix = new TileType[matrix.length][matrix.length];
        isHeroDead = false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];

                if (this.matrix[i][j] == TileType.HERO) {
                    heroRow = i;
                    heroCol = j;
                }

                if (this.matrix[i][j] == TileType.WHITE_MUMMY) {
                    wMummyRow = i;
                    wMummyCol = j;
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
        if (heroRow > 1)
            return TileType.canVerticallyPass(matrix[heroRow - 1][heroCol]) && TileType.isSafe(matrix[heroRow - 2][heroCol]);
        if (heroRow == 1)
            return matrix[heroRow - 1][heroCol] == TileType.EXIT;
        return false;
    }

    public boolean canMoveDown() {
        if (heroRow < getNumRows() - 2)
            return TileType.canVerticallyPass(matrix[heroRow + 1][heroCol]) && TileType.isSafe(matrix[heroRow + 2][heroCol]);
        if (heroRow == 11)
            return matrix[heroRow + 1][heroCol] == TileType.EXIT;
        return false;
    }

    public boolean canMoveLeft() {
        if (heroCol > 1)
            return TileType.canHorizontallyPass(matrix[heroRow][heroCol - 1]) && TileType.isSafe(matrix[heroRow][heroCol - 2]);
        if (heroCol == 1)
            return matrix[heroRow][heroCol - 1] == TileType.EXIT;
        return false;
    }

    public boolean canMoveRight() {
        if (heroCol < getNumCols() - 2)
            return TileType.canHorizontallyPass(matrix[heroRow][heroCol + 1]) && TileType.isSafe(matrix[heroRow][heroCol + 2]);
        if (heroCol == 11)
            return matrix[heroRow][heroCol + 1] == TileType.EXIT;
        return false;
    }

    /*
     * In the next four methods we don't verify if the actions are valid.
     * This is done in method executeActions in class EightPuzzleProblem.
     * Doing the verification in these methods would imply that a clone of the
     * state was created whether the operation could be executed or not.
     */

    public void updateEnemies(){
        if (!isHeroDead && matrix[heroRow][heroCol] != TileType.EXIT)
            moveWhiteMummy();
    }

    public void moveUp() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroRow == 1) heroRow--;
        else heroRow -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
        updateEnemies();
    }

    public void moveDown() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroRow == 11) heroRow++;
        else heroRow += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
        updateEnemies();
    }

    public void moveRight() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroCol == 11) heroCol += 1;
        else heroCol += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
        updateEnemies();
    }

    public void moveLeft() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroCol == 1) heroCol -= 1;
        else heroCol -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
        updateEnemies();
    }

    public void moveWhiteMummy() {
        // If the mummy is in the same row and column as the hero, the hero dies
        if (wMummyRow == heroRow && wMummyCol == heroCol) {
            isHeroDead = true;
            return;
        }

        // If the mummy is in the same column as the hero, it moves vertically
        if (wMummyCol == heroCol) {
            if (wMummyRow > heroRow) {
                if (matrix[wMummyRow - 1][wMummyCol] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyRow -= 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            } else {
                if (matrix[wMummyRow + 1][wMummyCol] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyRow += 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }
        }
        // If the mummy is in the same row as the hero, it moves horizontally
        else if (wMummyRow == heroRow) {
            if (wMummyCol > heroCol) {
                if (matrix[wMummyRow][wMummyCol - 1] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyCol -= 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            } else {
                if (matrix[wMummyRow][wMummyCol + 1] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyCol += 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }
        }

        // If the mummy is in a different row and column from the hero, it moves to the hero's column
        else {
            if (wMummyRow > heroRow) {
                if (matrix[wMummyRow - 1][wMummyCol] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyRow -= 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }

            if (wMummyRow < heroRow) {
                if (matrix[wMummyRow + 1][wMummyCol] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyRow += 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }

            if (wMummyCol > heroCol) {
                if (matrix[wMummyRow][wMummyCol - 1] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyCol -= 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }

            if (wMummyCol < heroCol) {
                if (matrix[wMummyRow][wMummyCol + 1] == TileType.EMPTY) {
                    matrix[wMummyRow][wMummyCol] = TileType.EMPTY;
                    wMummyCol += 2;
                    matrix[wMummyRow][wMummyCol] = TileType.WHITE_MUMMY;
                }
            }
        }
    }

    public double computeTilesOutOfPlace() {
        int h = 0;
        for (TileType[] tileRow : matrix)
            for (TileType tile : tileRow)
                if (TileType.isTileRelevantForHeuristic(tile)) h++;
        return h;
    }

    public double computeTileDistances() {
        return Math.abs(heroRow - exitRow) + Math.abs(heroCol - exitCol);
    }

    public int getNumRows() {
        return matrix.length;
    }

    public int getNumCols() {
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
        if (!(other instanceof MummyMazeState o)) return false;
        if (matrix.length != o.matrix.length) return false;
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
        if (listeners != null) listeners.remove(l);
    }

    public synchronized void addListener(MummyMazeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void fireMazeChanged() {
        for (MummyMazeListener listener : listeners)
            listener.mazeChanged(null);
    }

    public TileType[][] getMatrix() {
        return matrix;
    }

    public int getHeroRow() {
        return heroRow;
    }

    public int getHeroCol() {
        return heroCol;
    }

    public int getExitRow() {
        return exitRow;
    }

    public int getExitCol() {
        return exitCol;
    }

    public boolean isHeroDead() {
        return isHeroDead;
    }
}

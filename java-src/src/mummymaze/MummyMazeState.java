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
    private boolean whiteMummyExists = false;
    private int whiteMummyRow;
    private int whiteMummyCol;
    private boolean redMummyExists = false;
    private int redMummyRow;
    private int redMummyCol;
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
                    whiteMummyRow = i;
                    whiteMummyCol = j;
                    whiteMummyExists = true;
                }

                if (this.matrix[i][j] == TileType.RED_MUMMY) {
                    redMummyRow = i;
                    redMummyCol = j;
                    redMummyExists = true;
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

        if(!isAtGoal()){
            updateEnemies();
        }

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

    public void moveUp() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroRow == 1) heroRow--;
        else heroRow -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveDown() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroRow == 11) heroRow++;
        else heroRow += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveRight() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroCol == 11) heroCol += 1;
        else heroCol += 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    public void moveLeft() {
        matrix[heroRow][heroCol] = TileType.EMPTY;
        if (heroCol == 1) heroCol -= 1;
        else heroCol -= 2;
        matrix[heroRow][heroCol] = TileType.HERO;
    }

    private void updateEnemies(){
        int cont = 0;
        while(!isHeroDead && cont<2){
            if(whiteMummyExists)
                moveWhiteMummy();
            if(redMummyExists)
                moveRedMummy();
            cont++;
        }
    }

    private void moveMummyUp(int mummyRow, int mummyCol, TileType mummy){
        if (TileType.canVerticallyPass(matrix[mummyRow - 1][mummyCol])) {
            matrix[mummyRow][mummyCol] = TileType.EMPTY;
            mummyRow -= 2;
            matrix[mummyRow][mummyCol] = mummy;

            saveMummyState(mummyRow, mummyCol, mummy);
        }
    }

    private void moveMummyDown(int mummyRow, int mummyCol, TileType mummy){
        if (TileType.canVerticallyPass(matrix[mummyRow + 1][mummyCol])) {
            matrix[mummyRow][mummyCol] = TileType.EMPTY;
            mummyRow += 2;
            matrix[mummyRow][mummyCol] = mummy;

            saveMummyState(mummyRow, mummyCol, mummy);
        }
    }

    private void moveMummyLeft(int mummyRow, int mummyCol, TileType mummy){
        if (TileType.canHorizontallyPass(matrix[mummyRow][mummyCol - 1])) {
            matrix[mummyRow][mummyCol] = TileType.EMPTY;
            mummyCol -= 2;
            matrix[mummyRow][mummyCol] = mummy;

            saveMummyState(mummyRow, mummyCol, mummy);
        }
    }
    private void moveMummyRight(int mummyRow, int mummyCol, TileType mummy){
        if (TileType.canHorizontallyPass(matrix[mummyRow][mummyCol + 1])) {
            matrix[mummyRow][mummyCol] = TileType.EMPTY;
            mummyCol += 2;
            matrix[mummyRow][mummyCol] = mummy;

            saveMummyState(mummyRow, mummyCol, mummy);
        }
    }

    private void saveMummyState(int mummyRow, int mummyCol, TileType mummy){
        if(mummy == TileType.WHITE_MUMMY) {
            whiteMummyRow = mummyRow;
            whiteMummyCol = mummyCol;
        }
        else{
            redMummyRow = mummyRow;
            redMummyCol = mummyCol;
        }
    }

    private void moveWhiteMummy(){
        if(performMummyDefaultMovement(whiteMummyRow, whiteMummyCol, TileType.WHITE_MUMMY))
            if (whiteMummyCol > heroCol) {
                moveMummyLeft(whiteMummyRow, whiteMummyCol, TileType.WHITE_MUMMY);
            }else{
                // Mummy column is lower, because we already know it isn't equal.
                moveMummyRight(whiteMummyRow, whiteMummyCol, TileType.WHITE_MUMMY);
            }

        checkIfMummyKilledHero(whiteMummyRow, whiteMummyCol);
    }

    private void moveRedMummy(){
        if(performMummyDefaultMovement(redMummyRow, redMummyCol, TileType.RED_MUMMY))
            if (redMummyRow > heroRow) {
                moveMummyUp(redMummyRow, redMummyCol,TileType.RED_MUMMY);
            }else{
                // Mummy row is lower, because we already know it isn't equal.
                moveMummyDown(redMummyRow, redMummyCol,TileType.RED_MUMMY);
            }

        checkIfMummyKilledHero(redMummyRow, redMummyCol);
    }

    private void checkIfMummyKilledHero(int mummyRow, int mummyCol){
        // If the mummy is in the same row and column as the hero, the hero dies
        if (mummyRow == heroRow && mummyCol == heroCol) {
            isHeroDead = true;
        }
    }

    private boolean performMummyDefaultMovement(int mummyRow, int mummyCol, TileType mummy) {
        // If the mummy is in the same column as the hero, it moves vertically
        if (mummyCol == heroCol) {
            if (mummyRow > heroRow) {
                moveMummyUp(mummyRow, mummyCol, mummy);
            } else {
                moveMummyDown(mummyRow, mummyCol, mummy);
            }
        }
        // If the mummy is in the same row as the hero, it moves horizontally
        else if (mummyRow == heroRow) {
            if (mummyCol > heroCol) {
                moveMummyLeft(mummyRow, mummyCol, mummy);
            } else {
                moveMummyRight(mummyRow, mummyCol, mummy);
            }
        }
        // If the mummy is in a different row and column from the hero, it moves to the hero's column, therefore it is mummy-specific code
        else {
            return true;
        }

        return false;
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

    @Override
    public boolean isAtGoal(){
        return (heroCol==exitCol && heroRow==exitRow);
    }
}

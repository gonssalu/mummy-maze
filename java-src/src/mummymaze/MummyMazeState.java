package mummymaze;

import agent.Action;
import agent.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static mummymaze.TileType.*;

public class MummyMazeState extends State implements Cloneable {
    private final TileType[][] matrix;
    //The matrix bellow is used to keep track of where traps and the key is so that if an enemy passes through them they don't get "deleted".
    private final TileType[][] originalFloorsMatrix;
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
    private boolean scorpionExists = false;
    private int scorpionRow;
    private int scorpionCol;
    private int exitRow;
    private int exitCol;
    private boolean isHeroDead;

    public MummyMazeState(TileType[][] matrix){
        this(matrix, null);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if(matrix[i][j]!=null){
                    if(shouldSaveFloorType(matrix[i][j]))
                        originalFloorsMatrix[i][j] = matrix[i][j];
                    else
                        originalFloorsMatrix[i][j] = EMPTY;
                }
            }
        }
    }

    public MummyMazeState(TileType[][] matrix, TileType[][] originalFloorsMatrix) {
        this.matrix = new TileType[matrix.length][matrix.length];
        this.originalFloorsMatrix = new TileType[matrix.length][matrix.length];
        isHeroDead = false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];

                if (this.matrix[i][j] == HERO) {
                    heroRow = i;
                    heroCol = j;
                }

                if (this.matrix[i][j] == WHITE_MUMMY) {
                    whiteMummyRow = i;
                    whiteMummyCol = j;
                    whiteMummyExists = true;
                }

                if (this.matrix[i][j] == RED_MUMMY) {
                    redMummyRow = i;
                    redMummyCol = j;
                    redMummyExists = true;
                }

                if (this.matrix[i][j] == SCORPION) {
                    scorpionRow = i;
                    scorpionCol = j;
                    scorpionExists = true;
                }

                if (this.matrix[i][j] == EXIT) {
                    exitRow = i;
                    exitCol = j;
                }
            }
        }

        if(originalFloorsMatrix!=null)
            for (int i = 0; i < originalFloorsMatrix.length; i++)
                this.originalFloorsMatrix[i] = Arrays.copyOf(originalFloorsMatrix[i], originalFloorsMatrix[i].length);
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
            return canVerticallyPass(matrix[heroRow - 1][heroCol]) && allowsHeroMovement(matrix[heroRow - 2][heroCol]);
        if (heroRow == 1)
            return matrix[heroRow - 1][heroCol] == EXIT;
        return false;
    }

    public boolean canMoveDown() {
        if (heroRow < getNumRows() - 2)
            return canVerticallyPass(matrix[heroRow + 1][heroCol]) && allowsHeroMovement(matrix[heroRow + 2][heroCol]);
        if (heroRow == matrix.length-2)
            return matrix[heroRow + 1][heroCol] == EXIT;
        return false;
    }

    public boolean canMoveLeft() {
        if (heroCol > 1)
            return canHorizontallyPass(matrix[heroRow][heroCol - 1]) && allowsHeroMovement(matrix[heroRow][heroCol - 2]);
        if (heroCol == 1)
            return matrix[heroRow][heroCol - 1] == EXIT;
        return false;
    }

    public boolean canMoveRight() {
        if (heroCol < getNumCols() - 2)
            return canHorizontallyPass(matrix[heroRow][heroCol + 1]) && allowsHeroMovement(matrix[heroRow][heroCol + 2]);
        if (heroCol == matrix.length-2)
            return matrix[heroRow][heroCol + 1] == EXIT;
        return false;
    }

    /*
     * In the next four methods we don't verify if the actions are valid.
     * This is done in method executeActions in class EightPuzzleProblem.
     * Doing the verification in these methods would imply that a clone of the
     * state was created whether the operation could be executed or not.
     */

    public void moveUp() {
        matrix[heroRow][heroCol] = EMPTY;
        if (heroRow == 1) heroRow--;
        else heroRow -= 2;
        matrix[heroRow][heroCol] = HERO;
    }

    public void moveDown() {
        matrix[heroRow][heroCol] = EMPTY;
        if (heroRow == matrix.length-2) heroRow++;
        else heroRow += 2;
        matrix[heroRow][heroCol] = HERO;
    }

    public void moveRight() {
        matrix[heroRow][heroCol] = EMPTY;
        if (heroCol == matrix.length-2) heroCol += 1;
        else heroCol += 2;
        matrix[heroRow][heroCol] = HERO;
    }

    public void moveLeft() {
        matrix[heroRow][heroCol] = EMPTY;
        if (heroCol == 1) heroCol -= 1;
        else heroCol -= 2;
        matrix[heroRow][heroCol] = HERO;
    }

    private void updateEnemies(){
        int num = 0;

        if(scorpionExists)
            moveScorpion();

        while(!isHeroDead && num<2){
            if(whiteMummyExists)
                moveWhiteMummy();
            if(redMummyExists)
                moveRedMummy();
            num++;

        }

    }

    private void checkForFightsBetweenEnemies() {
        //Because the scorpion is the first to be updated, if his position is where a mummy is at it means they will fight.
        if(matrix[scorpionRow][scorpionCol] == WHITE_MUMMY || matrix[whiteMummyRow][whiteMummyCol] == SCORPION){
            //On a fight with a mummy the scorpion always dies.
            scorpionExists = false; // RIP
            matrix[whiteMummyRow][whiteMummyCol] = WHITE_MUMMY;
        }else if(matrix[scorpionRow][scorpionCol] == RED_MUMMY || matrix[redMummyRow][redMummyCol] == SCORPION){
            //On a fight with a mummy the scorpion always dies.
            scorpionExists = false; // RIP
            matrix[redMummyRow][redMummyCol] = RED_MUMMY;
        }

        //Id there is a red mummy at the white mummy position it means they will fight (and vice-versa).
        if(matrix[whiteMummyRow][whiteMummyCol] == RED_MUMMY || matrix[redMummyRow][redMummyCol] == WHITE_MUMMY){
            //On a fight between mummies the mummies fight with a 50/50 chance, one of them dies.
            boolean whiteMummyWins = (new Random()).nextBoolean();
            if (whiteMummyWins) {
                redMummyExists = false; // RIP
                matrix[whiteMummyRow][whiteMummyCol] = WHITE_MUMMY;
            }else{
                whiteMummyExists = false; // RIP
                matrix[redMummyRow][redMummyCol] = RED_MUMMY;
            }
        }
    }

    private void changeEnemyPosition(TileType enemy, int newEnemyRow, int newEnemyCol){
        matrix[newEnemyRow][newEnemyCol] = enemy;
        switch (enemy) {
            case WHITE_MUMMY -> {
                whiteMummyRow = newEnemyRow;
                whiteMummyCol = newEnemyCol;
            }
            case RED_MUMMY -> {
                redMummyRow = newEnemyRow;
                redMummyCol = newEnemyCol;
            }
            case SCORPION -> {
                scorpionRow = newEnemyRow;
                scorpionCol = newEnemyCol;
            }
        }
    }

    /* Enemy movement methods: These always return true if the enemy managed to move, and false if not */
    private boolean moveEnemyUp(TileType enemy, int enemyRow, int enemyCol){
        if (canVerticallyPass(matrix[enemyRow - 1][enemyCol])) {
            matrix[enemyRow][enemyCol] = originalFloorsMatrix[enemyRow][enemyCol];
            enemyRow -= 2;
            changeEnemyPosition(enemy, enemyRow, enemyCol);
            return true;
        }
        return false;
    }

    private boolean moveEnemyDown(TileType enemy, int enemyRow, int enemyCol){
        if (canVerticallyPass(matrix[enemyRow + 1][enemyCol])) {
            matrix[enemyRow][enemyCol] = originalFloorsMatrix[enemyRow][enemyCol];
            enemyRow += 2;
            changeEnemyPosition(enemy, enemyRow, enemyCol);
            return true;
        }
        return false;
    }

    private boolean moveEnemyLeft(TileType enemy, int enemyRow, int enemyCol){
        if (canHorizontallyPass(matrix[enemyRow][enemyCol - 1])) {
            matrix[enemyRow][enemyCol] = originalFloorsMatrix[enemyRow][enemyCol];
            enemyCol -= 2;
            changeEnemyPosition(enemy, enemyRow, enemyCol);
            return true;
        }
        return false;
    }
    private boolean moveEnemyRight(TileType enemy, int enemyRow, int enemyCol){
        if (canHorizontallyPass(matrix[enemyRow][enemyCol + 1])) {
            matrix[enemyRow][enemyCol] = originalFloorsMatrix[enemyRow][enemyCol];
            enemyCol += 2;
            changeEnemyPosition(enemy, enemyRow, enemyCol);
            return true;
        }
        return false;
    }

    private boolean shouldKillHero(TileType enemy){
        // If the enemy is in the same row and column as the hero, the hero dies
        switch (enemy) {
            case WHITE_MUMMY -> {
                return (whiteMummyRow==heroRow&&whiteMummyCol==heroCol);
            }
            case RED_MUMMY -> {
                return (redMummyRow==heroRow&&redMummyCol==heroCol);
            }
            case SCORPION -> {
                return (scorpionRow==heroRow&&scorpionCol==heroCol);
            }
        }
        return false;
    }

    private void checkIfEnemyKilledHero(TileType enemy){
        if(shouldKillHero(enemy))
            isHeroDead = true;
    }

    private void moveEnemy(TileType enemy, int enemyRow, int enemyCol, boolean rowFirst){
        boolean enemyMoved = false;
        int tries = 0;
        //It will only enter this while if the enemy hasn't moved yet, so an if(enemyMoved) is not needed.
        while(!enemyMoved && tries<2){
            if(rowFirst){
                if (enemyRow > heroRow)
                    enemyMoved = moveEnemyUp(enemy, enemyRow, enemyCol);
                else if(enemyRow < heroRow)
                    enemyMoved = moveEnemyDown(enemy, enemyRow, enemyCol);
                //If the enemy's row is the same as the hero's no point in moving to another row
            }else {
                if (enemyCol > heroCol)
                    enemyMoved = moveEnemyLeft(enemy, enemyRow, enemyCol);
                else if (enemyCol < heroCol)
                    enemyMoved = moveEnemyRight(enemy, enemyRow, enemyCol);
                //If the enemy's column is the same as the hero's no point in moving to another column
            }
            tries++;
            rowFirst = !rowFirst; //This way we are sure it tried to move in both directions.
        }

        if(enemyMoved){
            checkIfEnemyKilledHero(enemy);
            checkForFightsBetweenEnemies();
        }

    }

    private void moveWhiteMummy(){
        moveEnemy(WHITE_MUMMY, whiteMummyRow, whiteMummyCol, false);
    }

    private void moveRedMummy(){
        moveEnemy(RED_MUMMY, redMummyRow, redMummyCol, true);
    }

    private void moveScorpion(){
        moveEnemy(SCORPION, scorpionRow, scorpionCol, false);
    }

    public double computeTilesOutOfPlace() {
        int h = 0;
        for (TileType[] tileRow : matrix)
            for (TileType tile : tileRow)
                if (isTileRelevantForHeuristic(tile)) h++;
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
        return new MummyMazeState(matrix, originalFloorsMatrix);
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

    public boolean isHeroDead() {
        return isHeroDead;
    }

    @Override
    public boolean isAtGoal(){
        return (heroCol==exitCol && heroRow==exitRow);
    }

}

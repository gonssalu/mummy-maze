package mummymaze;

import agent.Action;
import agent.State;
import mummymaze.actions.ActionStay;
import mummymaze.util.TileType;

import java.awt.*;
import java.util.*;

import static mummymaze.util.TileType.*;

public class MummyMazeState extends State implements Cloneable {
    private final TileType[][] matrix;
    //The matrix bellow is used to keep track of where traps and the key is so that if an enemy passes through them they don't get "deleted".
    private final TileType[][] originalFloorsMatrix;
    //Listeners
    private final transient ArrayList<MummyMazeListener> listeners = new ArrayList<>(3);

    private HashMap<TileType, LinkedList<Point>> enemies;

    private Point hero;
    private Point exit;
    private Point key;
    private Point door;

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

        this.enemies = new HashMap<>(3);
        this.enemies.put(SCORPION, new LinkedList<>());
        this.enemies.put(RED_MUMMY, new LinkedList<>());
        this.enemies.put(WHITE_MUMMY, new LinkedList<>());

        isHeroDead = false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.matrix[i][j] = matrix[i][j];
                TileType tile = this.matrix[i][j];
                Point pt = new Point(i,j);
                if(tile!=null)
                    switch(tile){
                        case HERO -> {
                            hero = pt;
                        }
                        case WHITE_MUMMY,RED_MUMMY,SCORPION -> {
                            this.enemies.get(tile).add(pt);
                        }
                        case EXIT -> {
                            exit = pt;
                        }
                        case KEY -> {
                            key = pt;
                        }
                        case H_DOOR_CLOSED, H_DOOR_OPEN, V_DOOR_CLOSED, V_DOOR_OPEN -> {
                            door = pt;
                        }
                    }
            }
        }

        if(originalFloorsMatrix!=null)
            for (int i = 0; i < originalFloorsMatrix.length; i++)
                this.originalFloorsMatrix[i] = Arrays.copyOf(originalFloorsMatrix[i], originalFloorsMatrix[i].length);
    }

    public void revertToOriginalTile(Point pos){ matrix[pos.x][pos.y] = originalFloorsMatrix[pos.x][pos.y]; }

    public TileType getTile(Point pos) { return matrix[pos.x][pos.y]; }

    public void setTile(Point pos, TileType tile) { matrix[pos.x][pos.y] = tile; }

    public void checkForDoorToggle(){
        if(hero.equals(key)){
            switch(getTile(door)){
                case V_DOOR_CLOSED -> {
                    setTile(door,V_DOOR_OPEN);
                }
                case V_DOOR_OPEN -> {
                    setTile(door,V_DOOR_CLOSED);
                }
                case H_DOOR_CLOSED -> {
                    setTile(door,H_DOOR_OPEN);
                }
                case H_DOOR_OPEN -> {
                    setTile(door,H_DOOR_CLOSED);
                }
            }
        }
    }

    @Override
    public void executeAction(Action action) {
        action.execute(this);

        if(!(action instanceof ActionStay)) //Se ele estiver parado em cima da chave não ficar a dar toggle
            checkForDoorToggle();

        if(!isAtGoal()){
            updateEnemies();
        }

        fireMazeChanged();
    }

    public boolean canMoveUp() {
        if (hero.x > 1)
            return canVerticallyPass(matrix[hero.x - 1][hero.y]) && allowsHeroMovement(matrix[hero.x - 2][hero.y]);
        if (hero.x == 1)
            return matrix[hero.x - 1][hero.y] == EXIT;
        return false;
    }

    public boolean canMoveDown() {
        if (hero.x < getNumRows() - 2)
            return canVerticallyPass(matrix[hero.x + 1][hero.y]) && allowsHeroMovement(matrix[hero.x + 2][hero.y]);
        if (hero.x == matrix.length-2)
            return matrix[hero.x + 1][hero.y] == EXIT;
        return false;
    }

    public boolean canMoveLeft() {
        if (hero.y > 1)
            return canHorizontallyPass(matrix[hero.x][hero.y - 1]) && allowsHeroMovement(matrix[hero.x][hero.y - 2]);
        if (hero.y == 1)
            return matrix[hero.x][hero.y - 1] == EXIT;
        return false;
    }

    public boolean canMoveRight() {
        if (hero.y < getNumCols() - 2)
            return canHorizontallyPass(matrix[hero.x][hero.y + 1]) && allowsHeroMovement(matrix[hero.x][hero.y + 2]);
        if (hero.y == matrix.length-2)
            return matrix[hero.x][hero.y + 1] == EXIT;
        return false;
    }

    /*
     * In the next four methods we don't verify if the actions are valid.
     * This is done in method executeActions in class EightPuzzleProblem.
     * Doing the verification in these methods would imply that a clone of the
     * state was created whether the operation could be executed or not.
     */

    public void moveUp() {
        revertToOriginalTile(hero);
        if (hero.x == 1) hero.x--;
        else hero.x -= 2;
        setTile(hero, HERO);
    }

    public void moveDown() {
        revertToOriginalTile(hero);
        if (hero.x == matrix.length-2) hero.x++;
        else hero.x += 2;
        setTile(hero, HERO);
    }

    public void moveRight() {
        revertToOriginalTile(hero);
        if (hero.y == matrix.length-2) hero.y += 1;
        else hero.y += 2;
        setTile(hero, HERO);
    }

    public void moveLeft() {
        revertToOriginalTile(hero);
        if (hero.y == 1) hero.y -= 1;
        else hero.y -= 2;
        setTile(hero, HERO);
    }

    private void updateEnemies(){
        boolean done = false;

        for(TileType enemyType : enemies.keySet()) {
            for (int i = 0; i < enemies.get(enemyType).size(); i++) {
                moveEnemy(enemyType, i);
                if(isHeroDead){
                    done = true;
                    break;
                }
            }
            if(done) //to prevent unnecessary iterations
                break;
        }
    }

    //Returns true if the enemy in question died. False only guarantees that THIS enemy is alive.
    private boolean checkIfEnemyDied(TileType type, int idx) {
        Point pos = enemies.get(type).get(idx);
        for(TileType enemyType : enemies.keySet()){
            for(int i = 0; i<enemies.get(enemyType).size(); i++){
                if(enemyType == type && i == idx)
                    continue;
                if(enemies.get(enemyType).get(i).equals(pos)){
                    //If the entity who just moved finds a scorpion and isn't one, it kills it.
                    if(enemyType == SCORPION && type!=SCORPION){
                        //The entity which just moved kills the scorpion
                        enemies.get(enemyType).remove(i);
                        setTile(pos, type);
                        return false;
                    }else{
                        enemies.get(type).remove(pos);
                        setTile(pos, enemyType);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* Enemy movement methods: These always return true if the enemy managed to move, and false if not */

    private boolean moveEnemyRow(TileType type, int idx, int dx){
        Point pos = enemies.get(type).get(idx);
        if (canVerticallyPass(matrix[pos.x + dx][pos.y])) {
            revertToOriginalTile(pos);
            pos.translate(dx*2, 0);
            saveEnemyPos(type, idx, pos);
            return true;
        }
        return false;
    }

    private boolean moveEnemyCol(TileType type, int idx, int dy){
        Point pos = enemies.get(type).get(idx);
        if (canHorizontallyPass(matrix[pos.x][pos.y + dy])) {
            revertToOriginalTile(pos);
            pos.translate(0, dy*2);
            saveEnemyPos(type, idx, pos);
            return true;
        }
        return false;
    }

    //Saves the new enemy position
    private void saveEnemyPos(TileType type, int idx, Point pos) {
        setTile(pos, type);
        enemies.get(type).set(idx, pos);
    }

    private boolean moveEnemyUp(TileType type, int idx){ return moveEnemyRow(type, idx, -1); }
    private boolean moveEnemyDown(TileType type, int idx){ return moveEnemyRow(type, idx, 1); }
    private boolean moveEnemyLeft(TileType type, int idx){ return moveEnemyCol(type, idx, -1); }
    private boolean moveEnemyRight(TileType type, int idx){ return moveEnemyCol(type, idx, 1); }

    //True if the enemy is still alive, false otherwise.
    private boolean moveEnemyOnce(TileType enemyType, int enemyIdx){
        boolean rowFirst = (enemyType==RED_MUMMY);
        Point pos = enemies.get(enemyType).get(enemyIdx);

        boolean enemyMoved = false;
        int tries = 0;
        //It will only enter this while if the enemy hasn't moved yet, so an if(enemyMoved) is not needed.
        while(!enemyMoved && tries<2){
            if(rowFirst){
                if (pos.x > hero.x)
                    enemyMoved = moveEnemyUp(enemyType, enemyIdx);
                else if(pos.x < hero.x)
                    enemyMoved = moveEnemyDown(enemyType, enemyIdx);
                //If the enemy's row is the same as the hero's no point in moving to another row
            }else {
                if (pos.y > hero.y)
                    enemyMoved = moveEnemyLeft(enemyType, enemyIdx);
                else if (pos.y < hero.y)
                    enemyMoved = moveEnemyRight(enemyType, enemyIdx);
                //If the enemy's column is the same as the hero's no point in moving to another column
            }
            tries++;
            rowFirst = !rowFirst; //This way we are sure it tried to move in both directions
        }

        if(enemyMoved){
            if(enemies.get(enemyType).get(enemyIdx).equals(hero))
                isHeroDead = true;

            return !checkIfEnemyDied(enemyType, enemyIdx);
        }

        return true;

    }

    private void moveEnemy(TileType enemyType, int enemyIdx){
        if(moveEnemyOnce(enemyType, enemyIdx)) //Only attempt the second movement if the enemy survived the first
            if(isMummy(enemyType)) //Mummies will always try to move twice
                moveEnemyOnce(enemyType, enemyIdx);
    }

    private double calcDistToHero(Point pos){
        return Math.abs(hero.x - pos.x) + Math.abs(hero.y - pos.y);
    }

    public double computeDistanceClosestEnemy() {
        final double maxPossibleDistance = ((matrix.length-2)-1)*2;
        double h = maxPossibleDistance;
        double aux = maxPossibleDistance+1;

        //verificar morte e objetivo
        if(isHeroDead) return Double.MAX_VALUE;
        if(isAtGoal()) return 0;


        for(TileType enemyType : enemies.keySet()){
            for(int i = 0; i<enemies.get(enemyType).size(); i++) {
                aux = calcDistToHero(enemies.get(enemyType).get(i));
                if(aux<h)
                    h = aux;
            }
        }
        return (aux>maxPossibleDistance?0:maxPossibleDistance-h); //So that the lowest value, the better.
        //If the aux is bigger than the maxPossibleDistance it means no enemy was alive, so its 0
    }

    public double computeExitDistance() {
        if(isHeroDead) return Double.MAX_VALUE;
        return calcDistToHero(exit);
    }

    public int getNumRows() {
        return matrix.length;
    }

    public int getNumCols() {
        return matrix[0].length;
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
        return hero.equals(exit);
    }

}

package mummymaze.util;

import java.awt.*;

public class Entity extends Point {

    private boolean alive;

    public Entity(int x, int y) {
        super(x, y);
        alive = true;
    }

    public Entity(Point pt){
        super(pt);
        alive = true;
    }

    public void setDead(){
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

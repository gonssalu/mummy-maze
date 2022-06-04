package gui;

import mummymaze.MummyMazeEvent;
import mummymaze.MummyMazeListener;
import mummymaze.MummyMazeState;
import mummymaze.util.TileType;

import javax.swing.*;
import java.awt.*;

public class GameArea extends JPanel implements MummyMazeListener {

    private Image trap;
    private Image key;
    private Image stairsDown;
    private Image stairsUp;
    private Image stairsRight;
    private Image stairsLeft;
    private Image scorpion;
    private Image hero;
    private Image background;
    private Image mummyWhite;
    private Image mummyRed;
    private Image wallHorizontal;
    private Image wallVertical;
    private Image doorHorizontalOpen;
    private Image doorHorizontalClosed;
    private Image doorVerticalOpen;
    private Image doorVerticalClosed;
    private MummyMazeState state = null;
    private boolean showSolutionCost;
    private double solutionCost;

    public GameArea() {
        super();
        setPreferredSize(new Dimension(486, 474));
        loadImages();
        showSolutionCost = true;
    }

    private void loadImages() {
        trap = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/trap.png"));
        key = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/key.png"));
        stairsDown = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/stairsDown.png"));
        stairsUp = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/stairsUp.png"));
        stairsRight = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/stairsRight.png"));
        stairsLeft = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/stairsLeft.png"));
        scorpion = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/scorpion.png"));
        hero = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/explorer.png"));
        background = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/background.png"));
        mummyWhite = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/mummyWhite.png"));
        mummyRed = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/mummyRed.png"));
        wallHorizontal = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/wallHorizontal.png"));
        wallVertical = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/wallVertical.png"));
        doorHorizontalOpen = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/doorHorizontalOpen.png"));
        doorHorizontalClosed = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/doorHorizontalClosed.png"));
        doorVerticalOpen = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/doorVerticalOpen.png"));
        doorVerticalClosed = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/sprites/doorVerticalClosed.png"));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(background, 0, 0, this);

        if (state == null) return;

        TileType[][] matrix = state.getMatrix();

        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                int xStart = 63;
                int yStart = 79;

                int cond1 = j == 0 ? xStart + (j - 2) / 2 * 60 : xStart + j / 2 * 60;
                int cond2 = i == 0 ? yStart + (i - 2) / 2 * 60 - 6 : yStart + i / 2 * 60;

                switch (matrix[i][j]) {
                    case H_WALL:
                        g.drawImage(wallHorizontal, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case H_DOOR_CLOSED:
                        g.drawImage(doorHorizontalClosed, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case H_DOOR_OPEN:
                        g.drawImage(doorHorizontalOpen, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case V_WALL:
                        g.drawImage(wallVertical, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case V_DOOR_CLOSED:
                        g.drawImage(doorVerticalClosed, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case V_DOOR_OPEN:
                        g.drawImage(doorVerticalOpen, xStart + j / 2 * 60, yStart + i / 2 * 60 - 6, this);
                        break;
                    case WHITE_MUMMY:
                        g.drawImage(mummyWhite, xStart + j / 2 * 60, yStart + i / 2 * 60, this);
                        break;
                    case HERO:
                        g.drawImage(hero, cond1, cond2, this);
                        break;
                    case RED_MUMMY:
                        g.drawImage(mummyRed, xStart + j / 2 * 60, yStart + i / 2 * 60, this);
                        break;
                    case TRAP:
                        g.drawImage(trap, xStart + j / 2 * 60, yStart + i / 2 * 60, this);
                        break;
                    case SCORPION:
                        g.drawImage(scorpion, xStart + j / 2 * 60, yStart + i / 2 * 60, this);
                        break;
                    case KEY:
                        g.drawImage(key, xStart + j / 2 * 60, yStart + i / 2 * 60, this);
                        break;
                    case EXIT:
                        g.drawImage(i == 0 ? stairsUp : i == 12 ? stairsDown : j == 0 ? stairsLeft : stairsRight, cond1, cond2, this);
                        break;
                }
            }
        }

        if (showSolutionCost) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Solution cost: " + solutionCost, 10, 20);
        }

    }

    public void setState(MummyMazeState state) {
        if (state == null)
            throw new NullPointerException("Puzzle cannot be null");

        if (this.state != null)
            this.state.removeListener(this);

        this.state = state;
        state.addListener(this);
        repaint();
    }

    public void setShowSolutionCost(boolean showSolutionCost) {
        this.showSolutionCost = showSolutionCost;
    }

    public void setSolutionCost(double solutionCost) {
        this.solutionCost = solutionCost;
    }

    @Override
    public void mazeChanged(MummyMazeEvent pe) {
        repaint();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
        }
    }
}

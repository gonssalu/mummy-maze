package mummymaze;

import agent.Agent;
import mummymaze.heuristics.HeuristicEnemyDistance;
import mummymaze.heuristics.HeuristicExitDistance;
import mummymaze.util.TileType;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MummyMazeAgent extends Agent<MummyMazeState> {

    protected MummyMazeState initialEnvironment;

    public MummyMazeAgent(MummyMazeState environment) {
        super(environment);
        initialEnvironment = environment.clone();
        heuristics.add(new HeuristicExitDistance());
        heuristics.add(new HeuristicEnemyDistance());
        heuristic = heuristics.get(0);
    }

    public MummyMazeState resetEnvironment() {
        environment = initialEnvironment.clone();
        return environment;
    }

    public MummyMazeState readInitialStateFromFile(File file) throws IOException {
        Scanner scanner = new java.util.Scanner(file);
        TileType[][] matrix = new TileType[13][13];

        for (int i = 0; i < 13; i++) {
            String line = scanner.nextLine();
            for (int j = 0; j < 13; j++)
                matrix[i][j] = TileType.getTileType(line.charAt(j));
        }

        initialEnvironment = new MummyMazeState(matrix);
        resetEnvironment();
        return environment;
    }
}

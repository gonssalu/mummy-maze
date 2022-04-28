package mummymaze;

import agent.Agent;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MummyMazeAgent extends Agent<MummyMazeState> {

    protected MummyMazeState initialEnvironment;

    public MummyMazeAgent(MummyMazeState environment) {
        super(environment);
        initialEnvironment = environment.clone();
        heuristics.add(new HeuristicTileDistance());
        heuristics.add(new HeuristicTilesOutOfPlace());
        heuristic = heuristics.get(0);
    }

    public MummyMazeState resetEnvironment() {
        environment = initialEnvironment.clone();
        return environment;
    }

    public MummyMazeState readInitialStateFromFile(File file) throws IOException {
        Scanner scanner = new java.util.Scanner(file);

        char[][] tempMatrix = new char[13][13];

        for (int i = 0; i < 13; i++)
            tempMatrix[i] = scanner.nextLine().toCharArray();
        
        TileType[][] matrix = new TileType[13][13];
        
        
        
        initialEnvironment = new MummyMazeState(matrix);
        resetEnvironment();
        return environment;
    }
}

package mummymaze;

import agent.Agent;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MummyMazeAgent extends Agent<MummyMazeState> {

    protected MummyMazeState initialEnvironment;

    public MummyMazeAgent(MummyMazeState enviroment) {
        super(enviroment);
        initialEnvironment = enviroment.clone();
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

        TileType[][] matrix = new TileType[13][13];

        for (int i = 0; i < 13; i++)
            for (int j = 0; j < 13; j++)
                matrix[i][j] = TileType.getTileType(scanner.next().charAt(0));

        
        
        
        initialEnvironment = new MummyMazeState(matrix);
        resetEnvironment();
        return environment;
    }
}

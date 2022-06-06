package mummymaze;

import agent.Agent;
import mummymaze.heuristics.HeuristicEnemyAndGoalDistance;
import mummymaze.heuristics.HeuristicEnemyDistance;
import mummymaze.heuristics.HeuristicGoalDistance;
import mummymaze.util.TileType;
import searchmethods.BeamSearch;
import searchmethods.DepthLimitedSearch;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MummyMazeAgent extends Agent<MummyMazeState> {

    protected MummyMazeState initialEnvironment;

    public MummyMazeAgent(MummyMazeState environment) {
        super(environment);
        initialEnvironment = environment.clone();
        heuristics.add(new HeuristicGoalDistance());
        heuristics.add(new HeuristicEnemyDistance());
        heuristics.add(new HeuristicEnemyAndGoalDistance());
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

    public String getCsvSearchReport(){
        StringBuilder sb = new StringBuilder();
        sb.append(searchMethod).append(";");
        sb.append((heuristic==null?"N/A":heuristic)).append(";");
        if(searchMethod instanceof BeamSearch bs){
            sb.append(bs.getBeamSize());
        }else if(searchMethod instanceof DepthLimitedSearch dls){
            sb.append(dls.getDepthLimit());
        }else{
            sb.append("N/A");
        }
        sb.append(";");
        sb.append((solution==null?"N/A":solution.getCost())).append(";");
        sb.append(searchMethod.getStatistics().numExpandedNodes).append(";");
        sb.append(searchMethod.getStatistics().maxFrontierSize).append(";");
        sb.append(searchMethod.getStatistics().numGeneratedSates);

        return sb.toString();
    }
}

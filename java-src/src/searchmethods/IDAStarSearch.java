package searchmethods;

import agent.Action;
import agent.Problem;
import agent.Solution;
import agent.State;

import java.util.List;

public class IDAStarSearch extends InformedSearch {
    /*
     * Note that, on each iteration, the search is done in a depth first search way.
     */

    private double limit;
    private double newLimit;

    @Override
    public Solution search(Problem problem) {
        statistics.reset();
        stopped = false;
        this.heuristic = problem.getHeuristic();
        limit = heuristic.compute(problem.getInitialState());

        Solution solution;
        boolean stop = false;
        do {
            solution = graphSearch(problem);
            if(newLimit==limit)
                stop = true;
            limit = newLimit;
        } while (solution == null && !stop);

        return solution;
    }

    @Override
    protected Solution graphSearch(Problem problem) {
        newLimit = Double.MAX_VALUE;
        frontier.clear();
        frontier.add(new Node(problem.getInitialState()));
        while (!frontier.isEmpty() && !stopped) {
            Node n = frontier.poll();
            State state = n.getState();
            if (problem.isGoal(state)) {
                return new Solution(problem, n);
            }
            List<Action> actions = problem.getActions(state);
            int successorsSize = actions.size();
            for (Action action : actions) {
                State successor = problem.getSuccessor(state, action);
                addSuccessorToFrontier(successor, n);
            }
            computeStatistics(successorsSize);
        }
        return null;
    }

    @Override
    public void addSuccessorToFrontier(State successor, Node parent) {

        double g = parent.getG() + successor.getAction().getCost();
        if (!frontier.containsState(successor)) {
            double f = g + heuristic.compute(successor);
            if (f <= limit) {
                if (!parent.isCycle(successor)) {
                    frontier.add(new Node(successor, parent, g, f));
                }
            } else {
                newLimit = Math.min(newLimit, f);
            }
        } else if (frontier.getNode(successor).getG() > g) {
            frontier.removeNode(successor);
            frontier.add(new Node(successor, parent, g, g + heuristic.compute(successor)));
        }
    }

    @Override
    public String toString() {
        return "IDA* search";
    }
}

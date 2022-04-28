package searchmethods;

import agent.State;

public class GreedyBestFirstSearch extends InformedSearch {

    //f = h
    @Override
    public void addSuccessorToFrontier(State successor, Node parent) {
        double h = heuristic.compute(successor);
        double g = parent.getG() + successor.getAction().getCost();
        if (!frontier.containsState(successor)) {
            if (!explored.contains(successor))
                frontier.add(new Node(successor, parent, g, h));
        } else if (g < frontier.getNode(successor).getG()) {
            frontier.removeNode(successor);
            frontier.add(new Node(successor, parent, g, h));
        }
    }

    @Override
    public String toString() {
        return "Greedy best first search";
    }
}

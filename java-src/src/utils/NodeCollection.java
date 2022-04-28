package utils;

import agent.State;
import searchmethods.Node;

import java.util.Queue;

public interface NodeCollection extends Queue<Node> {
    boolean containsState(State e);
}

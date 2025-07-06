package utils;

import models.Edge;
import models.Node;

import java.util.*;

public class EdgeUtils {
    public static String createEdgeKey(Node source, Node destination) {
        String name1 = source.getName();
        String name2 = destination.getName();

        //sort names
        if (name1.compareTo(name2) < 0) { //0 if equall, <0 if less
            return name1 + "-" + name2;
        } else {
            return name2 + "-" + name1;
        }
    }

    public static Map<Node, Map<Node, Double>> buildGraphFromEdges(List<Edge> edges) {
        Map<Node, Map<Node, Double>> graph = new HashMap<>();

        // Initialize empty adjacency maps for all nodes
        Set<Node> allNodes = new HashSet<>();
        for (Edge edge : edges) {
            allNodes.add(edge.getSource());
            allNodes.add(edge.getDestination());
        }

        for (Node node : allNodes) {
            graph.put(node, new HashMap<>());
        }

        // Add edges to adjacency matrix
        for (Edge edge : edges) {
            graph.get(edge.getSource()).put(edge.getDestination(), edge.getWeight());
            graph.get(edge.getDestination()).put(edge.getSource(), edge.getWeight());
        }

        return graph;
    }

    public static List<Edge> getAllOriginalEdges(List<Node> nodes) {
        List<Edge> allEdges = new ArrayList<>();
        Set<String> addedEdges = new HashSet<>();

        for (Node node : nodes) {
            for (Edge edge : node.getEdges()) {
                // Create unique key for edge (direction independent)
                String edgeKey = createEdgeKey(edge.getSource(), edge.getDestination());
                if (!addedEdges.contains(edgeKey)) {
                    allEdges.add(edge);
                    addedEdges.add(edgeKey);
                }
            }
        }

        return allEdges;
    }

    public static boolean isEdgeInList(List<Edge> edgeList, Edge edge) {
        for (Edge e : edgeList) {
            if ((e.getSource().equals(edge.getSource()) && e.getDestination().equals(edge.getDestination())) ||
                    (e.getSource().equals(edge.getDestination()) && e.getDestination().equals(edge.getSource()))) {
                return true;
            }
        }
        return false;
    }
}

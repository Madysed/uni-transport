package algorithms;

import models.Edge;
import models.Node;

import java.util.*;

public class Kruskal {
    
    public static List<Edge> findMST(List<Node> nodes) {
        List<Edge> allEdges = new ArrayList<>();
        List<Edge> mstEdges = new ArrayList<>();
        Set<String> addedEdges = new HashSet<>();
        
        // Collect all edges without duplicates
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
        
        // Check if graph is connected
        if (nodes.size() <= 1) {
            return mstEdges; // Empty graph or single node
        }
        
        // Sort edges by weight
        allEdges.sort(Comparator.comparingDouble(Edge::getWeight));
        
        // Create map from nodes to indices
        Map<Node, Integer> nodeToIndex = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodeToIndex.put(nodes.get(i), i);
        }
        
        UnionFind uf = new UnionFind(nodes.size());
        
        // Run Kruskal's algorithm
        for (Edge edge : allEdges) {
            Integer sourceIndex = nodeToIndex.get(edge.getSource());
            Integer destIndex = nodeToIndex.get(edge.getDestination());
            
            // Check that both nodes exist in map
            if (sourceIndex != null && destIndex != null) {
                if (uf.union(sourceIndex, destIndex)) {
                    mstEdges.add(edge);
                    if (mstEdges.size() == nodes.size() - 1) {
                        break; // MST completed
                    }
                }
            }
        }
        
        return mstEdges;
    }
    
    // Create unique key for edge independent of direction
    private static String createEdgeKey(Node source, Node destination) {
        String name1 = source.getName();
        String name2 = destination.getName();
        
        // Sort names to create unique key
        if (name1.compareTo(name2) < 0) {
            return name1 + "-" + name2;
        } else {
            return name2 + "-" + name1;
        }
    }
    
    public static double calculateMSTCost(List<Edge> mstEdges) {
        double totalCost = 0;
        for (Edge edge : mstEdges) {
            totalCost += edge.getWeight();
        }
        return totalCost;
    }
    
    // Check if graph is connected
    public static boolean isGraphConnected(List<Node> nodes) {
        if (nodes.size() <= 1) return true;
        
        List<Edge> mstEdges = findMST(nodes);
        return mstEdges.size() == nodes.size() - 1;
    }
} 
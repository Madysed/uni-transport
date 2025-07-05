package algorithms;

import models.Edge;
import models.Node;
import java.util.*;

import static utils.EdgeUtils.createEdgeKey;

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

    
    public static double calculateMSTCost(List<Edge> mstEdges) {
        double totalCost = 0;
        for (Edge edge : mstEdges) {
            totalCost += edge.getWeight();
        }
        return totalCost;
    }
} 
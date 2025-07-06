package algorithms;

import models.Edge;
import models.Node;
import java.util.*;
import static utils.EdgeUtils.createEdgeKey;

/**
 * Implements Kruskal's algorithm to compute the Minimum Spanning Tree (MST) of a graph.
 */
public class Kruskal {

    /**
     * Finds the Minimum Spanning Tree (MST) of the given graph using Kruskal's algorithm.
     *
     * @param nodes List of all nodes in the graph
     * @return List of edges that form the MST
     *
     * @timeComplexity O(E log E + Eα(V)) where E is number of edges, V is number of vertices, α is inverse Ackermann
     * @spaceComplexity O(E + V) for edge list and Union-Find structure
     */
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

    /**
     * Calculates the total weight of the edges in the MST.
     *
     * @param mstEdges List of edges in the MST
     * @return Total weight (cost) of the MST
     *
     * @timeComplexity O(E) where E is the number of MST edges
     * @spaceComplexity O(1)
     */
    public static double calculateMSTCost(List<Edge> mstEdges) {
        double totalCost = 0;
        for (Edge edge : mstEdges) {
            totalCost += edge.getWeight();
        }
        return totalCost;
    }
} 
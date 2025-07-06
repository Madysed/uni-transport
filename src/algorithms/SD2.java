package algorithms;

import models.Edge;
import models.Node;

import java.util.*;
import java.util.stream.Collectors;

import static algorithms.Bfs.bfs;
import static utils.EdgeUtils.*;

public class SD2 {

    /**
     * Finds additional edges to be added to the MST to ensure the resulting graph
     * has a diameter of at most 2.
     *
     * Time Complexity: O(V^2 * E) where V is the number of nodes and E is the number of edges.
     * Space Complexity: O(V^2)
     *
     * @param mstEdges The list of edges forming the Minimum Spanning Tree.
     * @param originalNodes The original list of nodes with all possible edges.
     * @return A list of edges to be added to the MST to satisfy diameter-2 property.
     */
    public static List<Edge> findSD2Edges(List<Edge> mstEdges, List<Node> originalNodes) {
        List<Edge> sd2Edges = new ArrayList<>();
        if (mstEdges.isEmpty()) {
            System.out.println("No MST edges provided. Cannot compute SD2 edges.");
            return sd2Edges;
        }

        // Build adjacency matrix from MST edges
        Map<Node, Map<Node, Double>> mstGraph = buildGraphFromEdges(mstEdges);
        System.out.println("MST Graph: " + mstEdges.size() + " edges");
        for (Edge edge : mstEdges) {
            System.out.println("  MST Edge: " + edge.getSource().getName() + " -> " +
                    edge.getDestination().getName() + " (Weight: " + edge.getWeight() + ")");
        }

        // Find all original edges for potential additions
        List<Edge> allOriginalEdges = getAllOriginalEdges(originalNodes);
        System.out.println("All original edges: " + allOriginalEdges.size());
        for (Edge edge : allOriginalEdges) {
            System.out.println("Edge: " + edge.getSource().getName() + " -> " +
                    edge.getDestination().getName() + " (Weight: " + edge.getWeight() + ")");
        }

        // Find nodes that violate diameter-2 property
        Map<Node, List<Node>> violatingNodes = satisfiesDiameter2(originalNodes, mstGraph);
        System.out.println("Initial violating nodes count: " + violatingNodes.size());
        for (Map.Entry<Node, List<Node>> entry : violatingNodes.entrySet()) {
            System.out.println("  " + entry.getKey().getName() + " violates diameter-2 with: " +
                    entry.getValue().stream().map(Node::getName).collect(Collectors.joining(", ")));
        }

        int maxIterations = originalNodes.size() * originalNodes.size();
        int iteration = 0;

        while (!violatingNodes.isEmpty() && iteration < maxIterations) {
            iteration++;
            System.out.println("Iteration " + iteration + ": Violating nodes = " + violatingNodes.size());

            // Get first violating node pair
            Node sourceNode = violatingNodes.keySet().iterator().next();
            Node targetNode = violatingNodes.get(sourceNode).get(0);
            System.out.println("Processing violation: " + sourceNode.getName() + " -> " + targetNode.getName());

            // Find best edge to connect these nodes
            Edge bestEdge = findBestEdgeForConnection(sourceNode, targetNode, allOriginalEdges, mstEdges, sd2Edges);

            if (bestEdge != null) {
                sd2Edges.add(bestEdge);
                System.out.println("Added SD2 edge: " + bestEdge.getSource().getName() +
                        " -> " + bestEdge.getDestination().getName() +
                        " (weight: " + bestEdge.getWeight() + ")");

                // Update the graph with new edge
                mstGraph.computeIfAbsent(bestEdge.getSource(), k -> new HashMap<>())
                        .put(bestEdge.getDestination(), bestEdge.getWeight());
                mstGraph.computeIfAbsent(bestEdge.getDestination(), k -> new HashMap<>())
                        .put(bestEdge.getSource(), bestEdge.getWeight());
            } else {
                System.out.println("No suitable edge found for " + sourceNode.getName() + " -> " + targetNode.getName());
                violatingNodes.get(sourceNode).remove(targetNode);
                if (violatingNodes.get(sourceNode).isEmpty()) {
                    violatingNodes.remove(sourceNode);
                }
            }

            // Recalculate violations
            violatingNodes = satisfiesDiameter2(originalNodes, mstGraph);
        }

        System.out.println("Final SD2 edges: " + sd2Edges.size());
        return sd2Edges;
    }

    /**
     * Identifies nodes that violate the diameter-2 constraint.
     * A node u violates the property with another node v if their shortest path length > 2.
     *
     * Time Complexity: O(V * (V + E))
     * Space Complexity: O(V)
     *
     * @param nodes The list of all nodes.
     * @param graph The adjacency map representing the graph.
     * @return A map of nodes to their unreachable (beyond distance 2) neighbors.
     */
    public static Map<Node, List<Node>> satisfiesDiameter2(List<Node> nodes, Map<Node, Map<Node, Double>> graph) {
        Map<Node, List<Node>> violatingNodes = new HashMap<>();

        for (Node u : nodes) {
            List<Node> unreachableNodes = new ArrayList<>();
            Map<Node, Integer> distances = bfs(u, graph);

            for (Node v : nodes) {
                if (!u.equals(v) && distances.getOrDefault(v, Integer.MAX_VALUE) > 2) {
                    unreachableNodes.add(v);
                }
            }

            if (!unreachableNodes.isEmpty()) {
                violatingNodes.put(u, unreachableNodes);
            }
        }

        return violatingNodes;
    }

    /**
     * Finds the best edge (lowest weight) to connect two specific nodes that violate
     * the diameter-2 property.
     *
     * Time Complexity: O(E), where E is the number of original edges.
     * Space Complexity: O(1)
     *
     * @param source The source node.
     * @param target The target node.
     * @param allOriginalEdges All original edges in the graph.
     * @param mstEdges MST edges already selected.
     * @param sd2Edges Previously selected SD2 edges.
     * @return The best edge to connect the source and target.
     */
    private static Edge findBestEdgeForConnection(Node source, Node target,
                                                  List<Edge> allOriginalEdges,
                                                  List<Edge> mstEdges,
                                                  List<Edge> sd2Edges) {
        Edge bestEdge = null;
        double bestWeight = Double.MAX_VALUE;

        // Try direct edges first
        for (Edge edge : allOriginalEdges) {
            boolean connectsNodes = (edge.getSource().equals(source) && edge.getDestination().equals(target)) ||
                    (edge.getSource().equals(target) && edge.getDestination().equals(source));
            if (connectsNodes && !isEdgeInList(mstEdges, edge) && !isEdgeInList(sd2Edges, edge)) {
                if (edge.getWeight() < bestWeight) {
                    bestEdge = edge;
                    bestWeight = edge.getWeight();
                }
            }
        }

        // If no direct edge, select the lowest-weight edge that reduces distance
        if (bestEdge == null) {
            for (Edge edge : allOriginalEdges) {
                if (!isEdgeInList(mstEdges, edge) && !isEdgeInList(sd2Edges, edge)) {
                    // Check if edge connects to source or target
                    boolean connectsToSource = edge.getSource().equals(source) || edge.getDestination().equals(source);
                    boolean connectsToTarget = edge.getSource().equals(target) || edge.getDestination().equals(target);
                    if ((connectsToSource || connectsToTarget) && edge.getWeight() < bestWeight) {
                        bestEdge = edge;
                        bestWeight = edge.getWeight();
                    }
                }
            }
        }

        if (bestEdge != null) {
            System.out.println("Selected best edge: " + bestEdge.getSource().getName() + " -> " +
                    bestEdge.getDestination().getName() + " (Weight: " + bestEdge.getWeight() + ")");
        } else {
            System.out.println("No suitable edge found for " + source.getName() + " -> " + target.getName());
        }

        return bestEdge;
    }

    /**
     * Applies the Floyd-Warshall algorithm to compute all-pairs shortest paths.
     *
     * Time Complexity: O(V^3)
     * Space Complexity: O(V^2)
     *
     * @param nodes The list of graph nodes.
     * @return A 2D array representing shortest distances between all pairs of nodes.
     */
    public static double[][] floydWarshall(List<Node> nodes) {
        int n = nodes.size();
        double[][] dist = new double[n][n];
        
        // Initial assignment
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = Double.MAX_VALUE;
                }
            }
        }
        
        // Add existing edges
        for (int i = 0; i < n; i++) {
            Node node = nodes.get(i);
            for (Edge edge : node.getEdges()) {
                int j = nodes.indexOf(edge.getDestination());
                if (j != -1) {
                    dist[i][j] = Math.min(dist[i][j], edge.getWeight());
                }
            }
        }
        
        // Floyd-Warshall algorithm
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }
        
        return dist;
    }

    /**
     * Finds the center of the graph (node with minimum sum of distances to others).
     *
     * Time Complexity: O(V^2)
     * Space Complexity: O(V^2)
     *
     * @param nodes The list of graph nodes.
     * @return The center node of the graph.
     */
    public static Node findCenter(List<Node> nodes) {
        if (nodes.isEmpty()) return null;
        
        double[][] dist = floydWarshall(nodes);
        int n = nodes.size();
        
        double minSum = Double.MAX_VALUE;
        int centerIdx = 0;
        
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (dist[i][j] != Double.MAX_VALUE) {
                    sum += dist[i][j];
                } else {
                    sum = Double.MAX_VALUE;
                    break;
                }
            }
            
            if (sum < minSum) {
                minSum = sum;
                centerIdx = i;
            }
        }
        
        return nodes.get(centerIdx);
    }

    /**
     * Calculates the diameter of the graph (maximum shortest path length).
     *
     * Time Complexity: O(V^2)
     * Space Complexity: O(V^2)
     *
     * @param nodes The list of graph nodes.
     * @return The diameter of the graph.
     */
    public static double calculateDiameter(List<Node> nodes) {
        double[][] dist = floydWarshall(nodes);
        int n = nodes.size();
        
        double diameter = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][j] != Double.MAX_VALUE) {
                    diameter = Math.max(diameter, dist[i][j]);
                }
            }
        }
        
        return diameter;
    }

    /**
     * Calculates the radius of the graph (minimum eccentricity among all nodes).
     *
     * Time Complexity: O(V^2)
     * Space Complexity: O(V^2)
     *
     * @param nodes The list of graph nodes.
     * @return The radius of the graph.
     */
    public static double calculateRadius(List<Node> nodes) {
        double[][] dist = floydWarshall(nodes);
        int n = nodes.size();
        
        double radius = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double eccentricity = 0;
            for (int j = 0; j < n; j++) {
                if (dist[i][j] != Double.MAX_VALUE) {
                    eccentricity = Math.max(eccentricity, dist[i][j]);
                } else {
                    eccentricity = Double.MAX_VALUE;
                    break;
                }
            }
            
            if (eccentricity < radius) {
                radius = eccentricity;
            }
        }
        
        return radius;
    }

    /**
     * Prints the distance matrix between all pairs of nodes.
     *
     * Time Complexity: O(V^2)
     * Space Complexity: O(V^2)
     *
     * @param nodes The list of graph nodes.
     */
    public static void printDistanceMatrix(List<Node> nodes) {
        double[][] dist = floydWarshall(nodes);
        int n = nodes.size();
        
        System.out.println("Distance Matrix:");
        System.out.print("     ");
        for (int i = 0; i < n; i++) {
            System.out.printf("%8s", nodes.get(i).getName().substring(0, 
                Math.min(7, nodes.get(i).getName().length())));
        }
        System.out.println();
        
        for (int i = 0; i < n; i++) {
            System.out.printf("%8s", nodes.get(i).getName().substring(0, 
                Math.min(7, nodes.get(i).getName().length())));
            for (int j = 0; j < n; j++) {
                if (dist[i][j] == Double.MAX_VALUE) {
                    System.out.printf("%8s", "∞");
                } else {
                    System.out.printf("%8.1f", dist[i][j]);
                }
            }
            System.out.println();
        }
    }
} 
package algorithms;

import models.Color;
import models.Edge;
import models.Node;
import java.util.*;

/**
 * Utility class for performing Breadth-First Search (BFS) and connectivity checks on graphs.
 */
public class Bfs {

    /**
     * Performs BFS from a starting node over an adjacency list and returns distances.
     *
     * @param start The starting node for the BFS
     * @param graph The adjacency list representing the graph
     * @return A map of nodes to their distance (in hops) from the start node
     *
     * @timeComplexity O(V + E) where V is the number of vertices and E is the number of edges
     * @spaceComplexity O(V) for the distance map and queue
     */
    public static Map<Node, Integer> bfs(Node start, Map<Node, Map<Node, Double>> graph) {
        Map<Node, Integer> dist = new HashMap<>();
        Queue<Node> queue = new LinkedList<>();
        dist.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            int d = dist.get(u);
            if (graph.containsKey(u)) {
                for (Node v : graph.get(u).keySet()) {
                    if (!dist.containsKey(v)) {
                        dist.put(v, d + 1);
                        queue.add(v);
                    }
                }
            }
        }
        return dist;
    }

    /**
     * Checks if the entire graph represented by the list of nodes is connected.
     * Assumes edges are bidirectional.
     *
     * @param nodes List of all nodes in the graph
     * @return True if all nodes are reachable from any starting node, false otherwise
     *
     * @timeComplexity O(V + E)
     * @spaceComplexity O(V) for the visited set and queue
     */
    public static boolean isConnected(List<Node> nodes) {
        if (nodes.isEmpty()) return true;
        
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        
        queue.offer(nodes.getFirst());
        visited.add(nodes.getFirst());
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        return visited.size() == nodes.size();
    }
    /**
     * Finds all connected components in the graph using BFS.
     * Each component is returned as a list of nodes.
     *
     * @param nodes The list of all nodes in the graph
     * @return A list of components where each component is a list of nodes
     *
     * @timeComplexity O(V + E)
     * @spaceComplexity O(V + C) where C is the number of connected components
     */
    public static List<List<Node>> findConnectedComponents(List<Node> nodes) {
        List<List<Node>> components = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        
        for (Node node : nodes) {
            if (!visited.contains(node)) {
                List<Node> component = new ArrayList<>();
                Queue<Node> queue = new LinkedList<>();
                
                queue.offer(node);
                visited.add(node);
                
                while (!queue.isEmpty()) {
                    Node current = queue.poll();
                    component.add(current);
                    
                    for (Edge edge : current.getEdges()) {
                        Node neighbor = edge.getDestination();
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                        }
                    }
                }
                
                components.add(component);
            }
        }
        
        return components;
    }
} 
package algorithms;

import models.Color;
import models.Edge;
import models.Node;


import java.util.*;

public class Bfs {

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
    
    public static List<Node> breadthFirstSearch(List<Node> nodes, Node start, Node target) {
        if (start == null || target == null) return new ArrayList<>();
        
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        Map<Node, Node> parent = new HashMap<>();
        
        queue.offer(start);
        visited.add(start);
        parent.put(start, null);
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            if (current.equals(target)) {
                // Path reconstruction
                List<Node> path = new ArrayList<>();
                Node node = target;
                while (node != null) {
                    path.add(0, node);
                    node = parent.get(node);
                }
                return path;
            }
            
            // Check neighbors
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return new ArrayList<>(); // Path not found
    }
    
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
    
    public static void colorGraph(List<Node> nodes) {
        // Graph coloring using BFS
        for (Node node : nodes) {
            node.setColor(Color.WHITE);
        }
        
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.PURPLE, Color.ORANGE};
        
        for (Node node : nodes) {
            if (node.getColor() == Color.WHITE) {
                Queue<Node> queue = new LinkedList<>();
                queue.offer(node);
                node.setColor(Color.GRAY);
                
                while (!queue.isEmpty()) {
                    Node current = queue.poll();
                    
                    // Find the first suitable color
                    for (Color color : colors) {
                        boolean canUse = true;
                        for (Edge edge : current.getEdges()) {
                            if (edge.getDestination().getColor() == color) {
                                canUse = false;
                                break;
                            }
                        }
                        if (canUse) {
                            current.setColor(color);
                            break;
                        }
                    }
                    
                    // Add neighbors to queue
                    for (Edge edge : current.getEdges()) {
                        Node neighbor = edge.getDestination();
                        if (neighbor.getColor() == Color.WHITE) {
                            neighbor.setColor(Color.GRAY);
                            queue.offer(neighbor);
                        }
                    }
                }
            }
        }
    }
} 
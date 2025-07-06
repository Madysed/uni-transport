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
} 
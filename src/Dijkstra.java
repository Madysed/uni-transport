import java.util.*;

public class Dijkstra {
    
    public static List<Node> findShortestPath(List<Node> nodes, Node start, Node end) {
        // Initial setup
        for (Node node : nodes) {
            node.setDistance(Double.MAX_VALUE);
            node.setVisited(false);
            node.setParent(null);
        }
        
        start.setDistance(0);
        
        // Priority Queue for selecting node with minimum distance
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));
        pq.offer(start);
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            
            if (current.isVisited()) continue;
            current.setVisited(true);
            
            if (current.equals(end)) {
                break;
            }
            
            // Check neighbors
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getDestination();
                
                if (!neighbor.isVisited()) {
                    double newDistance = current.getDistance() + edge.getWeight();
                    
                    if (newDistance < neighbor.getDistance()) {
                        neighbor.setDistance(newDistance);
                        neighbor.setParent(current);
                        pq.offer(neighbor);
                    }
                }
            }
        }
        
        // Build path
        List<Node> path = new ArrayList<>();
        Node current = end;
        
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        
        // If no path found
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return new ArrayList<>();
        }
        
        return path;
    }
    
    public static Map<Node, Double> findAllDistances(List<Node> nodes, Node start) {
        Map<Node, Double> distances = new HashMap<>();
        
        // Initial setup
        for (Node node : nodes) {
            node.setDistance(Double.MAX_VALUE);
            node.setVisited(false);
            node.setParent(null);
        }
        
        start.setDistance(0);
        
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));
        pq.offer(start);
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            
            if (current.isVisited()) continue;
            current.setVisited(true);
            
            distances.put(current, current.getDistance());
            
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getDestination();
                
                if (!neighbor.isVisited()) {
                    double newDistance = current.getDistance() + edge.getWeight();
                    
                    if (newDistance < neighbor.getDistance()) {
                        neighbor.setDistance(newDistance);
                        neighbor.setParent(current);
                        pq.offer(neighbor);
                    }
                }
            }
        }
        
        return distances;
    }
} 
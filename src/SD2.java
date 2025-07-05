import java.util.*;

public class SD2 {
    
    // Floyd-Warshall algorithm for finding shortest paths between all pairs of nodes
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
    
    // Find shortest path between two specific nodes
    public static List<Node> findShortestPath(List<Node> nodes, Node start, Node end) {
        int n = nodes.size();
        int startIdx = nodes.indexOf(start);
        int endIdx = nodes.indexOf(end);
        
        if (startIdx == -1 || endIdx == -1) {
            return new ArrayList<>();
        }
        
        double[][] dist = floydWarshall(nodes);
        int[][] next = new int[n][n];
        
        // Initial assignment for path reconstruction
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                next[i][j] = -1;
            }
        }
        
        // Build next table
        for (int i = 0; i < n; i++) {
            Node node = nodes.get(i);
            for (Edge edge : node.getEdges()) {
                int j = nodes.indexOf(edge.getDestination());
                if (j != -1) {
                    next[i][j] = j;
                }
            }
        }
        
        // Update next table
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE) {
                        if (dist[i][k] + dist[k][j] < dist[i][j]) {
                            next[i][j] = next[i][k];
                        }
                    }
                }
            }
        }
        
        // Path reconstruction
        List<Node> path = new ArrayList<>();
        if (dist[startIdx][endIdx] == Double.MAX_VALUE) {
            return path; // Path does not exist
        }
        
        int current = startIdx;
        while (current != endIdx) {
            path.add(nodes.get(current));
            current = next[current][endIdx];
            if (current == -1) break;
        }
        
        if (current == endIdx) {
            path.add(nodes.get(endIdx));
        }
        
        return path;
    }
    
    // Find graph center (node whose sum of distances to other nodes is minimum)
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
    
    // Calculate graph diameter (longest shortest path)
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
    
    // Calculate graph radius (minimum eccentricity)
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
    
    // Display distance matrix
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
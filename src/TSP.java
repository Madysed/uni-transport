import java.util.*;

public class TSP {
    
    public static class TSPResult {
        private List<Node> path;
        private double totalCost;
        private double totalTime;
        private List<Node> visitedOrder;
        private String details;
        
        public TSPResult(List<Node> path, double totalCost, double totalTime) {
            this.path = path;
            this.totalCost = totalCost;
            this.totalTime = totalTime;
            this.visitedOrder = new ArrayList<>();
            this.details = "";
        }
        
        public List<Node> getPath() { return path; }
        public double getTotalCost() { return totalCost; }
        public double getTotalTime() { return totalTime; }
        public List<Node> getVisitedOrder() { return visitedOrder; }
        public void setVisitedOrder(List<Node> visitedOrder) { this.visitedOrder = visitedOrder; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public String getRouteDescription() {
            if (path.isEmpty()) return "No route found";
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) {
                    sb.append(" → ");
                }
            }
            return sb.toString();
        }
    }
    
    // Cost and time matrices
    private static double[][] costMatrix;
    private static double[][] timeMatrix;
    private static List<Node> nodeList;
    
    // TSP with Bitmasking and Dynamic Programming (simple and efficient)
    public static TSPResult solveTSPWithBitmasking(List<Node> universities, Node startUniversity) {
        if (universities.size() <= 1) {
            return new TSPResult(universities, 0, 0);
        }
        
        // Limit to 10 universities to avoid complexity
        if (universities.size() > 10) {
            universities = universities.subList(0, 10);
        }
        
        int n = universities.size();
        nodeList = new ArrayList<>(universities);
        
        // Create cost and time matrices
        createMatrices(nodeList);
        
        // Find starting university index
        int startIndex = nodeList.indexOf(startUniversity);
        if (startIndex == -1) {
            startIndex = 0;
            startUniversity = nodeList.get(0);
        }
        
        // If only one university
        if (n == 1) {
            return new TSPResult(Arrays.asList(startUniversity), 0, 0);
        }
        
        // DP array: dp[mask][i] = minimum cost to reach i with visited universities in mask
        double[][] dp = new double[1 << n][n];
        int[][] parent = new int[1 << n][n];
        
        // Initialize values
        for (int i = 0; i < (1 << n); i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = Double.MAX_VALUE;
                parent[i][j] = -1;
            }
        }
        
        // Start from initial university
        dp[1 << startIndex][startIndex] = 0;
        
        // Fill DP table
        for (int mask = 0; mask < (1 << n); mask++) {
            for (int u = 0; u < n; u++) {
                // If u is not in mask or cost is infinite, continue
                if ((mask & (1 << u)) == 0 || dp[mask][u] == Double.MAX_VALUE) {
                    continue;
                }
                
                // Check all remaining universities
                for (int v = 0; v < n; v++) {
                    // If v is already visited, continue
                    if ((mask & (1 << v)) != 0) {
                        continue;
                    }
                    
                    int newMask = mask | (1 << v);
                    double newCost = dp[mask][u] + costMatrix[u][v];
                    
                    if (newCost < dp[newMask][v]) {
                        dp[newMask][v] = newCost;
                        parent[newMask][v] = u;
                    }
                }
            }
        }
        
        // Find best solution (return to starting point)
        double minTotalCost = Double.MAX_VALUE;
        int bestLastNode = -1;
        int allVisited = (1 << n) - 1; // All universities visited
        
        for (int i = 0; i < n; i++) {
            if (i == startIndex) continue;
            
            double totalCost = dp[allVisited][i] + costMatrix[i][startIndex];
            if (totalCost < minTotalCost) {
                minTotalCost = totalCost;
                bestLastNode = i;
            }
        }
        
        // Build final path
        List<Node> path = new ArrayList<>();
        double totalTime = 0;
        
        if (bestLastNode != -1) {
            // Reconstruct path
            List<Integer> indices = new ArrayList<>();
            int currentMask = allVisited;
            int currentNode = bestLastNode;
            
            while (currentNode != -1) {
                indices.add(currentNode);
                int nextNode = parent[currentMask][currentNode];
                currentMask ^= (1 << currentNode);
                currentNode = nextNode;
            }
            
            Collections.reverse(indices);
            
            // Convert indices to nodes
            for (int index : indices) {
                path.add(nodeList.get(index));
            }
            path.add(startUniversity); // Return to starting point
            
            // Calculate total time
            for (int i = 0; i < path.size() - 1; i++) {
                int from = nodeList.indexOf(path.get(i));
                int to = nodeList.indexOf(path.get(i + 1));
                totalTime += timeMatrix[from][to];
            }
        }
        
        TSPResult result = new TSPResult(path, minTotalCost, totalTime);
        result.setVisitedOrder(new ArrayList<>(path));
        
        // Create details
        String details = String.format("TSP Solution using Dynamic Programming with Bitmasking\n");
        details += String.format("Universities visited: %d\n", n);
        details += String.format("Total cost: %.0f T\n", minTotalCost);
        details += String.format("Total time: %.1f hours\n", totalTime);
        details += String.format("Route: %s", result.getRouteDescription());
        result.setDetails(details);
        
        return result;
    }
    
    // Create cost and time matrices
    private static void createMatrices(List<Node> nodes) {
        int n = nodes.size();
        costMatrix = new double[n][n];
        timeMatrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    costMatrix[i][j] = 0;
                    timeMatrix[i][j] = 0;
                } else {
                    // Search for direct edge
                    Edge directEdge = findDirectEdge(nodes.get(i), nodes.get(j));
                    if (directEdge != null) {
                        costMatrix[i][j] = directEdge.getCost();
                        timeMatrix[i][j] = directEdge.getTravelTime();
                    } else {
                        // Calculate based on direct distance
                        double distance = calculateDistance(nodes.get(i), nodes.get(j));
                        costMatrix[i][j] = distance * 150; // Assumption: 150 Toman per kilometer
                        timeMatrix[i][j] = distance / 60.0; // Assumption: average speed 60 km/h
                    }
                }
            }
        }
    }
    
    // Find direct edge between two nodes
    private static Edge findDirectEdge(Node from, Node to) {
        for (Edge edge : from.getEdges()) {
            if (edge.getDestination().equals(to)) {
                return edge;
            }
        }
        return null;
    }
    
    // Calculate Euclidean distance
    private static double calculateDistance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // Simple TSP with Nearest Neighbor (for comparison)
    public static TSPResult solveTSPSimple(List<Node> universities, Node startUniversity) {
        if (universities.size() <= 1) {
            return new TSPResult(universities, 0, 0);
        }
        
        List<Node> path = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        Node current = startUniversity;
        double totalCost = 0;
        double totalTime = 0;
        
        path.add(current);
        visited.add(current);
        
        // Continue until all universities are visited
        while (visited.size() < universities.size()) {
            Node nearest = null;
            double minCost = Double.MAX_VALUE;
            
            // Find nearest university
            for (Node uni : universities) {
                if (!visited.contains(uni)) {
                    Edge edge = findDirectEdge(current, uni);
                    double cost;
                    if (edge != null) {
                        cost = edge.getCost();
                    } else {
                        double distance = calculateDistance(current, uni);
                        cost = distance * 150;
                    }
                    
                    if (cost < minCost) {
                        minCost = cost;
                        nearest = uni;
                    }
                }
            }
            
            if (nearest != null) {
                path.add(nearest);
                visited.add(nearest);
                totalCost += minCost;
                
                Edge edge = findDirectEdge(current, nearest);
                if (edge != null) {
                    totalTime += edge.getTravelTime();
                } else {
                    totalTime += calculateDistance(current, nearest) / 60.0;
                }
                current = nearest;
            } else {
                break;
            }
        }
        
        // Return to starting point
        if (path.size() > 1) {
            Edge returnEdge = findDirectEdge(current, startUniversity);
            if (returnEdge != null) {
                totalCost += returnEdge.getCost();
                totalTime += returnEdge.getTravelTime();
            } else {
                double distance = calculateDistance(current, startUniversity);
                totalCost += distance * 150;
                totalTime += distance / 60.0;
            }
            path.add(startUniversity);
        }
        
        TSPResult result = new TSPResult(path, totalCost, totalTime);
        result.setVisitedOrder(new ArrayList<>(path));
        result.setDetails(String.format("Simple TSP using Nearest Neighbor\nTotal Cost: %.0f T\nTotal Time: %.1f hours", 
                                       totalCost, totalTime));
        
        return result;
    }
    
    // Show cost matrix (for debugging)
    public static String showCostMatrix(List<Node> nodes) {
        createMatrices(nodes);
        StringBuilder sb = new StringBuilder();
        sb.append("Cost Matrix:\n");
        
        // Header
        sb.append("     ");
        for (Node node : nodes) {
            sb.append(String.format("%-8s", node.getName().substring(0, Math.min(6, node.getName().length()))));
        }
        sb.append("\n");
        
        // Rows
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(String.format("%-8s", nodes.get(i).getName().substring(0, Math.min(6, nodes.get(i).getName().length()))));
            for (int j = 0; j < nodes.size(); j++) {
                sb.append(String.format("%-8.0f", costMatrix[i][j]));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
} 
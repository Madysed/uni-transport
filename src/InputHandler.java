import java.io.*;
import java.util.*;

public class InputHandler {
    
    public static List<Node> loadDataFromFile(String filename) {
        List<Node> nodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingNodes = false;
            boolean readingRoutes = false;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Identify different file sections
                if (line.equals("# Universities") || line.equals("#Universities")) {
                    readingNodes = true;
                    readingRoutes = false;
                    continue;
                } else if (line.equals("# Routes") || line.equals("#Routes")) {
                    readingNodes = false;
                    readingRoutes = true;
                    continue;
                }
                
                if (readingNodes) {
                    // Read university information
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    double x = Double.parseDouble(parts[1].trim());
                    double y = Double.parseDouble(parts[2].trim());
                        nodes.add(new Node(name, x, y));
            }
                } else if (readingRoutes) {
                    // Read route information
                String[] parts = line.split(",");
                    if (parts.length >= 6) {
                    String sourceName = parts[0].trim();
                    String destName = parts[1].trim();
                    double weight = Double.parseDouble(parts[2].trim());
                        double cost = Double.parseDouble(parts[3].trim());
                        int capacity = Integer.parseInt(parts[4].trim());
                        String operatingHours = parts[5].trim();
                    
                    Node source = findNodeByName(nodes, sourceName);
                    Node destination = findNodeByName(nodes, destName);
                    
                    if (source != null && destination != null) {
                            // Create bidirectional edge
                            Edge edge1 = new Edge(source, destination, weight, cost, capacity, operatingHours);
                            Edge edge2 = new Edge(destination, source, weight, cost, capacity, operatingHours);
                            source.addEdge(edge1);
                            destination.addEdge(edge2);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing data: " + e.getMessage());
        }
        
        // Return only nodes that have at least one route
        List<Node> connectedNodes = new ArrayList<>();
        for (Node node : nodes) {
            if (!node.getEdges().isEmpty()) {
                connectedNodes.add(node);
            }
        }
        
        return connectedNodes;
    }
    
    public static void saveDataToFile(String filename, List<Node> nodes) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Write universities section
            pw.println("# Universities");
            for (Node node : nodes) {
                pw.println(node.getName() + "," + node.getX() + "," + node.getY());
            }
            
            pw.println();
            
            // Write routes section
            pw.println("# Routes");
            Set<String> writtenEdges = new HashSet<>();
            
            for (Node node : nodes) {
                for (Edge edge : node.getEdges()) {
                    String edgeKey = edge.getSource().getName() + "-" + edge.getDestination().getName();
                    String reverseKey = edge.getDestination().getName() + "-" + edge.getSource().getName();
                    
                    if (!writtenEdges.contains(edgeKey) && !writtenEdges.contains(reverseKey)) {
                        pw.println(edge.getSource().getName() + "," + 
                                 edge.getDestination().getName() + "," + 
                                 edge.getWeight() + "," + 
                                 edge.getCost() + "," +
                                 edge.getCapacity() + "," +
                                 edge.getOperatingHours());
                        writtenEdges.add(edgeKey);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }
    
    // Old functions - for compatibility
    public static List<Node> loadNodesFromFile(String filename) {
        return loadDataFromFile(filename);
    }
    
    public static void loadEdgesFromFile(String filename, List<Node> nodes) {
        // This function is no longer used because everything is done in loadDataFromFile
        // But kept for compatibility
    }
    
    public static void saveNodesToFile(String filename, List<Node> nodes) {
        saveDataToFile(filename, nodes);
    }
    
    public static void saveEdgesToFile(String filename, List<Node> nodes) {
        // This function is no longer used because everything is done in saveDataToFile
        // But kept for compatibility
    }
    
    private static Node findNodeByName(List<Node> nodes, String name) {
        for (Node node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }
    
    public static void saveNodeToFile(Node newNode, String filename) {
        try {
            // Read existing content
            List<String> lines = new ArrayList<>();
            boolean fileExists = false;
            
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                fileExists = true;
            } catch (IOException e) {
                // File doesn't exist, we'll create a new one
            }
            
            // Write new content
            try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
                if (!fileExists || lines.isEmpty()) {
                    // New file
                    pw.println("# Universities");
                    pw.println(newNode.getName() + "," + newNode.getX() + "," + newNode.getY());
                    pw.println();
                    pw.println("# Routes");
                } else {
                    // Add to existing file
                    boolean addedToUniversities = false;
                    
                    for (String line : lines) {
                        if (line.trim().equals("# Routes") || line.trim().equals("#Routes")) {
                            if (!addedToUniversities) {
                                pw.println(newNode.getName() + "," + newNode.getX() + "," + newNode.getY());
                                addedToUniversities = true;
                            }
                        }
                        pw.println(line);
                    }
                    
                    // If universities section didn't exist
                    if (!addedToUniversities) {
                        pw.println(newNode.getName() + "," + newNode.getX() + "," + newNode.getY());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving node to file: " + e.getMessage());
        }
    }
} 
import algorithms.*;
import gui.GraphApp;
import models.Edge;
import models.Node;
import utils.InputHandler;

import java.util.*;

public class Main {
    private static List<Node> universities = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean running = true;
    
    public static void main(String[] args) {
        System.out.println("Hello and Welcome!");
        System.out.println("=== Transport System Input Menu ===");
        
        while (running) {
            showMenu();
            int choice = getChoice();
            handleChoice(choice);
        }
        
        scanner.close();
    }
    
    private static void showMenu() {
        System.out.println("\n=== Transport System Input Menu ===");
        System.out.println("1. Add Universities");
        System.out.println("2. Add Routes");
        System.out.println("3. Load from File");
        System.out.println("4. Display Universities");
        System.out.println("5. Display Routes");
        System.out.println("6. Display Graph Structure");
        System.out.println("7. Save to File");
        System.out.println("8. Launch Graph Visualizer");
        System.out.println("9. Exit");
        System.out.print("Choose option: ");
    }
    
    private static int getChoice() {
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void handleChoice(int choice) {
        switch (choice) {
            case 1:
                addUniversities();
                break;
            case 2:
                addRoutes();
                break;
            case 3:
                loadFromFile();
                break;
            case 4:
                displayUniversities();
                break;
            case 5:
                displayRoutes();
                break;
            case 6:
                displayGraphStructure();
                break;
            case 7:
                saveToFile();
                break;
            case 8:
                launchGraphVisualizer();
                break;
            case 9:
                running = false;
                System.out.println("Goodbye!");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    private static void addUniversities() {
        System.out.println("\n=== Add Universities ===");
        System.out.print("Enter number of universities to add: ");
        
        try {
            String countStr = scanner.nextLine().trim();
            int count = Integer.parseInt(countStr);
        
                    for (int i = 0; i < count; i++) {
            System.out.println("University " + (i + 1) + ":");
            System.out.print("Name: ");
                String name = scanner.nextLine().trim();
                
                if (name.isEmpty()) {
                    System.out.println("Invalid name. Skipping...");
                    continue;
                }
                
                // Generate random coordinates for graphics display
                double x = 150 + Math.random() * 500;
                double y = 150 + Math.random() * 400;
            
            Node university = new Node(name, x, y);
            universities.add(university);
            System.out.println("University added successfully!");
                
                // Update GUI if open
                GraphApp.updateFromConsole(universities);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please try again.");
        }
    }
    
    private static void addRoutes() {
        System.out.println("\n=== Add Routes ===");
        if (universities.size() < 2) {
            System.out.println("Please add at least 2 universities first.");
            return;
        }
        
        System.out.println("Available Universities:");
        for (int i = 0; i < universities.size(); i++) {
            System.out.println((i + 1) + ". " + universities.get(i).getName());
        }
        
        System.out.print("Enter number of routes to add: ");
        
        try {
            String routeCountStr = scanner.nextLine().trim();
            int routeCount = Integer.parseInt(routeCountStr);
        
        for (int i = 0; i < routeCount; i++) {
            System.out.println("Route " + (i + 1) + ":");
            System.out.print("From university (number): ");
                int fromIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            System.out.print("To university (number): ");
                int toIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            System.out.print("Distance (km): ");
                double distance = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Cost (Toman): ");
                double cost = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Capacity (passengers): ");
                int capacity = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Operating hours (e.g., 06:00-22:00): ");
                String operatingHours = scanner.nextLine().trim();
            
            if (fromIndex >= 0 && fromIndex < universities.size() && 
                toIndex >= 0 && toIndex < universities.size()) {
                
                Node from = universities.get(fromIndex);
                Node to = universities.get(toIndex);
                
                // Add bidirectional edge
                Edge edge1 = new Edge(from, to, distance, cost, capacity, operatingHours);
                Edge edge2 = new Edge(to, from, distance, cost, capacity, operatingHours);
                
                from.addEdge(edge1);
                to.addEdge(edge2);
                
                System.out.println("Route added successfully!");
                
                // Update GUI if open
                GraphApp.updateFromConsole(universities);
            } else {
                System.out.println("Invalid university indices.");
            }
        }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please try again.");
        }
    }
    
    private static void loadFromFile() {
        System.out.println("\n=== Load from File ===");
        System.out.print("Enter data file name (default: transport_data.txt): ");
        String dataFile = scanner.nextLine().trim();
        if (dataFile.isEmpty()) {
            dataFile = "transport_data.txt";
        }
        
        // Load all data from one file
        universities = InputHandler.loadDataFromFile(dataFile);
        
        if (!universities.isEmpty()) {
            System.out.println("Loaded " + universities.size() + " universities successfully from " + dataFile + "!");
            
            // Update GUI if open
            GraphApp.updateFromConsole(universities);
        } else {
            System.out.println("No universities found in file. Please add universities manually.");
        }
    }
    
    private static void displayUniversities() {
        System.out.println("\n=== Universities List ===");
        if (universities.isEmpty()) {
            System.out.println("No universities found.");
            return;
        }
        
        for (int i = 0; i < universities.size(); i++) {
            Node uni = universities.get(i);
            System.out.println((i + 1) + ". " + uni.getName() + 
                             " - Position: (" + uni.getX() + ", " + uni.getY() + ")");
        }
    }
    
    private static void displayRoutes() {
        System.out.println("\n=== Routes List ===");
        if (universities.isEmpty()) {
            System.out.println("No universities found.");
            return;
        }
        
        int routeCount = 0;
        for (Node uni : universities) {
            for (Edge edge : uni.getEdges()) {
                routeCount++;
                System.out.println(routeCount + ". " + edge.getSource().getName() + 
                                 " -> " + edge.getDestination().getName() + 
                                 " (Distance: " + edge.getWeight() + " km, " + 
                                 "Cost: " + edge.getCost() + " T, " +
                                 "Capacity: " + edge.getCapacity() + ", " +
                                 "Time: " + String.format("%.2f", edge.getTravelTime()) + " h)");
            }
        }
        
        if (routeCount == 0) {
            System.out.println("No routes found.");
        }
    }
    
    private static void displayGraphStructure() {
        System.out.println("\n=== Graph Structure ===");
        if (universities.isEmpty()) {
            System.out.println("No universities found.");
            return;
        }
        
        System.out.println("Graph Analysis:");
        System.out.println("- Number of nodes (universities): " + universities.size());
        
        int totalEdges = 0;
        for (Node uni : universities) {
            totalEdges += uni.getEdges().size();
        }
        System.out.println("- Number of edges (routes): " + totalEdges);
        
        // Display adjacency matrix
        System.out.println("\nAdjacency Matrix:");
        System.out.print("     ");
        for (Node node : universities) {
            System.out.printf("%8s", node.getName().substring(0,
                    Math.min(7, node.getName().length())));
        }
        System.out.println();
        
        for (int i = 0; i < universities.size(); i++) {
            System.out.printf("%8s", universities.get(i).getName().substring(0, 
                Math.min(7, universities.get(i).getName().length())));
            for (Node university : universities) {
                double weight = getEdgeWeight(universities.get(i), university);
                if (weight > 0) {
                    System.out.printf("%8.0f", weight);
                } else {
                    System.out.printf("%8s", "∞");
                }
            }
            System.out.println();
        }
        
        // Run algorithms
        runAlgorithms();
    }
    
    private static void runAlgorithms() {
        System.out.println("\n=== Algorithm Results ===");
        
        // Phase 1: MST
        System.out.println("\n1. Minimum Spanning Tree (MST):");
        List<Edge> mstEdges = Kruskal.findMST(universities);
        double mstCost = Kruskal.calculateMSTCost(mstEdges);
        System.out.println("   MST Cost: " + String.format("%.2f", mstCost) + " km");
        System.out.println("   MST Edges:");
        for (Edge edge : mstEdges) {
            System.out.println("     " + edge.getSource().getName() + 
                             " <-> " + edge.getDestination().getName() + 
                             " (" + edge.getWeight() + " km)");
        }
        
        // Phase 2: Shortest Path
        if (universities.size() >= 2) {
            System.out.println("\n2. Shortest Path (Dijkstra):");
            Node start = universities.getFirst();
            Node end = universities.getLast();
            
            List<Node> shortestPath = Dijkstra.findShortestPath(universities, start, end);
            if (!shortestPath.isEmpty()) {
                System.out.println("   Path from " + start.getName() + " to " + end.getName() + ":");
                for (int i = 0; i < shortestPath.size(); i++) {
                    System.out.print("     " + shortestPath.get(i).getName());
                    if (i < shortestPath.size() - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println();
                System.out.println("   Total Distance: " + String.format("%.2f", end.getDistance()) + " km");
            } else {
                System.out.println("   No path found.");
            }
        }
        
        // Phase 3: TSP
        if (universities.size() >= 3) {
            System.out.println("\n3. Traveling Salesman Problem (TSP):");
            Node startTSP = universities.getFirst();
            TSP.TSPResult tspResult = TSP.solveTSPWithBitmasking(universities, startTSP, TSP.OptimizationType.COST);
            
            System.out.println("   TSP Tour starting from " + startTSP.getName() + ":");
            List<Node> tspPath = tspResult.getPath();
            for (int i = 0; i < tspPath.size(); i++) {
                System.out.print("     " + tspPath.get(i).getName());
                if (i < tspPath.size() - 1) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
            System.out.println("   Total Tour Cost: " + String.format("%.2f", tspResult.getTotalCost()) + " km");
        }
        
        // Bonus sections
        System.out.println("\n=== Bonus Features ===");
        
        // Check graph connectivity
        boolean isConnected = Bfs.isConnected(universities);
        System.out.println("Graph is connected: " + (isConnected ? "Yes" : "No"));
        
        // Connected components
        List<List<Node>> components = Bfs.findConnectedComponents(universities);
        System.out.println("Number of connected components: " + components.size());
        
        // Graph center
        Node center = SD2.findCenter(universities);
        if (center != null) {
            System.out.println("Graph center: " + center.getName());
        }
        
        // Graph diameter and radius
        double diameter = SD2.calculateDiameter(universities);
        double radius = SD2.calculateRadius(universities);
        System.out.println("Graph diameter: " + String.format("%.2f", diameter) + " km");
        System.out.println("Graph radius: " + String.format("%.2f", radius) + " km");
        
        // Display distance matrix
        System.out.println("\n=== All-Pairs Shortest Distances ===");
        SD2.printDistanceMatrix(universities);
    }
    
    private static double getEdgeWeight(Node from, Node to) {
        for (Edge edge : from.getEdges()) {
            if (edge.getDestination().equals(to)) {
                return edge.getWeight();
            }
        }
        return 0;
    }
    
    private static void saveToFile() {
        System.out.println("\n=== Save to File ===");
        if (universities.isEmpty()) {
            System.out.println("No data to save.");
            return;
        }
        
        System.out.print("Enter data file name (default: transport_data.txt): ");
        String dataFile = scanner.nextLine().trim();
        if (dataFile.isEmpty()) {
            dataFile = "transport_data.txt";
        }
        
        InputHandler.saveDataToFile(dataFile, universities);
        
        System.out.println("Data saved successfully to " + dataFile + "!");
        
        // Update GUI if open
        GraphApp.updateFromConsole(universities);
    }
    
    private static void launchGraphVisualizer() {
        System.out.println("\n=== Launching Graph Visualizer ===");
        System.out.println("Starting GUI with current data...");
        
        // Save current data to file for GUI
        if (!universities.isEmpty()) {
            InputHandler.saveDataToFile("transport_data.txt", universities);
            System.out.println("Current data saved to transport_data.txt for GUI");
        }
        
        // Start graphical interface
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Set system Look and Feel
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // In case of error, use default
            }

            GraphApp app = new GraphApp();
            app.setVisible(true);

            // Update graph with console data
            GraphApp.updateFromConsole(universities);
        });
        
        System.out.println("GUI launched successfully!");
        System.out.println("You can continue using console commands while GUI is running.");
        System.out.println("Any changes in console will be reflected in GUI automatically.");
    }
} 
package resources;

public class Global_variables {
    public static final int NODE_RADIUS = 35;
    public static final java.awt.Color NODE_COLOR = new java.awt.Color(173, 216, 230);
    public static final java.awt.Color HIGHLIGHTED_NODE_COLOR = new java.awt.Color(255, 215, 0);
    public static final java.awt.Color EDGE_COLOR = new java.awt.Color(70, 70, 70);
    public static final java.awt.Color HIGHLIGHTED_EDGE_COLOR = new java.awt.Color(220, 20, 60);
    public static final java.awt.Color HIGHLIGHTED_EDGE_COLOR_YELLOW = new java.awt.Color(255, 255, 0);;//yellow
    public static final java.awt.Color PATH_COLOR = new java.awt.Color(30, 144, 255);
    public static final java.awt.Color TEXT_COLOR = new java.awt.Color(25, 25, 112);
    public static final java.awt.Color BACKGROUND_COLOR = new java.awt.Color(248, 248, 255);
    public static final java.awt.Color ANIMATION_COLOR = new java.awt.Color(255, 69, 0);
    public static final java.awt.Color VISITED_COLOR = new java.awt.Color(144, 238, 144);

    // Enhanced colors for animations
    public static final java.awt.Color DIJKSTRA_START_COLOR = new java.awt.Color(0, 123, 255);      // Light blue
    public static final java.awt.Color DIJKSTRA_CURRENT_COLOR = new java.awt.Color(255, 193, 7);    // Golden yellow
    public static final java.awt.Color DIJKSTRA_VISITED_COLOR = new java.awt.Color(40, 167, 69);    // Green
    public static final java.awt.Color DIJKSTRA_DESTINATION_COLOR = new java.awt.Color(220, 53, 69); // Red
    public static final java.awt.Color DIJKSTRA_UNVISITED_COLOR = new java.awt.Color(108, 117, 125); // Gray
    public static final java.awt.Color DIJKSTRA_CANDIDATE_COLOR = new java.awt.Color(255, 206, 84);  // Light yellow
    public static final java.awt.Color DIJKSTRA_EXPLORING_COLOR = new java.awt.Color(255, 99, 71);   // Orange red

    // Special colors for smart Dijkstra
    public static final java.awt.Color SMART_DIJKSTRA_START_COLOR = new java.awt.Color(13, 110, 253);   // Dark blue
    public static final java.awt.Color SMART_DIJKSTRA_CURRENT_COLOR = new java.awt.Color(255, 143, 0);  // Orange
    public static final java.awt.Color SMART_DIJKSTRA_VISITED_COLOR = new java.awt.Color(25, 135, 84);  // Dark green
    public static final java.awt.Color SMART_DIJKSTRA_DESTINATION_COLOR = new java.awt.Color(214, 51, 132); // Pink red
    public static final java.awt.Color SMART_DIJKSTRA_UNVISITED_COLOR = new java.awt.Color(173, 181, 189); // Light gray
    public static final java.awt.Color SMART_DIJKSTRA_PROCESSING_COLOR = new java.awt.Color(255, 87, 34);  // Orange red
    public static final java.awt.Color SMART_DIJKSTRA_OPTIMAL_PATH_COLOR = new java.awt.Color(156, 39, 176); // Purple

    // General animation colors
    public static final java.awt.Color ANIMATION_GLOW_COLOR = new java.awt.Color(255, 255, 0, 80);
    public static final java.awt.Color PULSE_COLOR = new java.awt.Color(255, 255, 255, 150);

    // Constants for visualization
    public static final int EDGE_THICKNESS = 2;
    public static final int HIGHLIGHTED_EDGE_THICKNESS = 4;
    public static final int ANIMATION_DELAY = 500;

    // Colors for different transport types
    public static final java.awt.Color BUS_COLOR = new java.awt.Color(255, 165, 0); // Orange
    public static final java.awt.Color TRAIN_COLOR = new java.awt.Color(0, 128, 255); // Blue
    public static final java.awt.Color PLANE_COLOR = new java.awt.Color(255, 0, 0); // Red
    public static final java.awt.Color DEFAULT_COLOR = java.awt.Color.GRAY;

    // UI Colors
    public static final java.awt.Color MST_COLOR = java.awt.Color.GREEN;

    // Transport costs per km
    public static final double BUS_COST_PER_KM = 0.5;
    public static final double TRAIN_COST_PER_KM = 0.8;
    public static final double PLANE_COST_PER_KM = 2.0;

    // Transport speeds (km/h)
    public static final double BUS_SPEED = 60.0;
    public static final double TRAIN_SPEED = 120.0;
    public static final double PLANE_SPEED = 500.0;

    // Application settings
    public static final String APP_TITLE = "Transport System - University Network";
    public static final String APP_VERSION = "1.0";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    // File settings
    public static final String DEFAULT_UNIVERSITIES_FILE = "universities.txt";
    public static final String DEFAULT_ROUTES_FILE = "routes.txt";
    public static final String GRAPH_EXPORT_FILE = "graph_export.png";

    // Algorithm settings
    public static final int MAX_TSP_NODES = 15; // Maximum nodes for exact TSP
    public static final double EPSILON = 1e-9; // Small value for double comparisons

    // Graph drawing settings
    public static final int GRID_SIZE = 50;
    public static final boolean SHOW_GRID = false;
    public static final boolean SHOW_COORDINATES = true;
    public static final boolean SHOW_EDGE_WEIGHTS = true;
    public static final boolean SHOW_NODE_LABELS = true;

    // Font settings
    public static final java.awt.Font NODE_FONT = new java.awt.Font("Arial", java.awt.Font.BOLD, 12);
    public static final java.awt.Font EDGE_FONT = new java.awt.Font("Arial", java.awt.Font.PLAIN, 10);
    public static final java.awt.Font UI_FONT = new java.awt.Font("Arial", java.awt.Font.PLAIN, 14);

    // Status messages
    public static final String MSG_READY = "Ready";
    public static final String MSG_CALCULATING = "Calculating...";
    public static final String MSG_COMPLETE = "Algorithm Complete!";
    public static final String MSG_NO_PATH = "No path found";
    public static final String MSG_LOADING = "Loading data...";
    public static final String MSG_SAVING = "Saving data...";
    public static final String MSG_FILE_NOT_FOUND = "File not found";
    public static final String MSG_INVALID_INPUT = "Invalid input";

    // Heat map colors for visualization
    public static final java.awt.Color[] HEAT_MAP_COLORS = {
        new java.awt.Color(0, 0, 255),     // Blue (cold)
        new java.awt.Color(0, 255, 255),   // Cyan
        new java.awt.Color(0, 255, 0),     // Green
        new java.awt.Color(255, 255, 0),   // Yellow
        new java.awt.Color(255, 165, 0),   // Orange
        new java.awt.Color(255, 0, 0)      // Red (hot)
    };

    // Helper methods
    public static java.awt.Color getTransportColor(String transportType) {
        switch (transportType.toLowerCase()) {
            case "bus": return BUS_COLOR;
            case "train": return TRAIN_COLOR;
            case "plane": return PLANE_COLOR;
            default: return DEFAULT_COLOR;
        }
    }

    public static double getTransportCost(String transportType, double distance) {
        switch (transportType.toLowerCase()) {
            case "bus": return distance * BUS_COST_PER_KM;
            case "train": return distance * TRAIN_COST_PER_KM;
            case "plane": return distance * PLANE_COST_PER_KM;
            default: return distance * BUS_COST_PER_KM;
        }
    }

    public static double getTransportTime(String transportType, double distance) {
        switch (transportType.toLowerCase()) {
            case "bus": return distance / BUS_SPEED;
            case "train": return distance / TRAIN_SPEED;
            case "plane": return distance / PLANE_SPEED;
            default: return distance / BUS_SPEED;
        }
    }

    public static boolean isValidCoordinate(double x, double y) {
        return x >= 0 && y >= 0 && x <= 1000 && y <= 1000;
    }

    public static boolean isValidDistance(double distance) {
        return distance > 0 && distance <= 10000;
    }

    public static String formatDistance(double distance) {
        if (distance < 1000) {
            return String.format("%.1f km", distance);
        } else {
            return String.format("%.0f km", distance);
        }
    }

    public static String formatTime(double hours) {
        if (hours < 1) {
            return String.format("%.0f min", hours * 60);
        } else if (hours < 24) {
            return String.format("%.1f hours", hours);
        } else {
            return String.format("%.1f days", hours / 24);
        }
    }

    public static String formatCost(double cost) {
        return String.format("$%.2f", cost);
    }
}
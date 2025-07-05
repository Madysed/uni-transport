package models;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private double x, y;
    private List<Edge> edges;
    private boolean visited;
    private double distance;
    private Node parent;
    private Color color;

    public Node(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.edges = new ArrayList<>();
        this.visited = false;
        this.distance = Double.MAX_VALUE;
        this.parent = null;
        this.color = Color.WHITE;
    }

    public Node(String name) {
        this(name, 0, 0);
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public List<Edge> getEdges() { return edges; }
    public void addEdge(Edge edge) { this.edges.add(edge); }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    
    public Node getParent() { return parent; }
    public void setParent(Node parent) { this.parent = parent; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    
    @Override
    public String toString() {
        return name + " (" + x + ", " + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return name.equals(node.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
} 
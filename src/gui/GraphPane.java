package gui;

import algorithms.SmartDijkstra;
import models.Edge;
import models.Node;
import utils.BookingSystem;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static resources.Global_variables.*;

public class GraphPane extends JPanel {
    private final List<Node> nodes;
    private List<Edge> highlightedEdges;
    private List<Edge> highlightedEdgesYellow;
    private List<Node> highlightedPath;
    private List<Edge> animationEdges;
    private List<Node> animationNodes;
    private int animationStep;
    private boolean showAnimation;
    private String animationType = ""; // "MST", "DIJKSTRA", "TSP", "MSTSD2"
    private final java.util.Map<Node, String> nodeLabels = new java.util.HashMap<>();
    
    // Zoom and pan variables
    private double zoomFactor = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean dragging = false;
    private int lastMouseX, lastMouseY;
    
    // Animation variables
    private long animationStartTime;
    private boolean showPulseEffect = false;
    
    // Smart Dijkstra animation variables
    private SmartDijkstra.PathResult smartDijkstraResult;
    private List<Node> smartDijkstraSequence;
    private int smartDijkstraStep;
    private boolean showSmartDijkstraAnimation;
    
    public GraphPane() {
        nodes = new ArrayList<>();
        highlightedEdges = new ArrayList<>();
        highlightedEdgesYellow = new ArrayList<>();
        highlightedPath = new ArrayList<>();
        animationEdges = new ArrayList<>();
        animationNodes = new ArrayList<>();
        animationStep = 0;
        showAnimation = false;
        
        // Initialize smart Dijkstra animation
        smartDijkstraStep = 0;
        showSmartDijkstraAnimation = false;
        smartDijkstraSequence = new ArrayList<>();
        
        // Start animation time
        animationStartTime = System.currentTimeMillis();
        
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(1200, 800));
        
        // Add mouse capabilities
        setupMouseListeners();
        
        // Timer for animation effects
        new javax.swing.Timer(100, e -> {
            if (showAnimation || showSmartDijkstraAnimation) {
                showPulseEffect = !showPulseEffect;
                repaint();
            }
        }).start();
    }
    
    private void setupMouseListeners() {
        // Mouse Wheel for zoom
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                double oldZoom = zoomFactor;
                if (e.getWheelRotation() < 0) {
                    zoomFactor = Math.min(zoomFactor * 1.1, 3.0); // Maximum zoom 3x
                } else {
                    zoomFactor = Math.max(zoomFactor / 1.1, 0.3); // Minimum zoom 0.3x
                }
                
                // Set offset for zooming to mouse point
                if (oldZoom != zoomFactor) {
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    offsetX = (int) (mouseX - (mouseX - offsetX) * (zoomFactor / oldZoom));
                    offsetY = (int) (mouseY - (mouseY - offsetY) * (zoomFactor / oldZoom));
                }
                
                repaint();
            }
        });
        
        // Mouse Drag for movement
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                dragging = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                dragging = false;
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (dragging) {
                    offsetX += e.getX() - lastMouseX;
                    offsetY += e.getY() - lastMouseY;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        });
    }
    
    public void addNode(Node node) {
        nodes.add(node);
        arrangeNodesInCircle();
        repaint();
    }
    
    public void addNodeDirectly(Node node) {
        // Add node without checking edges
        nodes.add(node);
    }
    
    public void clearNodes() {
        // Clear all nodes
        nodes.clear();
        clearHighlights();
        repaint();
    }
    
    public void arrangeNodes() {
        // Rearrange nodes
        arrangeNodesInCircle();
        repaint();
    }
    
    private void arrangeNodesInCircle() {
        if (nodes.isEmpty()) return;
        
        // Calculate page center
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // If page not yet measured
        if (centerX == 0 || centerY == 0) {
            centerX = 800;  // Horizontal center
            centerY = 550;  // Vertical center for bigger circle
        }
        
        // Calculate smart radius based on number of nodes
        int nodeCount = nodes.size();
        
        // Minimum required distance between two node centers (node diameter + safe distance)
        int minNodeDistance = NODE_RADIUS * 2 + 60;  // Safe distance 60 pixels
        
        // Calculate required radius for this distance
        double requiredRadius = 0;
        if (nodeCount > 1) {
            // Calculate radius from circle circumference
            double circumference = nodeCount * minNodeDistance;
            requiredRadius = circumference / (2 * Math.PI);
        }
        
        // Final radius considering page limitations
        int maxRadius = Math.min(centerX, centerY) - NODE_RADIUS - 80;  // Distance from edge
        int radius = (int) Math.max(300, Math.min(requiredRadius, maxRadius));  // Minimum radius 300
        
        System.out.println("Arranging " + nodeCount + " nodes with radius: " + radius);
        
        // Place nodes on circle
        for (int i = 0; i < nodeCount; i++) {
            double angle = 2 * Math.PI * i / nodeCount;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            
            nodes.get(i).setX(x);
            nodes.get(i).setY(y);
        }
    }
    
    public void removeNode(Node node) {
        nodes.remove(node);
        repaint();
    }
    
    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    public void setHighlightedEdges(List<Edge> edges) {
        this.highlightedEdges = new ArrayList<>(edges);
        this.highlightedPath.clear();
        repaint();
    }

    public void setHighlightedEdgesYellow(List<Edge> edges) {
        this.highlightedEdgesYellow = new ArrayList<>(edges);
        this.highlightedPath.clear();
        repaint();
    }
    
    public void setHighlightedPath(List<Node> path) {
        this.highlightedPath = new ArrayList<>(path);
        this.highlightedEdges.clear();
        this.highlightedEdgesYellow.clear();
        repaint();
    }
    
    public void clearHighlights() {
        highlightedEdges.clear();
        highlightedEdgesYellow.clear();
        highlightedPath.clear();
        animationEdges.clear();
        animationNodes.clear();
        animationStep = 0;
        showAnimation = false;
        
        // Clear smart Dijkstra animation
        smartDijkstraStep = 0;
        showSmartDijkstraAnimation = false;
        if (smartDijkstraSequence != null) {
            smartDijkstraSequence.clear();
        }
        smartDijkstraResult = null;
        
        repaint();
    }
    
    public void startAnimation(List<Edge> edges, List<Node> nodes, String type) {
        animationEdges = new ArrayList<>(edges);
        animationNodes = new ArrayList<>(nodes);
        animationStep = 0;
        showAnimation = true;
        animationType = type;
        nodeLabels.clear();
        repaint();
    }
    
    public void startAnimation(List<Edge> edges, List<Node> nodes) {
        startAnimation(edges, nodes, "");
    }
    
    public void addNodeRealTime(Node node) {
        nodes.add(node);
        arrangeNodesInCircle();
        repaint();
    }
    
    public void nextAnimationStep() {
        if (showAnimation) {
            if ("MST".equals(animationType) && animationStep < animationEdges.size()) {
                animationStep++;
            } else if (("DIJKSTRA".equals(animationType) || "TSP".equals(animationType)) && animationStep < animationNodes.size()) {
                animationStep++;
            }else if("MSTSD2".equals(animationType) && animationStep < animationEdges.size()){
                animationStep++;
            }
            repaint();
        }
    }
    
    public void resetAnimation() {
        animationStep = 0;
        showAnimation = false;
        clearHighlights();
    }
    
    public boolean isAnimationComplete() {
        if (!showAnimation) return true;
        
        if ("MST".equals(animationType) || "MSTSD2".equals(animationType)) {
            return animationStep >= animationEdges.size();
        } else if ("DIJKSTRA".equals(animationType) || "TSP".equals(animationType)) {
            return animationStep >= animationNodes.size();
        }
        
        return true;
    }
    
    // Smart Dijkstra animation methods
    public void startSmartDijkstraAnimation(SmartDijkstra.PathResult pathResult, List<Node> animationSequence) {
        this.smartDijkstraResult = pathResult;
        this.smartDijkstraSequence = new ArrayList<>(animationSequence);
        this.smartDijkstraStep = 0;
        this.showSmartDijkstraAnimation = true;
        
        // Clear other animations
        showAnimation = false;
        clearHighlights();
        
        repaint();
    }
    
    public void nextSmartDijkstraAnimationStep() {
        if (showSmartDijkstraAnimation && smartDijkstraStep < smartDijkstraSequence.size()) {
            smartDijkstraStep++;
            repaint();
        }
    }
    
    public boolean isSmartDijkstraAnimationComplete() {
        return !showSmartDijkstraAnimation || smartDijkstraStep >= smartDijkstraSequence.size();
    }
    
    public void resetSmartDijkstraAnimation() {
        smartDijkstraStep = 0;
        showSmartDijkstraAnimation = false;
        clearHighlights();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // بهبود کیفیت رندرینگ
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // اعمال زوم و پن
        g2d.translate(offsetX, offsetY);
        g2d.scale(zoomFactor, zoomFactor);
        
        // رسم یال‌ها
        drawEdges(g2d);
        
        // رسم مسیر برجسته
        drawHighlightedPath(g2d);
        
        // رسم گره‌ها
        drawNodes(g2d);
        
        // رسم برچسب‌ها
        drawLabels(g2d);
        
        g2d.dispose();
    }

    private void drawEdges(Graphics2D g2d) {
        Set<String> drawnEdges = new HashSet<>();

        for (Node node : nodes) {
            for (Edge edge : node.getEdges()) {
                String edgeKey = getEdgeKey(edge.getSource(), edge.getDestination());
                if (drawnEdges.contains(edgeKey)) continue;
                drawnEdges.add(edgeKey);

                // Determine edge color and thickness
                if (showAnimation && ("MST".equals(animationType) || "MSTSD2".equals(animationType)) && animationEdges.contains(edge)) {
                    // MST or MSTSD2 animation
                    int edgeIndex = animationEdges.indexOf(edge);
                    if (edgeIndex < animationStep) {
                        g2d.setColor(VISITED_COLOR); // Green for visited
                        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    } else if (edgeIndex == animationStep) {
                        g2d.setColor(ANIMATION_COLOR); // Orange for being examined
                        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    } else {
                        g2d.setColor(new java.awt.Color(200, 200, 200)); // Gray for not yet examined
                        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    }
                } else if (showAnimation && ("DIJKSTRA".equals(animationType) || "TSP".equals(animationType))) {
                    // Existing Dijkstra/TSP logic remains unchanged
                    boolean isInPath = false;
                    if (animationNodes.size() > 1) {
                        for (int i = 0; i < Math.min(animationStep, animationNodes.size() - 1); i++) {
                            Node source = animationNodes.get(i);
                            Node dest = animationNodes.get(i + 1);
                            if ((edge.getSource().equals(source) && edge.getDestination().equals(dest)) ||
                                    (edge.getSource().equals(dest) && edge.getDestination().equals(source))) {
                                isInPath = true;
                                break;
                            }
                        }
                    }
                    if (isInPath) {
                        g2d.setColor(VISITED_COLOR); // Green for path edges
                        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    } else {
                        g2d.setColor(EDGE_COLOR);
                        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    }
                } else if (highlightedEdges.contains(edge)) {
                    g2d.setColor(HIGHLIGHTED_EDGE_COLOR);
                    g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else if (highlightedEdgesYellow.contains(edge)) {
                    g2d.setColor(HIGHLIGHTED_EDGE_COLOR_YELLOW);
                    g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                } else {
                    g2d.setColor(EDGE_COLOR);
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }

                // Draw line with zoom and offset calculation
                int x1 = (int) ((edge.getSource().getX() + offsetX) * zoomFactor);
                int y1 = (int) ((edge.getSource().getY() + offsetY) * zoomFactor);
                int x2 = (int) ((edge.getDestination().getX() + offsetX) * zoomFactor);
                int y2 = (int) ((edge.getDestination().getY() + offsetY) * zoomFactor);

                g2d.drawLine(x1, y1, x2, y2);

                // Draw edge information
                drawEdgeInfo(g2d, edge, x1, y1, x2, y2);
            }
        }
    }
    
    private void drawEdgeInfo(Graphics2D g2d, Edge edge, int x1, int y1, int x2, int y2) {
        // محاسبه نقطه میانی برای رسم اطلاعات یال
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        boolean isHighlighted = false;
        
        // Special colors for Dijkstra animations
        if (showSmartDijkstraAnimation && smartDijkstraResult != null) {
            // Optimal path edges
            if (smartDijkstraResult.getUsedEdges().contains(edge)) {
                isHighlighted = true;
            } else if (smartDijkstraSequence != null && smartDijkstraStep > 0) {
                // Edges being examined
                Node currentNode = smartDijkstraSequence.get(Math.min(smartDijkstraStep, smartDijkstraSequence.size() - 1));
                if (edge.getSource().equals(currentNode) || edge.getDestination().equals(currentNode)) {
                    isHighlighted = true;
                }
            }
        } else if (showAnimation && "DIJKSTRA".equals(animationType)) {
            // Edges examined in regular Dijkstra
            if (animationStep > 0 && animationNodes.size() > animationStep) {
                Node currentNode = animationNodes.get(animationStep);
                if (edge.getSource().equals(currentNode) || edge.getDestination().equals(currentNode)) {
                    isHighlighted = true;
                }
            }
        }
        
        // Display edge information
        Font originalFont = g2d.getFont();
        g2d.setFont(new Font("Arial", Font.BOLD, (int)(10 * zoomFactor)));
        FontMetrics fm = g2d.getFontMetrics();
        
        String edgeInfo = "";
        java.awt.Color textColor = java.awt.Color.BLACK;
        
        if (showSmartDijkstraAnimation || (showAnimation && "DIJKSTRA".equals(animationType))) {
            // Display complete information for Dijkstra animations
            edgeInfo = String.format("%.0f T / %.1f h", edge.getCost(), edge.getTravelTime());
            textColor = isHighlighted ? java.awt.Color.WHITE : java.awt.Color.BLACK;
        } else {
            // Normal display
            edgeInfo = String.format("%.0f", edge.getWeight());
            textColor = java.awt.Color.BLACK;
        }
        
        // Draw background for text
        int textWidth = fm.stringWidth(edgeInfo);
        int textHeight = fm.getHeight();
        
        java.awt.Color bgColor = isHighlighted ? 
            new java.awt.Color(0, 0, 0, 150) : 
            new java.awt.Color(255, 255, 255, 200);
        
        g2d.setColor(bgColor);
        g2d.fillRoundRect(midX - textWidth/2 - 3, midY - textHeight/2 - 2, 
                         textWidth + 6, textHeight + 4, 4, 4);
        
        // Draw text
        g2d.setColor(textColor);
        g2d.drawString(edgeInfo, midX - textWidth/2, midY + fm.getAscent()/2);
        
        // Draw capacity information for Dijkstra animations
        if ((showSmartDijkstraAnimation || (showAnimation && "DIJKSTRA".equals(animationType))) && 
            edge.getOriginalCapacity() > 0) {
            
            BookingSystem bs = BookingSystem.getInstance();
            int available = bs.getAvailableCapacity(edge);
            int total = edge.getOriginalCapacity();
            
            String capacityInfo = String.format("%d/%d", available, total);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, (int)(9 * zoomFactor)));
            fm = g2d.getFontMetrics();
            
            int capacityWidth = fm.stringWidth(capacityInfo);
            
            // Draw capacity background
            java.awt.Color capacityBgColor = available > 0 ? 
                new java.awt.Color(40, 167, 69, 180) : 
                new java.awt.Color(220, 53, 69, 180);
            
            g2d.setColor(capacityBgColor);
            g2d.fillRoundRect(midX - capacityWidth/2 - 2, midY + textHeight/2 + 2, 
                             capacityWidth + 4, fm.getHeight() + 2, 3, 3);
            
            // Draw capacity text
            g2d.setColor(java.awt.Color.WHITE);
            g2d.drawString(capacityInfo, midX - capacityWidth/2, 
                          midY + textHeight/2 + fm.getAscent() + 3);
        }
        
        g2d.setFont(originalFont);
    }
    
    private void drawHighlightedPath(Graphics2D g2d) {
        if (highlightedPath.size() < 2) return;
        
        // Draw a highlighted path with animation
        g2d.setColor(PATH_COLOR);
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < highlightedPath.size() - 1; i++) {
            Node current = highlightedPath.get(i);
            Node next = highlightedPath.get(i + 1);
            
            int x1 = (int) ((current.getX() + offsetX) * zoomFactor);
            int y1 = (int) ((current.getY() + offsetY) * zoomFactor);
            int x2 = (int) ((next.getX() + offsetX) * zoomFactor);
            int y2 = (int) ((next.getY() + offsetY) * zoomFactor);
            
            // Draw shadow for line
            g2d.setColor(new java.awt.Color(0, 0, 0, 100));
            g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1 + 2, y1 + 2, x2 + 2, y2 + 2);
            
            // Draw the main line
            g2d.setColor(PATH_COLOR);
            g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
            
            // Draw better arrow
            drawArrow(g2d, x1, y1, x2, y2);
        }
    }
    
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        int arrowLength = 15;
        double arrowAngle = Math.PI / 5;
        
        // Arrow end point
        int endX = (int) (x2 - NODE_RADIUS * Math.cos(angle));
        int endY = (int) (y2 - NODE_RADIUS * Math.sin(angle));
        
        // Arrow points
        int x3 = (int) (endX - arrowLength * Math.cos(angle - arrowAngle));
        int y3 = (int) (endY - arrowLength * Math.sin(angle - arrowAngle));
        int x4 = (int) (endX - arrowLength * Math.cos(angle + arrowAngle));
        int y4 = (int) (endY - arrowLength * Math.sin(angle + arrowAngle));
        
        // Draw arrow with more thickness
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(endX, endY, x3, y3);
        g2d.drawLine(endX, endY, x4, y4);
        
        // Draw arrow triangle
        int[] xPoints = {endX, x3, x4};
        int[] yPoints = {endY, y3, y4};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void drawDirectionalArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // Calculate line angle
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        // Calculate midpoint
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        
        // Arrow size
        int arrowLength = 8;
        double arrowAngle = Math.PI / 4;
        
        // Arrow points
        int x3 = (int) (midX - arrowLength * Math.cos(angle - arrowAngle));
        int y3 = (int) (midY - arrowLength * Math.sin(angle - arrowAngle));
        int x4 = (int) (midX - arrowLength * Math.cos(angle + arrowAngle));
        int y4 = (int) (midY - arrowLength * Math.sin(angle + arrowAngle));
        
        // Draw arrow
        g2d.drawLine(midX, midY, x3, y3);
        g2d.drawLine(midX, midY, x4, y4);
    }
    
    private void drawNodes(Graphics2D g2d) {
        for (Node node : nodes) {
            int x = (int) ((node.getX() + offsetX) * zoomFactor);
            int y = (int) ((node.getY() + offsetY) * zoomFactor);
            int radius = (int) (NODE_RADIUS * zoomFactor);
            
            // Determine node color based on animation state
            java.awt.Color nodeColor = NODE_COLOR;
            java.awt.Color borderColor = java.awt.Color.DARK_GRAY;
            int borderWidth = 2;
            boolean isCurrentNode = false;
            boolean isStartNode = false;
            boolean isDestinationNode = false;
            
            // Smart Dijkstra animation colors
            if (showSmartDijkstraAnimation && smartDijkstraSequence != null) {
                int nodeIndex = smartDijkstraSequence.indexOf(node);
                
                // Check start and destination nodes
                if (smartDijkstraResult != null && !smartDijkstraResult.getPath().isEmpty()) {
                    List<Node> path = smartDijkstraResult.getPath();
                    if (node.equals(path.getFirst())) {
                        // Start node - dark blue
                        nodeColor = SMART_DIJKSTRA_START_COLOR;
                        borderColor = new java.awt.Color(10, 90, 200);
                        borderWidth = 5;
                        isStartNode = true;
                    } else if (node.equals(path.getLast())) {
                        // Destination node - pink red
                        nodeColor = SMART_DIJKSTRA_DESTINATION_COLOR;
                        borderColor = new java.awt.Color(180, 40, 110);
                        borderWidth = 5;
                        isDestinationNode = true;
                    }
                }
                
                // Colors based on animation state
                if (!isStartNode && !isDestinationNode && nodeIndex != -1) {
                    if (nodeIndex < smartDijkstraStep) {
                        // Visited node - dark green
                        nodeColor = SMART_DIJKSTRA_VISITED_COLOR;
                        borderColor = new java.awt.Color(20, 110, 70);
                        borderWidth = 3;
                    } else if (nodeIndex == smartDijkstraStep) {
                        // Node being examined - orange
                        nodeColor = SMART_DIJKSTRA_CURRENT_COLOR;
                        borderColor = new java.awt.Color(220, 120, 0);
                        borderWidth = 4;
                        isCurrentNode = true;
                    } else {
                        // Node not yet visited - light gray
                        nodeColor = SMART_DIJKSTRA_UNVISITED_COLOR;
                        borderColor = new java.awt.Color(140, 150, 160);
                        borderWidth = 2;
                    }
                                  }
              } else {
                  // Regular animations (Dijkstra, TSP, MST, etc.)
                  if (showAnimation && "DIJKSTRA".equals(animationType)) {
                      if (animationNodes.contains(node)) {
                          int nodeIndex = animationNodes.indexOf(node);
                          if (nodeIndex < animationStep) {
                              // Visited node - green
                              nodeColor = DIJKSTRA_VISITED_COLOR;
                              borderColor = new java.awt.Color(30, 140, 60);
                              borderWidth = 3;
                          } else if (nodeIndex == animationStep) {
                              // Current node - golden yellow
                              nodeColor = DIJKSTRA_CURRENT_COLOR;
                              borderColor = new java.awt.Color(200, 160, 0);
                              borderWidth = 4;
                              isCurrentNode = true;
                          } else {
                              // Node in queue - light yellow
                              nodeColor = DIJKSTRA_CANDIDATE_COLOR;
                              borderColor = new java.awt.Color(200, 170, 60);
                              borderWidth = 2;
                          }
                      } else {
                          // Unvisited node - gray
                          nodeColor = DIJKSTRA_UNVISITED_COLOR;
                          borderColor = new java.awt.Color(90, 100, 110);
                          borderWidth = 2;
                      }
                } else if (showAnimation && "TSP".equals(animationType)) {
                    if (animationNodes.contains(node)) {
                        int nodeIndex = animationNodes.indexOf(node);
                        if (nodeIndex < animationStep) {
                            nodeColor = VISITED_COLOR;
                            borderColor = new java.awt.Color(0, 150, 0);
                            borderWidth = 3;
                        } else if (nodeIndex == animationStep) {
                            nodeColor = ANIMATION_COLOR;
                            borderColor = new java.awt.Color(200, 50, 0);
                            borderWidth = 4;
                            isCurrentNode = true;
                        }
                    }
                } else if (showAnimation && "MST".equals(animationType)) {
                    if (animationNodes.contains(node)) {
                        int nodeIndex = animationNodes.indexOf(node);
                        if (nodeIndex < animationStep) {
                            nodeColor = VISITED_COLOR;
                            borderColor = new java.awt.Color(0, 150, 0);
                            borderWidth = 3;
                        } else if (nodeIndex == animationStep) {
                            nodeColor = ANIMATION_COLOR;
                            borderColor = new java.awt.Color(200, 50, 0);
                            borderWidth = 4;
                            isCurrentNode = true;
                        }
                    }
                } else if (highlightedPath.contains(node)) {
                    nodeColor = HIGHLIGHTED_NODE_COLOR;
                    borderColor = new java.awt.Color(200, 150, 0);
                    borderWidth = 3;
                }
                          }
              
              // Draw shadow for important nodes
              if (isStartNode || isDestinationNode || isCurrentNode) {
                  g2d.setColor(new java.awt.Color(0, 0, 0, 40));
                  g2d.fillOval(x - radius + 3, y - radius + 3, radius * 2, radius * 2);
              }
              
              // Draw main node
              g2d.setColor(nodeColor);
              g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
              
              // Draw gradient for 3D effect
              if (isCurrentNode || isStartNode || isDestinationNode) {
                  GradientPaint gradient = new GradientPaint(
                      x - radius/2, y - radius/2, 
                      brighter(nodeColor, 0.3f),
                      x + radius/2, y + radius/2, 
                      nodeColor
                  );
                  g2d.setPaint(gradient);
                  g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                  g2d.setPaint(null);
              }
              
              // Draw border
              g2d.setColor(borderColor);
              g2d.setStroke(new BasicStroke(borderWidth));
              g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
              
              // Animation effects for current node
              if (isCurrentNode) {
                  // Draw glowing circle around current node
                  if (showPulseEffect) {
                      g2d.setColor(ANIMATION_GLOW_COLOR);
                      g2d.fillOval(x - radius - 10, y - radius - 10, (radius + 10) * 2, (radius + 10) * 2);
                  }
                  
                  // Draw pulsing ring
                  g2d.setColor(showPulseEffect ? PULSE_COLOR : new java.awt.Color(255, 255, 255, 100));
                  g2d.setStroke(new BasicStroke(3));
                  g2d.drawOval(x - radius - 8, y - radius - 8, (radius + 8) * 2, (radius + 8) * 2);
              }
              
              // Special effects for start and destination nodes
              if (isStartNode || isDestinationNode) {
                  // Draw outer circle for important nodes
                  g2d.setColor(new java.awt.Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 80));
                  g2d.setStroke(new BasicStroke(2));
                  g2d.drawOval(x - radius - 6, y - radius - 6, (radius + 6) * 2, (radius + 6) * 2);
                  
                  // Center point for emphasis
                  g2d.setColor(new java.awt.Color(255, 255, 255, 200));
                  g2d.fillOval(x - 3, y - 3, 6, 6);
              }
        }
        
        // رسم لیبل‌ها
        drawLabels(g2d);
    }
    
    private java.awt.Color brighter(java.awt.Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new java.awt.Color(r, g, b);
    }
    
    private void drawLabels(Graphics2D g2d) {
        Font originalFont = g2d.getFont();
        g2d.setFont(new Font("Arial", Font.BOLD, (int)(12 * zoomFactor)));
        FontMetrics fm = g2d.getFontMetrics();
        
        for (Node node : nodes) {
            int x = (int) ((node.getX() + offsetX) * zoomFactor);
            int y = (int) ((node.getY() + offsetY) * zoomFactor);
            int radius = (int) (NODE_RADIUS * zoomFactor);
            
            String nodeName = node.getName();
            String[] lines = splitTextToFitInCircle(nodeName, fm, (int)(radius * 1.5));
            
            // Add smart Dijkstra animation information
            if (showSmartDijkstraAnimation && smartDijkstraSequence != null) {
                int nodeIndex = smartDijkstraSequence.indexOf(node);
                if (nodeIndex != -1 && nodeIndex <= smartDijkstraStep) {
                    // Display distance calculated by algorithm
                    String distanceInfo;
                    if (node.getDistance() == Double.MAX_VALUE) {
                        distanceInfo = "∞";
                    } else if (node.getDistance() == 0) {
                        distanceInfo = "Start";
                    } else {
                        distanceInfo = String.format("%.1f", node.getDistance());
                    }
                    
                    // Add status information
                    String statusInfo = "";
                    if (nodeIndex < smartDijkstraStep) {
                        statusInfo = "✓"; // Visited
                    } else if (nodeIndex == smartDijkstraStep) {
                        statusInfo = "→"; // Being examined
                    }
                    
                    // Combine information
                    String[] newLines = new String[lines.length + 2];
                    System.arraycopy(lines, 0, newLines, 0, lines.length);
                    newLines[lines.length] = distanceInfo;
                    newLines[lines.length + 1] = statusInfo;
                    lines = newLines;
                }
                
                // Display special information for source and destination nodes
                if (smartDijkstraResult != null) {
                    List<Node> path = smartDijkstraResult.getPath();
                    if (!path.isEmpty()) {
                        if (node.equals(path.get(0))) {
                            String[] newLines = new String[lines.length + 1];
                            System.arraycopy(lines, 0, newLines, 0, lines.length);
                            newLines[lines.length] = "START";
                            lines = newLines;
                        } else if (node.equals(path.get(path.size() - 1))) {
                            String[] newLines = new String[lines.length + 1];
                            System.arraycopy(lines, 0, newLines, 0, lines.length);
                            newLines[lines.length] = "DEST";
                            lines = newLines;
                        }
                    }
                }
            } else {
                // Other animations (TSP, etc.)
                if (showAnimation && "TSP".equals(animationType)) {
                    if (animationNodes.contains(node)) {
                        int nodeIndex = animationNodes.indexOf(node);
                        if (nodeIndex < animationStep) {
                            String visitInfo = "✓" + (nodeIndex + 1);
                            String[] newLines = new String[lines.length + 1];
                            System.arraycopy(lines, 0, newLines, 0, lines.length);
                            newLines[lines.length] = visitInfo;
                            lines = newLines;
                        } else if (nodeIndex == animationStep) {
                            String currentInfo = "→";
                            String[] newLines = new String[lines.length + 1];
                            System.arraycopy(lines, 0, newLines, 0, lines.length);
                            newLines[lines.length] = currentInfo;
                            lines = newLines;
                        }
                    }
                } else if (showAnimation && "MST".equals(animationType)) {
                    if (animationNodes.contains(node)) {
                        int nodeIndex = animationNodes.indexOf(node);
                        if (nodeIndex < animationStep) {
                            String mstInfo = "●";
                            String[] newLines = new String[lines.length + 1];
                            System.arraycopy(lines, 0, newLines, 0, lines.length);
                            newLines[lines.length] = mstInfo;
                            lines = newLines;
                        }
                    }
                }
            }

            // Calculate starting position for text
            int totalHeight = lines.length * fm.getHeight();
            int startY = y - totalHeight / 2 + fm.getAscent();
            
            // Draw each line
            g2d.setColor(java.awt.Color.BLACK);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int lineWidth = fm.stringWidth(line);
                int lineX = x - lineWidth / 2;
                int lineY = startY + i * fm.getHeight();
                
                // Draw white background for better readability
                if (showSmartDijkstraAnimation || showAnimation) {
                    g2d.setColor(new java.awt.Color(255, 255, 255, 200));
                    g2d.fillRoundRect(lineX - 2, lineY - fm.getAscent(), 
                                     lineWidth + 4, fm.getHeight(), 4, 4);
                }
                
                // Select text color based on information type
                if (i >= lines.length - 2 && showSmartDijkstraAnimation) {
                    if (i == lines.length - 2) {
                        // Distance color
                        g2d.setColor(new java.awt.Color(0, 0, 139)); // Dark blue
                    } else {
                        // Status color
                        int nodeIndex = smartDijkstraSequence.indexOf(node);
                        if (nodeIndex == smartDijkstraStep) {
                            g2d.setColor(new java.awt.Color(255, 69, 0)); // Orange
                        } else {
                            g2d.setColor(new java.awt.Color(0, 128, 0)); // Green
                        }
                    }
                } else {
                    g2d.setColor(java.awt.Color.BLACK);
                }
                
                g2d.drawString(line, lineX, lineY);
            }
        }
        
        // Display general smart Dijkstra animation information
        if (showSmartDijkstraAnimation && smartDijkstraResult != null) {
            drawSmartDijkstraInfo(g2d);
        }
        
        // Display general regular Dijkstra animation information - removed
        
        g2d.setFont(originalFont);
    }
    
    private void drawSmartDijkstraInfo(Graphics2D g2d) {
        // Display general algorithm information in top-right corner
        Font titleFont = new Font("Arial", Font.BOLD, 16);
        Font infoFont = new Font("Arial", Font.BOLD, 12);
        Font legendFont = new Font("Arial", Font.PLAIN, 11);
        
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();
        
        String[] infoLines = {
            "🔍 Smart Dijkstra Algorithm",
            "Progress: " + smartDijkstraStep + "/" + smartDijkstraSequence.size(),
            "",
            "🎯 Legend:",
            "🟦 Start Node",
            "🟠 Current Node", 
            "🟢 Visited Nodes",
            "🟥 Destination",
            "⚫ Unvisited Nodes"
        };
        
        // Calculate additional information
        String progressInfo = "";
        if (smartDijkstraResult != null) {
            progressInfo = String.format("💰 Cost: %.0f T | ⏱️ Time: %.1f h", 
                                      smartDijkstraResult.getTotalCost(), 
                                      smartDijkstraResult.getTotalTime());
        }
        
        int panelWidth = 280;
        int panelHeight = (infoLines.length + 2) * fm.getHeight() + 30;
        int panelX = getWidth() - panelWidth - 10;
        int panelY = 10;
        
        // Draw panel background with transparency
        g2d.setColor(new java.awt.Color(20, 20, 20, 200));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Draw gradient border
        g2d.setColor(new java.awt.Color(100, 149, 237));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Draw text
        int currentY = panelY + 25;
        for (int i = 0; i < infoLines.length; i++) {
            String line = infoLines[i];
            if (line.isEmpty()) {
                currentY += fm.getHeight() / 2;
                continue;
            }
            
            if (i == 0) {
                // Title
                g2d.setColor(new java.awt.Color(100, 149, 237));
                g2d.setFont(titleFont);
                fm = g2d.getFontMetrics();
            } else if (i == 1) {
                // Progress
                g2d.setColor(new java.awt.Color(255, 193, 7));
                g2d.setFont(infoFont);
                fm = g2d.getFontMetrics();
            } else if (line.startsWith("🎯")) {
                // Legend header
                g2d.setColor(new java.awt.Color(255, 255, 255));
                g2d.setFont(infoFont);
                fm = g2d.getFontMetrics();
            } else if (line.startsWith("🟦") || line.startsWith("🟠") || line.startsWith("🟢") || 
                      line.startsWith("🟥") || line.startsWith("⚫")) {
                // Legend items
                g2d.setColor(new java.awt.Color(220, 220, 220));
                g2d.setFont(legendFont);
                fm = g2d.getFontMetrics();
            } else {
                g2d.setColor(new java.awt.Color(200, 200, 200));
                g2d.setFont(infoFont);
                fm = g2d.getFontMetrics();
            }
            
            g2d.drawString(line, panelX + 15, currentY);
            currentY += fm.getHeight() + 2;
        }
        
        // Add general information
        if (!progressInfo.isEmpty()) {
            g2d.setColor(new java.awt.Color(144, 238, 144));
            g2d.setFont(legendFont);
            fm = g2d.getFontMetrics();
            g2d.drawString(progressInfo, panelX + 15, currentY + 5);
        }
    }
    
    // تابع drawDijkstraInfo حذف شده - کادر اطلاعاتی Dijkstra عادی دیگر نمایش داده نمی‌شود
    
    private String[] splitTextToFitInCircle(String text, FontMetrics fm, int maxWidth) {
        // اگر متن در یک خط جا می‌شود
        if (fm.stringWidth(text) <= maxWidth) {
            return new String[]{text};
        }
        
        // Split into words
        String[] words = text.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            
            if (fm.stringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, shorten it
                    lines.add(word.substring(0, Math.min(word.length(), 12)) + "...");
                }
            }
        }
        
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }

    
    private String getEdgeKey(Node source, Node destination) {
        String key1 = source.getName() + "-" + destination.getName();
        String key2 = destination.getName() + "-" + source.getName();
        return key1.compareTo(key2) < 0 ? key1 : key2;
    }
} 
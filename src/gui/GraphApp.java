package gui;

import algorithms.*;
import models.Edge;
import models.Node;
import models.Reservation;
import models.Student;
import utils.BookingSystem;
import utils.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class GraphApp extends JFrame {
    private GraphPane graphPane;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JSlider speedSlider;
    private JTextArea statusArea;
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> startNodeCombo;
    private JComboBox<String> endNodeCombo;
    private javax.swing.Timer animationTimer;
    private int animationStep = 0;
    private boolean isAnimating = false;
    private InputHandler inputHandler;
    private static GraphApp instance; // For console access

    // Student management components
    private JPanel reservationPanel;
    private JList<String> reservationList;
    private DefaultListModel<String> reservationListModel;
    private JButton addStudentButton;
    private JButton viewReservationsButton;
    private JButton manageStudentsButton;
    private BookingSystem bookingSystem;

    public GraphApp() {
        setTitle("Graph Visualizer - Transport System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE
        setSize(1800, 1200);  // Much larger size for bigger circle and more space
        setLocationRelativeTo(null);

        // Set static instance
        instance = this;

        // Initialize booking system
        bookingSystem = BookingSystem.getInstance();

        initializeComponents();
        layoutComponents();
        setupListeners();

        // Set WindowListener to clear instance
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                instance = null;
                System.out.println("Graphics window closed. Console continues running...");
            }
        });
    }

    // Method for updating graph from console
    public static void updateFromConsole(List<Node> nodes) {
        if (instance != null) {
            SwingUtilities.invokeLater(() -> {
                instance.updateGraphFromConsole(nodes);
            });
        }
    }

    private void updateGraphFromConsole(List<Node> nodes) {
        graphPane.clearNodes();
        for (Node node : nodes) {
            graphPane.addNodeDirectly(node);
        }
        graphPane.arrangeNodes();
        updateNodeCombos();
        updateReservationList();
        updateStatus("Graph updated from console - " + nodes.size() + " universities loaded");
    }

    private void initializeComponents() {
        graphPane = new GraphPane();
        startButton = new JButton("Start Algorithm");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");
        speedSlider = new JSlider(100, 1100, 600);
        statusArea = new JTextArea(5, 30);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        algorithmCombo = new JComboBox<>(new String[]{
            "(MST)",
            "(Dijkstra)",
            "(TSP)",
                "(MST+SD2)",
        });

        startNodeCombo = new JComboBox<>();
        endNodeCombo = new JComboBox<>();

        pauseButton.setEnabled(false);

        // Speed slider settings
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

         //Speed labels
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(100, new JLabel("Fast"));
        labelTable.put(600, new JLabel("Medium"));
        labelTable.put(1100, new JLabel("Slow"));
        speedSlider.setLabelTable(labelTable);

        // Student management components
        addStudentButton = new JButton("Add Student");
        viewReservationsButton = new JButton("View All Reservations");
        manageStudentsButton = new JButton("Manage Students");

        reservationListModel = new DefaultListModel<>();
        reservationList = new JList<>(reservationListModel);
        reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationList.setVisibleRowCount(8);

        reservationPanel = new JPanel();
        reservationPanel.setLayout(new BorderLayout());
        reservationPanel.setBorder(BorderFactory.createTitledBorder("Active Reservations"));
        reservationPanel.add(new JScrollPane(reservationList), BorderLayout.CENTER);

        // Reservation panel buttons
        JPanel reservationButtonPanel = new JPanel();
        reservationButtonPanel.setLayout(new FlowLayout());
        JButton cancelReservationButton = new JButton("Cancel");
        cancelReservationButton.setBackground(new java.awt.Color(244, 67, 54));
        cancelReservationButton.setForeground(java.awt.Color.WHITE);
        cancelReservationButton.setOpaque(true);
        cancelReservationButton.setBorderPainted(false);

        JButton confirmReservationButton = new JButton("Confirm");
        confirmReservationButton.setBackground(new java.awt.Color(76, 175, 80));
        confirmReservationButton.setForeground(java.awt.Color.WHITE);
        confirmReservationButton.setOpaque(true);
        confirmReservationButton.setBorderPainted(false);

        reservationButtonPanel.add(cancelReservationButton);
        reservationButtonPanel.add(confirmReservationButton);
        reservationPanel.add(reservationButtonPanel, BorderLayout.SOUTH);

        // Add listeners for reservation buttons
        cancelReservationButton.addActionListener(e -> cancelSelectedReservation());
        confirmReservationButton.addActionListener(e -> confirmSelectedReservation());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algorithmCombo);
        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(startNodeCombo);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(endNodeCombo);

        // Style main control buttons
        startButton.setBackground(new java.awt.Color(76, 175, 80));
        startButton.setForeground(java.awt.Color.WHITE);
        startButton.setOpaque(true);
        startButton.setBorderPainted(false);
        controlPanel.add(startButton);
        
        pauseButton.setBackground(new java.awt.Color(255, 193, 7));
        pauseButton.setForeground(java.awt.Color.BLACK);
        pauseButton.setOpaque(true);
        pauseButton.setBorderPainted(false);
        controlPanel.add(pauseButton);
        
        resetButton.setBackground(new java.awt.Color(158, 158, 158));
        resetButton.setForeground(java.awt.Color.WHITE);
        resetButton.setOpaque(true);
        resetButton.setBorderPainted(false);
        controlPanel.add(resetButton);

        // Speed panel
        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        speedPanel.add(new JLabel("Speed (ms):"));
        speedPanel.add(speedSlider);

        // Action buttons panel (colored buttons)
        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add university button
        JButton addUniversityButton = new JButton("Add University");
        addUniversityButton.addActionListener(e -> addNewUniversity());
        addUniversityButton.setBackground(new java.awt.Color(46, 125, 50));
        addUniversityButton.setForeground(java.awt.Color.WHITE);
        addUniversityButton.setOpaque(true);
        addUniversityButton.setBorderPainted(false);
        actionButtonsPanel.add(addUniversityButton);

        // Add route button
        JButton addRouteButton = new JButton("Add Route");
        addRouteButton.addActionListener(e -> addNewRoute());
        addRouteButton.setBackground(new java.awt.Color(2, 136, 209));
        addRouteButton.setForeground(java.awt.Color.WHITE);
        addRouteButton.setOpaque(true);
        addRouteButton.setBorderPainted(false);
        actionButtonsPanel.add(addRouteButton);

        // Remove university button
        JButton removeUniversityButton = new JButton("Remove University");
        removeUniversityButton.addActionListener(e -> removeUniversity());
        removeUniversityButton.setBackground(new java.awt.Color(244, 67, 54));
        removeUniversityButton.setForeground(java.awt.Color.WHITE);
        removeUniversityButton.setOpaque(true);
        removeUniversityButton.setBorderPainted(false);
        actionButtonsPanel.add(removeUniversityButton);

        // TSP buttons
        JButton planTourButton = new JButton("Plan University Tour");
        planTourButton.addActionListener(e -> planUniversityTour());
        planTourButton.setBackground(new java.awt.Color(156, 39, 176));
        planTourButton.setForeground(java.awt.Color.WHITE);
        planTourButton.setOpaque(true);
        planTourButton.setBorderPainted(false);
        actionButtonsPanel.add(planTourButton);

        JButton showMatrixButton = new JButton("Show Cost Matrix");
        showMatrixButton.addActionListener(e -> showCostMatrix());
        showMatrixButton.setBackground(new java.awt.Color(103, 58, 183));
        showMatrixButton.setForeground(java.awt.Color.WHITE);
        showMatrixButton.setOpaque(true);
        showMatrixButton.setBorderPainted(false);
        actionButtonsPanel.add(showMatrixButton);

        // Student management buttons
        addStudentButton.setBackground(new java.awt.Color(33, 150, 243));
        addStudentButton.setForeground(java.awt.Color.WHITE);
        addStudentButton.setOpaque(true);
        addStudentButton.setBorderPainted(false);
        actionButtonsPanel.add(addStudentButton);

        viewReservationsButton.setBackground(new java.awt.Color(76, 175, 80));
        viewReservationsButton.setForeground(java.awt.Color.WHITE);
        viewReservationsButton.setOpaque(true);
        viewReservationsButton.setBorderPainted(false);
        actionButtonsPanel.add(viewReservationsButton);

        manageStudentsButton.setBackground(new java.awt.Color(255, 152, 0));
        manageStudentsButton.setForeground(java.awt.Color.WHITE);
        manageStudentsButton.setOpaque(true);
        manageStudentsButton.setBorderPainted(false);
        actionButtonsPanel.add(manageStudentsButton);

        // Top panel with vertical layout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(controlPanel);
        topPanel.add(speedPanel);
        topPanel.add(actionButtonsPanel);  // Action buttons added below speed panel

        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(new JLabel("Status:"), BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Right panel for reservations
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.add(reservationPanel, BorderLayout.CENTER);

        // Main layout
        add(topPanel, BorderLayout.NORTH);
        add(graphPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAlgorithm();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseAlgorithm();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAlgorithm();
            }
        });

        algorithmCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNodeCombos();
            }
        });

        speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                if (animationTimer != null) {
                    animationTimer.setDelay(speedSlider.getValue());
                }
            }
        });

        // Student management listeners
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewStudent();
            }
        });

        viewReservationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewAllReservations();
            }
        });

        manageStudentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageStudents();
            }
        });

        // Update reservation list when clicking on it
        reservationList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedReservationDetails();
                }
            }
        });
    }

    private void updateNodeCombos() {
        startNodeCombo.removeAllItems();
        endNodeCombo.removeAllItems();
        for (Node node : graphPane.getNodes()) {
            String displayName = node.getName().length() > 10 ? node.getName().substring(0,7) + "..." : node.getName();
            startNodeCombo.addItem(displayName);
            endNodeCombo.addItem(displayName);
        }
        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
        assert selectedAlgorithm != null;
        boolean needsEndNode = selectedAlgorithm.contains("Shortest Path");
        endNodeCombo.setEnabled(needsEndNode);
    }

    private void startAlgorithm() {
        if (isAnimating) return;

        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
        String startNodeName = (String) startNodeCombo.getSelectedItem();
        String endNodeName = (String) endNodeCombo.getSelectedItem();

        if (startNodeName == null) {
            updateStatus("Please select a start node.");
            return;
        }

        Node startNode = findNodeByName(startNodeName);
        Node endNode = findNodeByName(endNodeName);

        resetAlgorithm();
        isAnimating = true;
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);

        if (selectedAlgorithm.contains("MST")) {
            runMSTAlgorithm();
        } else if (selectedAlgorithm.contains("Shortest Path")) {
            if (endNode == null) {
                updateStatus("Please select an end node for shortest path.");
                finishAnimation();
                return;
            }
            runDijkstraAlgorithm(startNode, endNode);
        } else if (selectedAlgorithm.contains("TSP")) {
            runTSPAlgorithm(startNode);
        }
    }

    private void runMSTAlgorithm() {
        updateStatus("Running Minimum Spanning Tree algorithm...");
        List<Edge> mstEdges = Kruskal.findMST(graphPane.getNodes());
        double totalCost = Kruskal.calculateMSTCost(mstEdges);

        // Start step-by-step animation
        graphPane.startAnimation(mstEdges, new ArrayList<>(), "MST");
        updateStatus("Starting MST animation - watching edges being added...");

        // Set timer for animation
        if (animationTimer != null) {
            animationTimer.stop();
        }

        final int[] stepCount = {0};
        animationTimer = new javax.swing.Timer(speedSlider.getValue(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphPane.isAnimationComplete()) {
                    graphPane.setHighlightedEdges(mstEdges);
                    updateStatus("MST completed. Total cost: " + String.format("%.2f", totalCost) + " km");
                    finishAnimation();
                } else {
                    stepCount[0]++;
                    updateStatus("MST Step " + stepCount[0] + " - Adding edge to spanning tree...");
                    graphPane.nextAnimationStep();
                }
            }
        });

        animationTimer.start();
    }
    private void runMSTandSD2Algorithm() {
        updateStatus("Running Minimum Spanning Tree algorithm with SD2...");
        List<Edge> mstEdges = Kruskal.findMST(graphPane.getNodes());
        List<Edge> sD2Edges = SD2.findSD2Edges(mstEdges, graphPane.getNodes());
        double totalCost = Kruskal.calculateMSTCost(mstEdges);

        // Start step-by-step animation
        graphPane.startAnimation(mstEdges, new ArrayList<>(), "MST");
        updateStatus("Starting MST animation - watching edges being added...");

        // Set timer for animation
        if (animationTimer != null) {
            animationTimer.stop();
        }

        final int[] stepCount = {0};
        animationTimer = new javax.swing.Timer(speedSlider.getValue(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphPane.isAnimationComplete()) {
                    graphPane.setHighlightedEdges(mstEdges);
                    updateStatus("MST completed. Total cost: " + String.format("%.2f", totalCost) + " km");
                    finishAnimation();
                } else {
                    stepCount[0]++;
                    updateStatus("MST Step " + stepCount[0] + " - Adding edge to spanning tree...");
                    graphPane.nextAnimationStep();
                }
            }
        });

        animationTimer.start();
    }

    private void runDijkstraAlgorithm(Node start, Node end) {
        updateStatus("Running Dijkstra's shortest path algorithm...");

        // Find the shortest path with step display
        List<Node> allNodes = graphPane.getNodes();
        List<Node> visitedOrder = new ArrayList<>();

        // Initial settings
        for (Node node : allNodes) {
            node.setDistance(Double.MAX_VALUE);
            node.setVisited(false);
            node.setParent(null);
        }

        start.setDistance(0);

        // Simulate algorithm to record visit order
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(Node::getDistance));
        pq.offer(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.isVisited()) continue;
            current.setVisited(true);
            visitedOrder.add(current);

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

        // Build final path
        List<Node> finalPath = new ArrayList<>();
        Node current = end;
        while (current != null) {
            finalPath.add(0, current);
            current = current.getParent();
        }

        if (!finalPath.isEmpty() && finalPath.get(0).equals(start)) {
            // Start step-by-step animation
            graphPane.startAnimation(new ArrayList<>(), visitedOrder, "DIJKSTRA");
            updateStatus("Starting Dijkstra animation from " + start.getName() + " to " + end.getName() + "...");

            // Set timer for animation
            if (animationTimer != null) {
                animationTimer.stop();
            }

            final int[] stepCount = {0};
            animationTimer = new javax.swing.Timer(speedSlider.getValue(), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (stepCount[0] >= visitedOrder.size()) {
                        graphPane.setHighlightedPath(finalPath);
                        updateStatus("Shortest path found: " + getPathString(finalPath) +
                                    " (Distance: " + String.format("%.2f", end.getDistance()) + " km)");
                        finishAnimation();
                    } else {
                        Node currentNode = visitedOrder.get(stepCount[0]);
                        updateStatus("Dijkstra Step " + (stepCount[0] + 1) + " - Visiting: " +
                                    currentNode.getName() + " (Distance: " + String.format("%.2f", currentNode.getDistance()) + ")");
                        stepCount[0]++;
                        graphPane.nextAnimationStep();
                    }
                }
            });

            animationTimer.start();
        } else {
            updateStatus("No path found between selected nodes.");
            finishAnimation();
        }
    }

    private void runTSPAlgorithm(Node start) {
        updateStatus("Running Traveling Salesman Problem algorithm...");

        // Use advanced TSP with optimal method selection
        TSP.TSPResult result = TSP.solveTSPWithBitmasking(graphPane.getNodes(), start);
        List<Node> visitOrder = result.getVisitedOrder();
        double totalCost = result.getTotalCost();
        double totalTime = result.getTotalTime();

        // Initial settings for animation
        for (Node node : graphPane.getNodes()) {
            node.setVisited(false);
        }

        if (!visitOrder.isEmpty()) {
            // Start step-by-step animation
            graphPane.startAnimation(new ArrayList<>(), visitOrder, "TSP");
            updateStatus("Starting TSP animation - finding optimal tour from " + start.getName() + "...");

            // Set timer for animation
            if (animationTimer != null) {
                animationTimer.stop();
            }

            final double finalCost = totalCost;
            final double finalTime = totalTime;
            final int[] stepCount = {0};
            animationTimer = new javax.swing.Timer(speedSlider.getValue(), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (stepCount[0] >= visitOrder.size()) {
                        graphPane.setHighlightedPath(visitOrder);
                        updateStatus("TSP tour completed: " + getPathString(visitOrder) +
                                    " (Total cost: " + String.format("%.2f", finalCost) + " km, " +
                                    "Total time: " + String.format("%.2f", finalTime) + " hours)");
                        finishAnimation();
                    } else {
                        Node currentNode = visitOrder.get(stepCount[0]);
                        updateStatus("TSP Step " + (stepCount[0] + 1) + " - Traveling to: " +
                                    currentNode.getName() + " (Progress: " + (stepCount[0] + 1) + "/" + visitOrder.size() + ")");
                        stepCount[0]++;
                        graphPane.nextAnimationStep();
                    }
                }
            });

            animationTimer.start();
        } else {
            updateStatus("No TSP tour found.");
            finishAnimation();
        }
    }

    private double calculateDistance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void pauseAlgorithm() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        isAnimating = false;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        updateStatus("Algorithm paused.");
    }

    private void resetAlgorithm() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        isAnimating = false;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        graphPane.resetAnimation();
        updateStatus("Algorithm reset.");
    }

    private void finishAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        isAnimating = false;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }

    private Node findNodeByName(String name) {
        for (Node node : graphPane.getNodes()) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    private String getPathString(List<Node> path) {
        if (path.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    private void updateStatus(String message) {
        statusArea.append(message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void addNewUniversity() {
        String name = JOptionPane.showInputDialog(this, "Enter university name:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        // Check for duplicate names
        for (Node node : graphPane.getNodes()) {
            if (node.getName().equals(name.trim())) {
                JOptionPane.showMessageDialog(this, "University with this name already exists!");
                return;
            }
        }

        // Create new node
        Node newNode = new Node(name.trim());

        // Add to graph
        graphPane.addNodeRealTime(newNode);
        updateNodeCombos();
        updateStatus("Added new university: " + name.trim());

        // Save to file
        InputHandler.saveNodeToFile(newNode, "transport_data.txt");
    }

    private void addNewRoute() {
        List<Node> nodes = graphPane.getNodes();
        if (nodes.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 universities to create a route!");
            return;
        }

        // Select source university
        String[] nodeNames = nodes.stream().map(Node::getName).toArray(String[]::new);
        String sourceName = (String) JOptionPane.showInputDialog(this, "Select source university:",
                                                                "Source", JOptionPane.QUESTION_MESSAGE,
                                                                null, nodeNames, nodeNames[0]);
        if (sourceName == null) return;

        // Select destination university
        String destName = (String) JOptionPane.showInputDialog(this, "Select destination university:",
                                                              "Destination", JOptionPane.QUESTION_MESSAGE,
                                                              null, nodeNames, nodeNames[0]);
        if (destName == null || destName.equals(sourceName)) {
            JOptionPane.showMessageDialog(this, "Please select a different destination!");
            return;
        }

        // Get route information
        String distanceStr = JOptionPane.showInputDialog(this, "Enter distance (km):");
        if (distanceStr == null) return;

        String costStr = JOptionPane.showInputDialog(this, "Enter cost (Tomans):");
        if (costStr == null) return;

        String capacityStr = JOptionPane.showInputDialog(this, "Enter bus capacity:");
        if (capacityStr == null) return;

        String operatingHours = JOptionPane.showInputDialog(this, "Enter operating hours (e.g., 6:00-22:00):");
        if (operatingHours == null) return;

        try {
            double distance = Double.parseDouble(distanceStr);
            double cost = Double.parseDouble(costStr);
            int capacity = Integer.parseInt(capacityStr);

            // Find nodes
            Node source = nodes.stream().filter(n -> n.getName().equals(sourceName)).findFirst().orElse(null);
            Node dest = nodes.stream().filter(n -> n.getName().equals(destName)).findFirst().orElse(null);

            if (source != null && dest != null) {
                // Create bidirectional edges
                Edge edge1 = new Edge(source, dest, distance, cost, capacity, operatingHours);
                Edge edge2 = new Edge(dest, source, distance, cost, capacity, operatingHours);

                source.addEdge(edge1);
                dest.addEdge(edge2);

                // Save to file
                InputHandler.saveDataToFile("transport_data.txt", graphPane.getNodes());

                updateStatus("Added new route: " + sourceName + " <-> " + destName);
                graphPane.repaint();

                // If animating, add new route
                updateActiveAnimation();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
        }
    }

    private void removeUniversity() {
        List<Node> nodes = graphPane.getNodes();
        if (nodes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No universities to remove!");
            return;
        }

        String[] nodeNames = nodes.stream().map(Node::getName).toArray(String[]::new);
        String selectedName = (String) JOptionPane.showInputDialog(this, "Select university to remove:",
                                                                   "Remove University", JOptionPane.QUESTION_MESSAGE,
                                                                   null, nodeNames, nodeNames[0]);
        if (selectedName == null) return;

        Node nodeToRemove = nodes.stream().filter(n -> n.getName().equals(selectedName)).findFirst().orElse(null);
        if (nodeToRemove != null) {
            // Remove edges related to this node from other nodes
            for (Node node : nodes) {
                node.getEdges().removeIf(edge -> edge.getDestination().equals(nodeToRemove));
            }

            // Remove node from graph
            graphPane.removeNode(nodeToRemove);
            graphPane.arrangeNodes();

            // Save to file
//            InputHandler.saveDataToFile("transport_data.txt", graphPane.getNodes());

            updateNodeCombos();
            updateStatus("Removed university: " + selectedName);

            // If animating, add new route
            updateActiveAnimation();
        }
    }

    private void updateActiveAnimation() {
        if (isAnimating) {
            String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
            String startNodeName = (String) startNodeCombo.getSelectedItem();
            String endNodeName = (String) endNodeCombo.getSelectedItem();

            if (startNodeName != null) {
                Node startNode = findNodeByName(startNodeName);
                Node endNode = findNodeByName(endNodeName);

                if (startNode != null) {
                    // Stop current animation
                    if (animationTimer != null && animationTimer.isRunning()) {
                        animationTimer.stop();
                    }

                    // Recalculate and start new animation
                    if (selectedAlgorithm.contains("MST")) {
                        runMSTAlgorithm();
                    } else if (selectedAlgorithm.contains("Shortest Path") && endNode != null) {
                        runDijkstraAlgorithm(startNode, endNode);
                    } else if (selectedAlgorithm.contains("TSP")) {
                        runTSPAlgorithm(startNode);
                    }

                    updateStatus("Algorithm restarted with new route data...");
                }
            }
        } else {
            // If animation is not running, only update final result
            updateStaticResults();
        }
    }

    // New method for updating results without animation
    private void updateStaticResults() {
        String selectedAlgorithm = (String) algorithmCombo.getSelectedItem();
        String startNodeName = (String) startNodeCombo.getSelectedItem();
        String endNodeName = (String) endNodeCombo.getSelectedItem();

        if (startNodeName != null) {
            Node startNode = findNodeByName(startNodeName);
            Node endNode = findNodeByName(endNodeName);

            if (startNode != null) {
                if (selectedAlgorithm.contains("MST")) {
                    // Check graph connectivity before calculating MST
                    List<Node> nodes = graphPane.getNodes();
                    if (nodes.size() < 2) {
                        updateStatus("MST: Need at least 2 nodes to compute MST");
                        return;
                    }

                    List<Edge> mstEdges = Kruskal.findMST(nodes);
                    double totalCost = Kruskal.calculateMSTCost(mstEdges);

                    // Check if MST is completely calculated
                    if (mstEdges.size() == nodes.size() - 1) {
                        graphPane.setHighlightedEdges(mstEdges);
                        updateStatus("MST updated with new data. Total cost: " + String.format("%.2f", totalCost) + " km - " + mstEdges.size() + " edges");
                    } else {
                        updateStatus("Warning: Graph is not fully connected. MST incomplete - only " + mstEdges.size() + " edges found");
                        graphPane.setHighlightedEdges(mstEdges); // Display connected components
                    }
                } else if (selectedAlgorithm.contains("Shortest Path") && endNode != null) {
                    List<Node> shortestPath = Dijkstra.findShortestPath(graphPane.getNodes(), startNode, endNode);
                    if (!shortestPath.isEmpty()) {
                        graphPane.setHighlightedPath(shortestPath);
                        updateStatus("Shortest path updated: " + getPathString(shortestPath) +
                                    " (Distance: " + String.format("%.2f", endNode.getDistance()) + " km)");
                    }
                } else if (selectedAlgorithm.contains("TSP")) {
                    TSP.TSPResult result = TSP.solveTSPWithBitmasking(graphPane.getNodes(), startNode);
                    if (!result.getPath().isEmpty()) {
                        graphPane.setHighlightedPath(result.getPath());
                        updateStatus("TSP tour updated: " + getPathString(result.getPath()) +
                                    " (Total cost: " + String.format("%.2f", result.getTotalCost()) + " km)");
                    }
                }
            }
        }
    }

    public GraphPane getGraphPane() {
        return graphPane;
    }

    // Student management methods
    private void updateReservationList() {
        reservationListModel.clear();
        List<Reservation> activeReservations = bookingSystem.getActiveReservations();

        for (Reservation reservation : activeReservations) {
            String displayText = String.format("[%s] %s - %s → %s (%.0f T) - %s",
                reservation.getReservationId().substring(0, 4),
                reservation.getStudent().getName(),
                reservation.getRoute().getSource().getName(),
                reservation.getRoute().getDestination().getName(),
                reservation.getTotalCost(),
                reservation.getStatusString()
            );
            reservationListModel.addElement(displayText);
        }

        if (activeReservations.isEmpty()) {
            reservationListModel.addElement("No active reservations");
        }
    }

    private void cancelSelectedReservation() {
        int selectedIndex = reservationList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.");
            return;
        }

        List<Reservation> activeReservations = bookingSystem.getActiveReservations();
        if (selectedIndex < activeReservations.size()) {
            Reservation reservation = activeReservations.get(selectedIndex);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this reservation?\n" + reservation.toString(),
                "Cancel Reservation",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (bookingSystem.cancelReservation(reservation.getReservationId())) {
                    updateReservationList();
                    updateStatus("Reservation cancelled: " + reservation.getReservationId());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel reservation.");
                }
            }
        }
    }

    private void confirmSelectedReservation() {
        int selectedIndex = reservationList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to confirm.");
            return;
        }

        List<Reservation> activeReservations = bookingSystem.getActiveReservations();
        if (selectedIndex < activeReservations.size()) {
            Reservation reservation = activeReservations.get(selectedIndex);

            if (bookingSystem.confirmReservation(reservation.getReservationId())) {
                updateReservationList();
                updateStatus("Reservation confirmed: " + reservation.getReservationId());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to confirm reservation.");
            }
        }
    }

    // Student management action methods
    private void addNewStudent() {
        List<Node> nodes = graphPane.getNodes();
        if (nodes.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 universities to add a student!");
            return;
        }

        // Student information dialog
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField budgetField = new JTextField("100000");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Max Budget (T):"));
        panel.add(budgetField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String budgetStr = budgetField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
                return;
            }

            try {
                double budget = Double.parseDouble(budgetStr);
                Student student = new Student(name, phone, email);
                student.setMaxBudget(budget);

                bookingSystem.addStudent(student);
                updateStatus("Student added: " + name + " (ID: " + student.getId() + ")");

                // Ask if they want to make a reservation
                int makeReservation = JOptionPane.showConfirmDialog(this,
                    "Would you like to make a reservation for this student?",
                    "Make Reservation", JOptionPane.YES_NO_OPTION);

                if (makeReservation == JOptionPane.YES_OPTION) {
                    makeReservationForStudent(student);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid budget amount!");
            }
        }
    }

    private void makeReservationForStudent(Student student) {
        List<Node> nodes = graphPane.getNodes();
        String[] nodeNames = nodes.stream().map(Node::getName).toArray(String[]::new);

        // Select source
        String sourceName = (String) JOptionPane.showInputDialog(this, "Select departure university:",
                                                                "Departure", JOptionPane.QUESTION_MESSAGE,
                                                                null, nodeNames, nodeNames[0]);
        if (sourceName == null) return;

        // Select destination
        String destName = (String) JOptionPane.showInputDialog(this, "Select destination university:",
                                                              "Destination", JOptionPane.QUESTION_MESSAGE,
                                                              null, nodeNames, nodeNames[0]);
        if (destName == null || destName.equals(sourceName)) {
            JOptionPane.showMessageDialog(this, "Please select a different destination!");
            return;
        }

        // Find nodes
        Node source = nodes.stream().filter(n -> n.getName().equals(sourceName)).findFirst().orElse(null);
        Node dest = nodes.stream().filter(n -> n.getName().equals(destName)).findFirst().orElse(null);

        if (source == null || dest == null) {
            JOptionPane.showMessageDialog(this, "Could not find the selected universities!");
            return;
        }

        // Set student travel details
        student.setCurrentLocation(source);
        student.setDestination(dest);

        // Ask for optimization preference
        String[] optimizationOptions = {
            "Balanced (Cost + Time + Distance)",
            "Cheapest Route (Minimize Cost)",
            "Fastest Route (Minimize Time)",
            "Shortest Route (Minimize Distance)",
            "Show All Alternatives"
        };

        String selectedOption = (String) JOptionPane.showInputDialog(this,
            "Choose optimization preference:", "Route Optimization",
            JOptionPane.QUESTION_MESSAGE, null, optimizationOptions, optimizationOptions[0]);

        if (selectedOption == null) return;

        // Show animated path finding
        int animationChoice = JOptionPane.showConfirmDialog(this,
            "Would you like to see the animated path-finding process?",
            "Animation", JOptionPane.YES_NO_OPTION);

        SmartDijkstra.OptimizationWeights weights;
        List<SmartDijkstra.PathResult> pathResults = new ArrayList<>();

        if (selectedOption.contains("Show All")) {
            // Find all alternative paths
            pathResults = SmartDijkstra.findAlternativePaths(nodes, source, dest, student, 4);
        } else {
            // Choose weights based on preference
            switch (selectedOption) {
                case "Cheapest Route (Minimize Cost)":
                    weights = new SmartDijkstra.OptimizationWeights(0.8, 0.1, 0.1);
                    break;
                case "Fastest Route (Minimize Time)":
                    weights = new SmartDijkstra.OptimizationWeights(0.1, 0.8, 0.1);
                    break;
                case "Shortest Route (Minimize Distance)":
                    weights = new SmartDijkstra.OptimizationWeights(0.1, 0.1, 0.8);
                    break;
                default: // Balanced
                    weights = new SmartDijkstra.OptimizationWeights(0.4, 0.4, 0.2);
                    break;
            }

            if (animationChoice == JOptionPane.YES_OPTION) {
                // Use animation callback
                List<Node> animationSequence = new ArrayList<>();
                SmartDijkstra.PathResult result = SmartDijkstra.findOptimalPath(
                    nodes, source, dest, student, weights, animationSequence);

                if (result.isSuccessful()) {
                    // Start animated visualization
                    startSmartDijkstraAnimation(result, animationSequence);
                    pathResults.add(result);
                } else {
                    JOptionPane.showMessageDialog(this, "No valid path found: " + result.getFailureReason());
                    return;
                }
            } else {
                // Direct calculation
                SmartDijkstra.PathResult result = SmartDijkstra.findOptimalPath(
                    nodes, source, dest, student, weights);
                pathResults.add(result);
            }
        }

        if (pathResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No valid paths found!");
            return;
        }

        // Show path selection dialog
        SmartDijkstra.PathResult selectedPath = selectPathFromResults(pathResults);
        if (selectedPath == null) return;

        // Make reservation with the selected path
        if (selectedPath.getUsedEdges().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid path selected!");
            return;
        }

        BookingSystem.ReservationResult result = bookingSystem.makePathReservation(
            student, selectedPath.getUsedEdges(), LocalDateTime.now().plusHours(1));

        if (result.isSuccess()) {
            updateReservationList();

            // Highlight the complete path
            graphPane.setHighlightedPath(selectedPath.getPath());

            String message = String.format(
                "Reservation successful!\n\nReservation ID: %s\n\nSelected Path:\n%s\n\nDetailed Information:\n%s",
                result.getReservation().getReservationId(),
                selectedPath.getPathSummary(),
                SmartDijkstra.getDetailedPathInfo(selectedPath)
            );

            updateStatus("Smart reservation: " + selectedPath.getPathSummary());

            JTextArea textArea = new JTextArea(message);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "Reservation Successful", JOptionPane.INFORMATION_MESSAGE);

        } else {
            // Show error with alternative routes if available
            StringBuilder errorMsg = new StringBuilder(result.getMessage());
            List<Edge> alternatives = result.getAlternativeRoutes();
            
            if (alternatives != null && !alternatives.isEmpty()) {
                errorMsg.append("\n\nAlternative routes available:\n");
                for (Edge alt : alternatives) {
                    errorMsg.append(String.format("\n%s → %s (Cost: %.0f T, Time: %.1f h)", 
                        alt.getSource().getName(), 
                        alt.getDestination().getName(),
                        alt.getCost(),
                        alt.getTravelTime()));
                }
            }
            
            JOptionPane.showMessageDialog(this, errorMsg.toString(), "Reservation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SmartDijkstra.PathResult selectPathFromResults(List<SmartDijkstra.PathResult> pathResults) {
        if (pathResults.size() == 1) {
            return pathResults.get(0);
        }

        // Create selection dialog
        String[] pathOptions = pathResults.stream()
            .map(path -> String.format("Cost: %.0f T, Time: %.1f h, Distance: %.1f km - %s",
                path.getTotalCost(), path.getTotalTime(), path.getTotalDistance(),
                path.getPath().stream().map(Node::getName).reduce((a, b) -> a + "→" + b).orElse("")))
            .toArray(String[]::new);

        String selectedOption = (String) JOptionPane.showInputDialog(this,
            "Select your preferred route:", "Route Selection",
            JOptionPane.QUESTION_MESSAGE, null, pathOptions, pathOptions[0]);

        if (selectedOption == null) return null;

        int selectedIndex = Arrays.asList(pathOptions).indexOf(selectedOption);
        SmartDijkstra.PathResult selectedPath = pathResults.get(selectedIndex);

        // Show detailed information
        String detailedInfo = SmartDijkstra.getDetailedPathInfo(selectedPath);

        JTextArea textArea = new JTextArea(detailedInfo);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        int confirm = JOptionPane.showConfirmDialog(this, scrollPane,
            "Route Details - Confirm Selection", JOptionPane.OK_CANCEL_OPTION);

        return (confirm == JOptionPane.OK_OPTION) ? selectedPath : null;
    }

    private void startSmartDijkstraAnimation(SmartDijkstra.PathResult pathResult, List<Node> animationSequence) {
        // Clear previous highlights
        graphPane.clearHighlights();

        updateStatus("Starting Smart Dijkstra animation...");

        // Set animation for GraphPane
        graphPane.startSmartDijkstraAnimation(pathResult, animationSequence);

        // Setup animation timer
        if (animationTimer != null) {
            animationTimer.stop();
        }

        final int[] stepCount = {0};
        animationTimer = new javax.swing.Timer(speedSlider.getValue(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphPane.isSmartDijkstraAnimationComplete()) {
                    // Animation finished
                    graphPane.setHighlightedPath(pathResult.getPath());
                    updateStatus(String.format("Smart Dijkstra completed: %s", pathResult.getPathSummary()));
                    finishAnimation();
                } else {
                    stepCount[0]++;
                    updateStatus(String.format("Smart Dijkstra Step %d - Exploring nodes...", stepCount[0]));
                    graphPane.nextSmartDijkstraAnimationStep();
                }
            }
        });

        animationTimer.start();

        // Update button states
        isAnimating = true;
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }

    private void viewAllReservations() {
        List<Reservation> allReservations = bookingSystem.getReservations();
        if (allReservations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No reservations found.");
            return;
        }

        StringBuilder sb = new StringBuilder("All Reservations:\n\n");
        for (Reservation reservation : allReservations) {
            sb.append(String.format("ID: %s\n", reservation.getReservationId()));
            sb.append(String.format("Student: %s\n", reservation.getStudent().getName()));
            sb.append(String.format("Route: %s\n", reservation.getRouteString()));
            sb.append(String.format("Cost: %.0f T\n", reservation.getTotalCost()));
            sb.append(String.format("Status: %s\n", reservation.getStatusString()));
            sb.append(String.format("Booking Time: %s\n", reservation.getFormattedBookingTime()));
            sb.append("---\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "All Reservations", JOptionPane.INFORMATION_MESSAGE);
    }

    private void manageStudents() {
        List<Student> students = bookingSystem.getStudents();
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found.");
            return;
        }

        String[] studentNames = students.stream()
            .map(s -> s.getName() + " (" + s.getId() + ")")
            .toArray(String[]::new);

        String selectedStudent = (String) JOptionPane.showInputDialog(this, "Select student to manage:",
                                                                     "Manage Students", JOptionPane.QUESTION_MESSAGE,
                                                                     null, studentNames, studentNames[0]);
        if (selectedStudent == null) return;

        int studentIndex = Arrays.asList(studentNames).indexOf(selectedStudent);
        Student student = students.get(studentIndex);

        // Show student details and options
        String[] options = {"View Details", "View Reservations", "Remove Student"};
        int choice = JOptionPane.showOptionDialog(this, "What would you like to do?", "Student Management",
                                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                null, options, options[0]);

        switch (choice) {
            case 0: // View Details
                showStudentDetails(student);
                break;
            case 1: // View Reservations
                showStudentReservations(student);
                break;
            case 2: // Remove Student
                removeStudent(student);
                break;
        }
    }

    private void showStudentDetails(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Student ID: %s\n", student.getId()));
        sb.append(String.format("Name: %s\n", student.getName()));
        sb.append(String.format("Phone: %s\n", student.getPhoneNumber()));
        sb.append(String.format("Email: %s\n", student.getEmail()));
        sb.append(String.format("Max Budget: %.0f T\n", student.getMaxBudget()));
        sb.append(String.format("Current Location: %s\n",
                  student.getCurrentLocation() != null ? student.getCurrentLocation().getName() : "Not set"));
        sb.append(String.format("Destination: %s\n",
                  student.getDestination() != null ? student.getDestination().getName() : "Not set"));
        sb.append(String.format("Active: %s\n", student.isActive() ? "Yes" : "No"));

        JOptionPane.showMessageDialog(this, sb.toString(), "Student Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStudentReservations(Student student) {
        List<Reservation> reservations = bookingSystem.getReservationsForStudent(student);
        if (reservations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No reservations found for this student.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Reservations for %s:\n\n", student.getName()));

        for (Reservation reservation : reservations) {
            sb.append(String.format("ID: %s\n", reservation.getReservationId()));
            sb.append(String.format("Route: %s\n", reservation.getRouteString()));
            sb.append(String.format("Cost: %.0f T\n", reservation.getTotalCost()));
            sb.append(String.format("Status: %s\n", reservation.getStatusString()));
            sb.append(String.format("Travel Time: %s\n", reservation.getFormattedTravelTime()));
            sb.append("---\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Student Reservations", JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeStudent(Student student) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove this student?\nThis will cancel all their reservations.",
            "Remove Student",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            bookingSystem.removeStudent(student);
            updateReservationList();
            updateStatus("Student removed: " + student.getName());
        }
    }

    private void viewSelectedReservationDetails() {
        String selected = reservationList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation first.");
            return;
        }

        // Extract reservation ID from the list item
        String reservationId = selected.split(" - ")[0];

        // Find the reservation
        for (Reservation reservation : bookingSystem.getReservations()) {
            if (reservation.getReservationId().equals(reservationId)) {
                String details = String.format(
                    "Reservation Details:\n\n" +
                    "ID: %s\n" +
                    "Student: %s\n" +
                    "Phone: %s\n" +
                    "Route: %s → %s\n" +
                    "Booking Time: %s\n" +
                    "Travel Time: %s\n" +
                    "Status: %s\n" +
                    "Cost: %.0f T",
                    reservation.getReservationId(),
                    reservation.getStudent().getName(),
                    reservation.getStudent().getPhoneNumber(),
                    reservation.getRoute().getSource().getName(),
                    reservation.getRoute().getDestination().getName(),
                    reservation.getBookingTime().toString(),
                    reservation.getTravelTime().toString(),
                    reservation.getStatus().toString(),
                    reservation.getTotalCost()
                );

                JTextArea textArea = new JTextArea(details);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));

                JOptionPane.showMessageDialog(this, scrollPane, "Reservation Details", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
        }
    }

    // TSP Methods
    private void planUniversityTour() {
        List<Node> universities = graphPane.getNodes();
        if (universities.size() < 2) {
            JOptionPane.showMessageDialog(this, "You need at least 2 universities to plan a tour!");
            return;
        }

        // Select universities for tour
        List<Node> selectedUniversities = selectUniversitiesForTour(universities);
        if (selectedUniversities.size() < 2) {
            return; // User canceled or didn't select enough universities
        }

        // Select starting university
        String[] uniNames = selectedUniversities.stream().map(Node::getName).toArray(String[]::new);
        String startUniName = (String) JOptionPane.showInputDialog(this,
            "Select starting university:", "Tour Start Point",
            JOptionPane.QUESTION_MESSAGE, null, uniNames, uniNames[0]);

        if (startUniName == null) return;

        Node startUni = selectedUniversities.stream()
            .filter(n -> n.getName().equals(startUniName))
            .findFirst().orElse(selectedUniversities.get(0));

        // Select algorithm type
        String[] algorithms = {"Smart TSP (Bitmasking)", "Simple TSP (Nearest Neighbor)"};
        String algorithm = (String) JOptionPane.showInputDialog(this,
            "Choose TSP algorithm:", "Algorithm Selection",
            JOptionPane.QUESTION_MESSAGE, null, algorithms, algorithms[0]);

        if (algorithm == null) return;

        // Solve TSP problem
        TSP.TSPResult result;
        if (algorithm.contains("Smart")) {
            result = TSP.solveTSPWithBitmasking(selectedUniversities, startUni);
        } else {
            result = TSP.solveTSPSimple(selectedUniversities, startUni);
        }

        // Display results
        if (result.getPath().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not find a valid tour route!");
            return;
        }

        // Display path on graph
        graphPane.setHighlightedPath(result.getPath());

        // Display details
        String details = String.format(
            "University Tour Results\n\n" +
            "Algorithm: %s\n" +
            "Universities visited: %d\n" +
            "Total cost: %.0f T\n" +
            "Total time: %.1f hours\n\n" +
            "Route:\n%s\n\n" +
            "Details:\n%s",
            algorithm,
            selectedUniversities.size(),
            result.getTotalCost(),
            result.getTotalTime(),
            result.getRouteDescription(),
            result.getDetails()
        );

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "University Tour Plan", JOptionPane.INFORMATION_MESSAGE);

        updateStatus(String.format("TSP tour planned: %.0f T, %.1f hours, %d universities",
                                  result.getTotalCost(), result.getTotalTime(), selectedUniversities.size()));
    }

    private List<Node> selectUniversitiesForTour(List<Node> allUniversities) {
        // Limit to 10 universities to avoid complexity
        List<Node> availableUniversities = allUniversities.size() > 10 ?
            allUniversities.subList(0, 10) : allUniversities;

        // Create multiple selection dialog
        JDialog dialog = new JDialog(this, "Select Universities for Tour", true);
        dialog.setLayout(new BorderLayout());

        // Checkbox list
        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();
        for (Node uni : availableUniversities) {
            JCheckBox checkBox = new JCheckBox(uni.getName());
            listModel.addElement(checkBox);
        }

        JList<JCheckBox> checkBoxList = new JList<>(listModel);
        checkBoxList.setCellRenderer(new GuiHelper.CheckBoxListCellRenderer());
        checkBoxList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = checkBoxList.locationToIndex(evt.getPoint());
                if (index != -1) {
                    JCheckBox checkBox = listModel.getElementAt(index);
                    checkBox.setSelected(!checkBox.isSelected());
                    checkBoxList.repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(checkBoxList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        JPanel buttonPanel = new JPanel();
        JButton selectAllButton = new JButton("Select All");
        JButton clearAllButton = new JButton("Clear All");
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        final boolean[] dialogResult = {false};

        selectAllButton.addActionListener(e -> {
            for (int i = 0; i < listModel.size(); i++) {
                listModel.getElementAt(i).setSelected(true);
            }
            checkBoxList.repaint();
        });

        clearAllButton.addActionListener(e -> {
            for (int i = 0; i < listModel.size(); i++) {
                listModel.getElementAt(i).setSelected(false);
            }
            checkBoxList.repaint();
        });

        okButton.addActionListener(e -> {
            dialogResult[0] = true;
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(selectAllButton);
        buttonPanel.add(clearAllButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel("Select universities for the tour (max 10):"), BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Collect results
        List<Node> selectedUniversities = new ArrayList<>();
        if (dialogResult[0]) {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.getElementAt(i).isSelected()) {
                    selectedUniversities.add(availableUniversities.get(i));
                }
            }
        }

        return selectedUniversities;
    }

    private void showCostMatrix() {
        List<Node> universities = graphPane.getNodes();
        if (universities.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No universities found! Please add universities first.");
            return;
        }

        if (universities.size() > 10) {
            universities = universities.subList(0, 10);
        }

        String matrixText = TSP.showCostMatrix(universities);

        JTextArea textArea = new JTextArea(matrixText);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Cost Matrix", JOptionPane.INFORMATION_MESSAGE);
    }
}
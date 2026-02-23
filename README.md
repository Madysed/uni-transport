# University Transport System (Graph Algorithms + Booking + Visualizer)

A Java-based university transportation simulator that models universities as nodes and routes as weighted edges, then applies classic graph algorithms to analyze and optimize travel. The project includes both a console interface for managing data and a Swing GUI visualizer for interactive exploration, algorithm animation, and reservation management.

---

## Key Features

### Graph Modeling
- Universities represented as nodes (with coordinates for visualization)
- Routes represented as weighted edges including:
  - Distance/weight
  - Cost
  - Capacity
  - Operating hours

### Implemented Algorithms
Located in `src/algorithms`:

- BFS (Breadth-First Search)
- Dijkstra (Shortest Path)
- SmartDijkstra (multi-constraint shortest path with cost/capacity awareness)
- Kruskal (Minimum Spanning Tree)
- TSP (Traveling Salesman Problem)

### GUI (Swing-Based)
- Interactive graph visualization
- Node selection (start/end)
- Algorithm animation
- Speed control
- Output/status panel
- Reservation & student management interface

### Console Interface
From `Main.java`:
- Add universities
- Add routes
- Load graph from file
- Display graph data
- Save graph to file
- Launch GUI visualizer

---

## Project Structure

```
src/
  Main.java
  algorithms/       # BFS, Dijkstra, SmartDijkstra, Kruskal, TSP
  gui/              # Swing visualizer components
  models/           # Node, Edge, Student, Reservation
  utils/            # BookingSystem, InputHandler, helpers
transport_data.txt  # Sample dataset
```

---

## Data File Format

The system loads/saves graphs in a structured format.

### Universities
```
# Universities
Name,x,y
```

### Routes
```
# Routes
Source,Destination,Weight,Cost,Capacity,OperatingHours
```

Example:
```
# Universities
A,678.0,43.0
B,978.0,343.0

# Routes
A,B,10.0,5.0,20,06:00-22:00
```

Routes are treated as bidirectional when loaded.

---

## How to Run

### IntelliJ IDEA (Recommended)
1. Open project
2. Set JDK (Java 8+)
3. Run `Main.java`

### Command Line

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```

---

## Learning Objectives

This project demonstrates:

- Graph data structures
- Pathfinding algorithms
- Minimum spanning tree
- Optimization problems (TSP)
- Capacity-aware routing
- Java Swing GUI development
- File I/O handling
- Basic booking/reservation system design

package algorithms;

public class UnionFind {
    private int[] parent;
    private int[] rank;
    private int[] size;
    private int components;
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        size = new int[n];
        components = n;
        
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }
    
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }
    
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        
        if (rootX == rootY) return false;
        
        // Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
            size[rootY] += size[rootX];
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
            size[rootX] += size[rootY];
        } else {
            parent[rootY] = rootX;
            size[rootX] += size[rootY];
            rank[rootX]++;
        }
        
        components--;
        return true;
    }
    
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }
    
    public int getSize(int x) {
        return size[find(x)];
    }
    
    public int getComponents() {
        return components;
    }
    
    public boolean isConnected() {
        return components == 1;
    }
    
    // Return all connected components
    public java.util.List<java.util.List<Integer>> getConnectedComponents() {
        java.util.Map<Integer, java.util.List<Integer>> components = new java.util.HashMap<>();
        
        for (int i = 0; i < parent.length; i++) {
            int root = find(i);
            if (!components.containsKey(root)) {
                components.put(root, new java.util.ArrayList<>());
            }
            components.get(root).add(i);
        }
        
        return new java.util.ArrayList<>(components.values());
    }
} 
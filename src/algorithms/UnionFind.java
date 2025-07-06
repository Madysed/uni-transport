package algorithms;


/**
 * Disjoint Set Union (Union-Find) data structure with path compression and union by rank.
 * Efficiently supports union and find operations to determine the connectivity of elements.
 *
 * @timeComplexity All operations are nearly constant time, amortized O(α(n)), where α is the inverse Ackermann function.
 * @spaceComplexity O(n) for maintaining parent and rank arrays.
 */
public class UnionFind {
    private final int[] parent;
    private final int[] rank;

    /**
     * Initializes the Union-Find structure for n elements, each in its own set.
     *
     * @param n The number of elements
     */
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i; //self parents at start
            rank[i] = 0;
        }
    }

    /**
     * Finds the representative (root) of the set that element x belongs to.
     * Applies path compression to flatten the tree structure.
     *
     * @param x The element to find
     * @return The representative of the set
     *
     * @timeComplexity Amortized O(α(n)), where α is the inverse Ackermann function
     * @spaceComplexity O(1)
     */
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }

    /**
     * Unites the sets containing elements x and y using union by rank.
     *
     * @param x First element
     * @param y Second element
     * @return True if the sets were united, false if x and y were already in the same set
     *
     * @timeComplexity Amortized O(α(n))
     * @spaceComplexity O(1)
     */
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        
        if (rootX == rootY) return false; // union exists
        
        // if not --> Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        return true;
    }
} 
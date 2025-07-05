package models;

public enum Color {
    RED(255, 0, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    YELLOW(255, 255, 0),
    PURPLE(128, 0, 128),
    ORANGE(255, 165, 0),
    CYAN(0, 255, 255),
    PINK(255, 192, 203),
    BLACK(0, 0, 0),
    WHITE(255, 255, 255),
    GRAY(128, 128, 128),
    LIGHT_BLUE(173, 216, 230);
    
    private final int r, g, b;
    
    Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }
    
    public java.awt.Color toAwtColor() {
        return new java.awt.Color(r, g, b);
    }
} 
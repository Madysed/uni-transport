package utils;

import models.Node;

public class EdgeUtils {
    public static String createEdgeKey(Node source, Node destination) {
        String name1 = source.getName();
        String name2 = destination.getName();

        //sort names
        if (name1.compareTo(name2) < 0) { //0 if equall, <0 if less
            return name1 + "-" + name2;
        } else {
            return name2 + "-" + name1;
        }
    }
}

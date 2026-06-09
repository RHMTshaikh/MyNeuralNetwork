package Visualisation;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Point {
    public static void main(String[] args) {
        
    }
    public IntegerProperty x;
    public IntegerProperty y;

    public Point(int x, int y) {
        this.x = new SimpleIntegerProperty(x);
        this.y = new SimpleIntegerProperty(y);
    }
}



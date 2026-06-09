package Visualisation;

import java.util.HashMap;
import java.util.Map;

import TheBrain.Neuron;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Visual_Neuron {
    public DoubleProperty excitationLevel = new SimpleDoubleProperty();
    public DoubleProperty thresholdExcitationLevel = new SimpleDoubleProperty();
    public Circle threshold_circle;
    public Circle excitation_circle;
    public Point center;
    public Color color;
    // public Map<VisualNeuron, VisualLink> linksIn = new HashMap<>();
    public Map<Visual_Neuron, Visual_Link> linksOut = new HashMap<>();

    public Visual_Neuron(Neuron neuron) {
        this.excitationLevel.set(neuron.excitation_level);;
        this.thresholdExcitationLevel.set(neuron.threshold_excitation_level);
        int f = 11;
        threshold_circle = new Circle(neuron.threshold_excitation_level, Color.TRANSPARENT);
        threshold_circle.radiusProperty().bind(thresholdExcitationLevel.multiply(f));
        threshold_circle.setStroke(Color.RED);
        
        excitation_circle = new Circle(neuron.excitation_level, Color.GREEN);
        excitation_circle.radiusProperty().bind(excitationLevel.multiply(f));
        excitation_circle.setOpacity(0.5);
    }
    
    public void set_center(int x, int y) {
        center = new Point(x, y);
        threshold_circle.centerXProperty().bind(center.x);
        threshold_circle.centerYProperty().bind(center.y);

        excitation_circle.centerXProperty().bind(center.x);
        excitation_circle.centerYProperty().bind(center.y);
    }

    public void increaseExcitationLevel(double value) {
        excitationLevel.set(excitationLevel.get() + value);
    }
    
}

package Visualisation;

import TheBrain.Link;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Line;

public class Visual_Link {
    public Visual_Neuron from;
    public Visual_Neuron to;
    public DoubleProperty weight = new SimpleDoubleProperty();
    public Shape curve;
    
    public Visual_Link(Link link, int clock) {
        this.from = link.from.visual_neuron;
        this.to = link.to.visual_neuron;
        this.weight.set(Math.abs(link.get_weight(clock)));
        
        if (from.center.x.get() == to.center.x.get()) {
            curve = createQuadCurve(from.center, to.center);
        } else {
            curve = createCubicCurve(from.center, to.center);
            // curve = createLine(from.center, to.center, this.weight);
        }
        curve.strokeWidthProperty().bind(weight.multiply(1));
        curve.setOpacity(0.5);
        curve.setStroke(Color.hsb(Math.random() * 360, 1.0, 1.0));
    }


    public static CubicCurve createCubicCurve(Point startPoint, Point endPoint) {
        CubicCurve curve = new CubicCurve();
        setStartPoint(curve, startPoint);
        setEndPoint(curve, endPoint);
        setControls(curve, startPoint, endPoint);
        curve.setFill(Color.TRANSPARENT);
        return curve;
    }
    static void setStartPoint(CubicCurve curve,Point startPoint){
        curve.setStartX(startPoint.x.doubleValue());
        curve.setStartY(startPoint.y.doubleValue());
    }
    static void setEndPoint(CubicCurve curve,Point endPoint){
        curve.setEndX(endPoint.x.doubleValue());
        curve.setEndY(endPoint.y.doubleValue());
    }
    static void setControls(CubicCurve curve, Point startPoint, Point endPoint){
        double midX = (startPoint.x.doubleValue()+endPoint.x.doubleValue())/2;
        double f = 1.5;
        double startX = (startPoint.x.doubleValue()-midX)/f;
        double endX = (endPoint.x.doubleValue()-midX)/f;
        curve.setControlX1(endX + midX);
        curve.setControlY1(startPoint.y.doubleValue());
        curve.setControlX2(startX + midX);
        curve.setControlY2(endPoint.y.doubleValue());
    }
    
    public static QuadCurve createQuadCurve(Point startPoint, Point endPoint) {
        QuadCurve curve = new QuadCurve();
        setStartPoint(curve, startPoint);
        setEndPoint(curve, endPoint);
        setControls(curve, startPoint, endPoint);
        curve.setFill(Color.TRANSPARENT);
        return curve;
    }
    static void setStartPoint(QuadCurve curve,Point startPoint){
        curve.setStartX(startPoint.x.doubleValue());
        curve.setStartY(startPoint.y.doubleValue());
    }
    static void setEndPoint(QuadCurve curve,Point endPoint){
        curve.setEndX(endPoint.x.doubleValue());
        curve.setEndY(endPoint.y.doubleValue());
    }
    static void setControls(QuadCurve curve,Point startPoint, Point endPoint){
        int sign = startPoint.y.doubleValue() < endPoint.y.doubleValue() ? 1 : -1;
        double x = startPoint.x.doubleValue() +  sign*2.5*Math.sqrt(Math.abs(endPoint.y.doubleValue() - startPoint.y.doubleValue()));
        double y = (startPoint.y.doubleValue() + endPoint.y.doubleValue()) / 2;
        curve.setControlX(x);
        curve.setControlY(y);
    }

    static Line createLine(Point startPoint, Point endPoint, DoubleProperty weight) {
        Line line = new Line();
        line.startXProperty().bind(startPoint.x);
        line.startYProperty().bind(startPoint.y);
        line.endXProperty().bind(endPoint.x);
        line.endYProperty().bind(endPoint.y);
        return line;
    }
}

package Visualisation;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SCurveFlowDiagram extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create the main layout
        Pane pane = new Pane();

        // Create two rectangles (representing flowchart boxes)
        Rectangle box1 = new javafx.scene.shape.Rectangle(100, 100, 100, 50);
        box1.setFill(Color.LIGHTBLUE);
        box1.setStroke(Color.BLACK);

        Rectangle box2 = new javafx.scene.shape.Rectangle(300, 100, 100, 50);
        box2.setFill(Color.LIGHTGREEN);
        box2.setStroke(Color.BLACK);

        // Create a CubicCurve between the two boxes (S-curve)
        CubicCurve curve = new CubicCurve();
        curve.setStartX(box1.getX() + box1.getWidth());
        curve.setStartY(box1.getY() + box1.getHeight() / 2);
        curve.setControlX1(200);
        curve.setControlY1(50);
        curve.setControlX2(200);
        curve.setControlY2(150);
        curve.setEndX(box2.getX());
        curve.setEndY(box2.getY() + box2.getHeight() / 2);
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(Color.BLACK);

        // Add an arrowhead at the end of the curve
        Polygon arrow = new Polygon(); 
        arrow.getPoints().addAll(
            box2.getX() - 10, box2.getY() + box2.getHeight() / 2 - 5,
            box2.getX(), box2.getY() + box2.getHeight() / 2,
            box2.getX() - 10, box2.getY() + box2.getHeight() / 2 + 5
        );
        arrow.setFill(Color.BLACK);

        // Add text labels inside the boxes
        Text text1 = new Text(box1.getX() + 30, box1.getY() + 30, "Box 1");
        Text text2 = new Text(box2.getX() + 30, box2.getY() + 30, "Box 2");

        // Create a small circle (representing the point) to animate along the curve
        Circle movingPoint = new Circle(5, Color.RED);

        // Create a PathTransition for the moving point
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(5));  // Duration of the animation
        pathTransition.setPath(curve);                    // Path to follow (the curve)
        pathTransition.setNode(movingPoint);              // Node to animate (the circle)
        pathTransition.setCycleCount(PathTransition.INDEFINITE);  // Repeat indefinitely
        pathTransition.setAutoReverse(true);              // Reverse direction at the end

        // Start the animation
        pathTransition.play();

        // Add everything to the pane
        pane.getChildren().addAll(box1, box2, curve, arrow, text1, text2, movingPoint);

        // Create the scene
        Scene scene = new Scene(pane, 500, 300);
        primaryStage.setTitle("S-Curve Flow Diagram with Animation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


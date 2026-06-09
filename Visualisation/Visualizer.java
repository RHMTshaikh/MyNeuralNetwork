package Visualisation;

import java.awt.image.BufferedImage; // Needed for ImageIO
import java.io.File;                // Needed for FileChooser/Saving
import java.io.IOException;         // Needed for ImageIO exception handling
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;       // Needed for saving the image

import Brain_Regions.Input_Pattern_Recognition;
// --- Import Brain types ---
import Brains.My_Brain;
import Brains.Speed_Brain;
import Brains.Copy_Cat_Brain;
import Brains.Counting_Brain;
import Brains.Edge_Detector_Brain;
import Brains.Efficient_Networking_Demo_Brain;
import Brains.Input_Pattern_Recognition_Brain;
// Removed duplicate import: import Brains.Speed_Brain;
import TheBrain.Brain;
// --- End Brain imports ---

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.embed.swing.SwingFXUtils; // Needed to convert JavaFX Image to BufferedImage
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters; // Optional, for snapshot settings
import javafx.scene.control.Button;      // The button!
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage; // The snapshot result
import javafx.scene.layout.AnchorPane;   // Layout pane for positioning
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;        // Optional, for snapshot background
import javafx.stage.FileChooser;       // Dialog to choose save location
import javafx.stage.Stage;
import javafx.util.Duration;


public class Visualizer extends Application {

    // Make myBrain accessible to the button's action handler
    private Brain myBrain;
    // Make the group accessible for snapshotting
    private Group visualizationGroup;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // --- Initialize Brain ---
        // Choose ONE brain implementation:
        // myBrain = new My_Brain();
        // myBrain = new Copy_Cat_Brain();
        // myBrain = new Counting_Brain();
        // myBrain = new Speed_Brain();
        // myBrain = new Edge_Detector_Brain(); // Assign to the class member
        // myBrain = new Efficient_Networking_Demo_Brain(); // Assign to the class member
        myBrain = new Input_Pattern_Recognition_Brain();
        
        if (myBrain == null) {
            System.err.println("ERROR: No Brain implementation selected!");
            Platform.exit(); // Exit if no brain is chosen
            return;
        }
        visualizationGroup = myBrain.group; // Assign to the class member
        if (visualizationGroup == null) {
            System.err.println("ERROR: The selected Brain's group is null!");
            Platform.exit();
            return;
        }


        // --- Setup Content Pane ---
        // This Pane contains the visualization Group and its size is bound to the Group's size
        // This helps the ScrollPane determine the actual content dimensions.
        Pane contentPane = create_new_pane(visualizationGroup);

        // --- Setup ScrollPane ---
        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setPannable(true); // Allows dragging the content with the mouse

        // --- REMOVED THESE LINES TO ENABLE SCROLLING ---
        // scrollPane.setFitToWidth(true);  // This prevents horizontal scrolling
        // scrollPane.setFitToHeight(true); // This prevents vertical scrolling
        // --- END REMOVED LINES ---

        // --- Create Save Button ---
        Button saveButton = new Button("Save Image");
        saveButton.setOnAction(event -> saveSnapshot(primaryStage, visualizationGroup));

        // --- Create Root Layout (AnchorPane) ---
        // This pane holds the ScrollPane and the Button
        AnchorPane rootLayout = new AnchorPane();

        // Add ScrollPane first (drawn underneath)
        rootLayout.getChildren().add(scrollPane);
        // Anchor ScrollPane to fill the entire AnchorPane
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        // Add Button on top
        rootLayout.getChildren().add(saveButton);
        // Anchor Button to the top-left corner with some padding
        AnchorPane.setTopAnchor(saveButton, 10.0);
        AnchorPane.setLeftAnchor(saveButton, 10.0);


        // --- Setup Scene ---
        Scene scene = new Scene(rootLayout); // Use the AnchorPane as the root

        // --- Configure Stage ---
        primaryStage.setScene(scene);
        primaryStage.setTitle("Brain Visualization");
        primaryStage.setMaximized(true);
        primaryStage.show();

        // --- Start Simulation Cycle ---
        scheduleCycle(myBrain); // Pass only the brain instance
    }

    // Modified scheduleCycle to take only the brain
    private void scheduleCycle(Brain brainInstance) {
        CompletableFuture.runAsync(() ->
            {
                try {
                    brainInstance.step(); // Use the passed instance
                } catch (Exception e) {
                    System.err.println("Error during brain step:");
                    e.printStackTrace();
                }
            }
        )
        .thenRun(() ->
            Platform.runLater(() -> {
                try {
                    // It's crucial that update_visual() modifies the layout bounds
                    // of the visualizationGroup if its size changes.
                    brainInstance.update_visual(); // Use the passed instance

                    // Schedule the next cycle
                    PauseTransition pause = new PauseTransition(Duration.seconds(pause_duration)); // Adjust duration as needed
                    // Pass the same instance recursively
                    pause.setOnFinished(e -> scheduleCycle(brainInstance));
                    pause.play();
                } catch (Exception e) {
                    System.err.println("Error during UI update or scheduling next cycle:");
                    e.printStackTrace();
                }
            })
        )
        .exceptionally(ex -> { // Add exception handling for the async part
            System.err.println("Exception in async brain step execution: " + ex);
            ex.printStackTrace();
            // Optionally decide if you want to stop the cycle here
            return null; // Required return for exceptionally
        });
    }

    private Pane create_new_pane(Group group) {
        Pane pane = new Pane(group); // Add the group to the pane

        // Bind the pane's preferred size to the group's actual layout bounds + padding.
        // This tells the ScrollPane how big the content *wants* to be.
        DoubleBinding paneWidth = javafx.beans.binding.Bindings.createDoubleBinding(
            () -> {
                // Ensure layout bounds are calculated before getting width
                group.autosize(); // May not be strictly necessary but can help ensure bounds are up-to-date
                return group.getLayoutBounds().getWidth() + 50; // Add padding
                },
            group.layoutBoundsProperty() // Recalculate when bounds change
        );
        DoubleBinding paneHeight = javafx.beans.binding.Bindings.createDoubleBinding(
            () -> {
                // Ensure layout bounds are calculated before getting height
                 group.autosize(); // May not be strictly necessary but can help ensure bounds are up-to-date
                 return group.getLayoutBounds().getHeight() + 50; // Add padding
                 },
             group.layoutBoundsProperty() // Recalculate when bounds change
        );

        // Set the Pane's preferred size based on the bindings
        pane.prefWidthProperty().bind(paneWidth);
        pane.prefHeightProperty().bind(paneHeight);

        // Optional: Set minimum size as well if needed, although pref size is usually sufficient for ScrollPane
        // pane.minWidthProperty().bind(paneWidth);
        // pane.minHeightProperty().bind(paneHeight);

        return pane;
    }

    // --- Method to handle saving the snapshot ---
    private void saveSnapshot(Stage ownerStage, Group groupToCapture) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Visualization Image");
        // Suggest a filename based on the Brain type if possible
        String brainName = myBrain.getClass().getSimpleName().replace("_Brain", "");
        fileChooser.setInitialFileName(brainName + "_visualization.png");

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            try {
                // Ensure the filename ends with .png
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");
                }

                System.out.println("Preparing snapshot for: " + groupToCapture);
                System.out.println("Group Bounds: " + groupToCapture.getLayoutBounds());


                // Snapshot the specific group, not the whole scene/scrollpane
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.WHITE); // Set background to white (good for transparency)
                // Optionally, set a viewport if you only want to capture a specific area
                // params.setViewport(new Rectangle2D(0, 0, desiredWidth, desiredHeight));

                // Take the snapshot on the JavaFX Application Thread
                WritableImage writableImage = groupToCapture.snapshot(params, null);
                System.out.println("Snapshot taken: " + writableImage.getWidth() + "x" + writableImage.getHeight());


                if (writableImage.getWidth() == 0 || writableImage.getHeight() == 0) {
                     System.err.println("Warning: Snapshot dimensions are zero. Is the group visible and laid out?");
                     // Consider adding an alert dialog for the user
                     return; // Don't try to save an empty image
                }


                // Convert JavaFX WritableImage to AWT BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                if (bufferedImage == null) {
                     System.err.println("Error: Failed to convert WritableImage to BufferedImage.");
                     return;
                }


                // Save the BufferedImage to the chosen file
                boolean success = ImageIO.write(bufferedImage, "png", file);

                if(success) {
                    System.out.println("Snapshot saved successfully to: " + file.getAbsolutePath());
                } else {
                    System.err.println("Error: ImageIO failed to write the PNG file. Check writers for PNG format.");
                }

            } catch (IOException ex) {
                System.err.println("Error saving snapshot (I/O): " + ex.getMessage());
                ex.printStackTrace();
                // Optionally show an error dialog to the user here
            } catch (Exception ex) {
                System.err.println("An unexpected error occurred during snapshot: " + ex.getMessage());
                ex.printStackTrace();
                // Optionally show an error dialog to the user here
            }
        } else {
            System.out.println("Save command cancelled by user.");
        }
    }

    double pause_duration = .1;
    public static void main(String[] args) {
        // Basic check before launching
        System.out.println("JavaFX Runtime Version: " + System.getProperty("javafx.runtime.version"));
        try {
            launch(args);
        } catch (IllegalStateException e) {
            System.err.println("Error launching JavaFX Application: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Toolkit not initialized")) {
                System.err.println("This might happen if JavaFX components are missing or not configured correctly.");
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Application launch failed:");
            e.printStackTrace();
        }
    }
}
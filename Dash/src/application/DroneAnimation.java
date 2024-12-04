package application;

import javafx.animation.PathTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class DroneAnimation {
    private ImageView drone;
    private Label droneLabel;
    private Pane visualizationPane;

    public DroneAnimation(Pane visualizationPane) {
        this.visualizationPane = visualizationPane;

        // Load the drone image
        Image droneImage = new Image(getClass().getResourceAsStream("drone.png"));
        drone = new ImageView(droneImage);
        drone.setFitWidth(50); // Set the width of the image
        drone.setFitHeight(50); // Set the height of the image
        drone.setLayoutX(30); // Initial base X position
        drone.setLayoutY(30); // Initial base Y position
        
     // Add the label above the drone
        droneLabel = new Label("Drone");
        droneLabel.setLayoutX(drone.getLayoutX() + 10); // Position label above the drone
        droneLabel.setLayoutY(drone.getLayoutY() - 20); // Offset label above the image

        // Add the drone and label to the visualization pane
        visualizationPane.getChildren().addAll(drone, droneLabel);
    }

    public void visit(double x, double y) {
        // Clamp the target position to stay within the bounds of the pane
        double hoverX = clamp(x, 0, visualizationPane.getPrefWidth() - drone.getFitWidth());
        double hoverY = clamp(y, 0, visualizationPane.getPrefHeight() - drone.getFitHeight());

        // Debugging pane size and position
        System.out.println("Visualization pane bounds: Width=" + visualizationPane.getWidth() + ", Height=" + visualizationPane.getHeight());
        System.out.println("Drone current position: (" + drone.getLayoutX() + ", " + drone.getLayoutY() + ")");
        System.out.println("Target position: (" + hoverX + ", " + hoverY + ")");

        // Ensure the drone is visible
        drone.setVisible(true);
        drone.setOpacity(1.0);

        // Reset any transformations
        drone.getTransforms().clear();

        // Bring the drone to the front
        drone.toFront();

        // Create a path from the current position to the target position
        Line path = new Line(drone.getLayoutX(), drone.getLayoutY(), hoverX, hoverY);
        PathTransition transition = new PathTransition(Duration.seconds(2), path, drone);

        // Ensure the drone's position is updated when the animation finishes
        transition.setOnFinished(event -> {
            // Update the drone's layout coordinates to match the target position
            drone.setLayoutX(hoverX);
            drone.setLayoutY(hoverY);

            // Debug output
            System.out.println("Drone arrived at target position: (" + hoverX + ", " + hoverY + ")");
            
         // Reset translations to prevent visual inconsistencies
            drone.setTranslateX(0);
            drone.setTranslateY(0);

            // Force UI to re-render
            visualizationPane.requestLayout();
            visualizationPane.layout();
        });

        // Play the transition animation
        transition.play();

        // Debug output
        System.out.println("Drone moving to: (" + hoverX + ", " + hoverY + ")");
    }
    
    
    public void returnToBase() {
        double baseX = 30; // Original X position
        double baseY = 30; // Original Y position

        // Debug information
        System.out.println("Returning drone to base:");
        System.out.println("Current position: (" + drone.getLayoutX() + ", " + drone.getLayoutY() + ")");
        System.out.println("Base position: (" + baseX + ", " + baseY + ")");

        // Ensure drone is visible and reset any transformations
        drone.setVisible(true);
        drone.setOpacity(1.0);
        drone.getTransforms().clear();

        // Directly update layout coordinates to prepare for animation
        double startX = drone.getLayoutX();
        double startY = drone.getLayoutY();

        // Use a Timeline to animate the drone's return
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.getKeyFrames().addAll(
            new javafx.animation.KeyFrame(
                Duration.ZERO,
                e -> {
                    // Debug initial position
                    System.out.println("Start position: (" + startX + ", " + startY + ")");
                }
            ),
            new javafx.animation.KeyFrame(
                Duration.seconds(2),
                e -> {
                    // Update layoutX and layoutY at the end of the animation
                    drone.setLayoutX(baseX);
                    drone.setLayoutY(baseY);

                    // Clear translation to avoid visual discrepancies
                    drone.setTranslateX(0);
                    drone.setTranslateY(0);

                    System.out.println("Drone arrived at base: (" + baseX + ", " + baseY + ")");
                }
            )
        );

        // Play the animation
        timeline.play();
        System.out.println("Timeline animation started for returning to base.");
    }



    
    public double getDroneWidth() {
        return drone.getFitWidth();
    }

    public double getDroneHeight() {
        return drone.getFitHeight();
    }
    
    
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public Node getDroneNode() {
        return this.drone; // Replace with the actual node representing the drone
    }
}
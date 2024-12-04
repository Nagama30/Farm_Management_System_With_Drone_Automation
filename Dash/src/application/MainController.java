package application;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private TreeView<String> itemsTreeView;
    @FXML
    private Pane visualizationPane;

    private TreeItem<String> rootItem;
    //private Circle drone;
    private Map<String, Rectangle> visualizationMap = new HashMap<>();
    private Map<String, Text> labelMap = new HashMap<>();
    private List<double[]> availableGridPositions = new ArrayList<>();
    private final double[] dronePosition = {0, 0}; // Reserve the top-left grid for the drone
    private static final double CELL_WIDTH = 100;
    private static final double CELL_HEIGHT = 100;
    
    @FXML
    private RadioButton visitItemRadioButton;
    @FXML
    private RadioButton scanFarmRadioButton;
    private ToggleGroup droneActionGroup;
    @FXML
    private Button goButton;
    
    private DroneAnimation droneAnimation; // Declare droneAnimation here
    private boolean isDroneReturning = false;  

    @FXML
    public void initialize() {
        rootItem = new TreeItem<>("Farm");
        itemsTreeView.setRoot(rootItem);
        itemsTreeView.setShowRoot(true);

        // Initialize the ToggleGroup and assign it to the radio buttons
        droneActionGroup = new ToggleGroup();
        visitItemRadioButton.setToggleGroup(droneActionGroup);
        scanFarmRadioButton.setToggleGroup(droneActionGroup);
        
        // Optionally, set a default selection
        visitItemRadioButton.setSelected(true); // By default, select the first button
        droneAnimation = new DroneAnimation(visualizationPane); // Properly initialize DroneAnimation
    }



    @FXML
    private void handleGoButton() {
        if (isDroneReturning) {
            // If the drone is returning, move it back to the base
            droneAnimation.returnToBase();
            goButton.setText("Go");
            isDroneReturning = false;
        } else {
            // If the drone is not returning, determine the selected action
            if (visitItemRadioButton.isSelected()) {
            	handleVisitAction();
            } else if (scanFarmRadioButton.isSelected()) {
                handleScanFarm();
            }
        }
    }

    
    //drone visiting item/ container
	private void handleVisitAction() {
	    // Get the selected item or container in the TreeView
	    TreeItem<String> selected = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    // Ensure the selection is valid and not the root
	    if (selected == null || selected == rootItem) {
	        showAlert("Error", "Please select a valid item or container.");
	        return;
	    }
	
	    // Get the rectangle of the selected item/container
	    Rectangle targetRectangle = visualizationMap.get(selected.getValue());
	    if (targetRectangle == null) {
	        showAlert("Error", "The selected item or container does not exist in the visualization.");
	        return;
	    }
	
	    // Check if the selected element has a parent container (it's an item inside a container)
	    TreeItem<String> parentContainer = selected.getParent();
	    double targetX, targetY;
	
	    if (parentContainer != null && parentContainer != rootItem) {
	        // The selected element is an item inside a container
	        Rectangle containerRectangle = visualizationMap.get(parentContainer.getValue());
	        if (containerRectangle == null) {
	            showAlert("Error", "The parent container does not exist in the visualization.");
	            return;
	        }

	        // Calculate the item's absolute position relative to the container
	     // Use the item's absolute position directly
	        targetX = targetRectangle.getLayoutX() + targetRectangle.getWidth() / 2 - droneAnimation.getDroneWidth() / 2;
	        targetY = targetRectangle.getLayoutY() + targetRectangle.getHeight() / 2 - droneAnimation.getDroneHeight() / 2;
	        //System.out.println("Selected Item: " + selected.getValue());
		    //System.out.println("Parent Container: " + (parentContainer != null ? parentContainer.getValue() : "None"));
		    //System.out.println("Container Layout: (" + containerRectangle.getLayoutX() + ", " + containerRectangle.getLayoutY() + ")");
		    //System.out.println("Item Layout: (" + targetRectangle.getLayoutX() + ", " + targetRectangle.getLayoutY() + ")");
		    //System.out.println("Calculated Target: (" + targetX + ", " + targetY + ")");
	    } else {
	        // The selected element is a standalone container
	        targetX = targetRectangle.getLayoutX() + targetRectangle.getWidth() / 2 - droneAnimation.getDroneWidth() / 2;
	        targetY = targetRectangle.getLayoutY() + targetRectangle.getHeight() / 2 - droneAnimation.getDroneHeight() / 2;
	    }

	
	    // Move the drone to the calculated position
	    droneAnimation.visit(targetX, targetY);
	
	    // Update the "Go" button to allow returning
	    goButton.setText("Return");
	    isDroneReturning = true;
	}
	
	    
    /**
     * Scan the entire farm by visiting all containers sequentially.
     */
	public void handleScanFarm() {
	    if (visualizationPane == null) {
	        showAlert("Error", "Visualization pane not initialized.");
	        return;
	    }
	
	    // Define the rectangular path with exact corners
	    double topLeftX = 30; // Start position (30,30)
	    double topLeftY = 30;
	    double topRightX = visualizationPane.getWidth() - 60; // Right boundary
	    double bottomRightY = visualizationPane.getHeight() - 60; // Bottom boundary
	
	    // List of sequential target points (rectangle corners)
	    List<double[]> path = List.of(
	        new double[]{topLeftX, topLeftY},          // Start (top-left corner)
	        new double[]{topRightX, topLeftY},         // Top-right corner
	        new double[]{topRightX, bottomRightY},     // Bottom-right corner
	        new double[]{topLeftX, bottomRightY},      // Bottom-left corner
	        new double[]{topLeftX, topLeftY}           // Back to origin
	    );
	
	    // Sequentially move the drone along the path
	    moveAlongPath(path, 2000); // 1500ms for each segment
	}
	
	

	private void moveAlongPath(List<double[]> path, int durationMillis) {
	    if (path == null || path.isEmpty()) {
	        return;
	    }
	
	    new Thread(() -> {
	        for (int i = 0; i < path.size() - 1; i++) {
	            double[] start = path.get(i);
	            double[] end = path.get(i + 1);
	
	            javafx.application.Platform.runLater(() -> {
	                animateDroneMovement(start[0], start[1], end[0], end[1], durationMillis);
	            });
	
	            try {
	                Thread.sleep(durationMillis); // Wait for the animation to complete
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                return; // Exit if interrupted
	            }
	        }
	    }).start();
	}
	
	

	private void animateDroneMovement(double startX, double startY, double endX, double endY, int durationMillis) {
	    Timeline timeline = new Timeline();
	
	    // Use KeyValues to interpolate drone's position
	    KeyValue xValue = new KeyValue(droneAnimation.getDroneNode().layoutXProperty(), endX);
	    KeyValue yValue = new KeyValue(droneAnimation.getDroneNode().layoutYProperty(), endY);
	
	    // Create a KeyFrame with the specified duration and KeyValues
	    KeyFrame keyFrame = new KeyFrame(Duration.millis(durationMillis), xValue, yValue);
	
	    timeline.getKeyFrames().add(keyFrame);
	    timeline.play();
	}
	  
	    

	 private boolean isOutsideBounds(double x, double y) {
        return x < 0 || x > visualizationPane.getPrefWidth() || y < 0 || y > visualizationPane.getPrefHeight();
    }




	// Method for adding a new item container with additional fields
	public void handleAddItemContainer() {
	    // Prompt for container name
	    TextInputDialog nameDialog = new TextInputDialog("Building");
	    nameDialog.setTitle("Add Item Container");
	    nameDialog.setHeaderText("Enter the name of the new container:");
	    Optional<String> nameResult = nameDialog.showAndWait();
	
	    nameResult.ifPresent(name -> {
	        // Prompt for price
	        TextInputDialog priceDialog = new TextInputDialog("0");
	        priceDialog.setTitle("Add Price");
	        priceDialog.setHeaderText("Enter the price of the container:");
	        Optional<String> priceResult = priceDialog.showAndWait();
	
	        priceResult.ifPresent(priceStr -> {
	            try {
	                double price = Double.parseDouble(priceStr);
	
	                // Prompt for x-coordinate
	                TextInputDialog xDialog = new TextInputDialog("0");
	                xDialog.setTitle("Container Position");
	                xDialog.setHeaderText("Enter the X Coordinate of the container:");
	                Optional<String> xResult = xDialog.showAndWait();
	
	                xResult.ifPresent(xStr -> {
	                    try {
	                        double x = Double.parseDouble(xStr);
	
	                        // Prompt for y-coordinate
	                        TextInputDialog yDialog = new TextInputDialog("0");
	                        yDialog.setTitle("Container Position");
	                        yDialog.setHeaderText("Enter the Y Coordinate of the container:");
	                        Optional<String> yResult = yDialog.showAndWait();
	
	                        yResult.ifPresent(yStr -> {
	                            try {
	                                double y = Double.parseDouble(yStr);
	
	                                // Prompt for length
	                                TextInputDialog lengthDialog = new TextInputDialog("100");
	                                lengthDialog.setTitle("Container Dimensions");
	                                lengthDialog.setHeaderText("Enter the Length of the container:");
	                                Optional<String> lengthResult = lengthDialog.showAndWait();
	
	                                lengthResult.ifPresent(lengthStr -> {
	                                    try {
	                                        double length = Double.parseDouble(lengthStr);
	
	                                        // Prompt for width
	                                        TextInputDialog widthDialog = new TextInputDialog("100");
	                                        widthDialog.setTitle("Container Dimensions");
	                                        widthDialog.setHeaderText("Enter the Width of the container:");
	                                        Optional<String> widthResult = widthDialog.showAndWait();
	
	                                        widthResult.ifPresent(widthStr -> {
	                                            try {
	                                                double width = Double.parseDouble(widthStr);
	                                                
	                                                // Check if the container exceeds boundaries
	                                                if (x + length > visualizationPane.getWidth() || y + width > visualizationPane.getHeight()) {
	                                                    showAlert("Boundary Error", "Container exceeds the farm boundaries.");
	                                                    return; // Abort adding the container
	                                                }
	                                                
	                                                // Create a temporary rectangle to check overlap
	                                                Rectangle tempRect = new Rectangle(length, width);
	                                                tempRect.setLayoutX(x);
	                                                tempRect.setLayoutY(y);

	                                                if (isOverlapping(tempRect)) {
	                                                    showAlert("Overlap Error", "The new container overlaps with an existing container or item.");
	                                                    return; // Abort adding the container
	                                                }
	
	                                                // Prompt for height
	                                                TextInputDialog heightDialog = new TextInputDialog("50");
	                                                heightDialog.setTitle("Container Height");
	                                                heightDialog.setHeaderText("Enter the Height of the container:");
	                                                Optional<String> heightResult = heightDialog.showAndWait();
	
	                                                heightResult.ifPresent(heightStr -> {
	                                                    try {
	                                                        //double height = Double.parseDouble(heightStr);
	
	                                                        // Create container and add it to the TreeView
	                                                        TreeItem<String> containerItem = new TreeItem<>(name);
	                                                        rootItem.getChildren().add(containerItem);
	
	                                                        // Expand the rootItem to display the new container
	                                                        rootItem.setExpanded(true);
	
	                                                        // Create visual rectangle and label
	                                                        Rectangle containerRect = new Rectangle(length, width, Color.LIGHTGRAY);
	                                                        containerRect.setStroke(Color.BLACK);
	
	                                                        Text containerLabel = new Text(name + " (" + length + " x " + width + ")");
	                                                        containerLabel.setX(x + (length / 2) - (containerLabel.getBoundsInLocal().getWidth() / 2));
	                                                        containerLabel.setY(y - 10);
	
	                                                        containerRect.setLayoutX(x);
	                                                        containerRect.setLayoutY(y);
	                                                        visualizationPane.getChildren().addAll(containerRect, containerLabel);
	
	                                                        // Track the container in maps
	                                                        visualizationMap.put(name, containerRect);
	                                                        labelMap.put(name, containerLabel);
	                                                    } catch (NumberFormatException e) {
	                                                        showAlert("Error", "Invalid height input. Please enter a valid number.");
	                                                    }
	                                                });
	                                            } catch (NumberFormatException e) {
	                                                showAlert("Error", "Invalid width input. Please enter a valid number.");
	                                            }
	                                        });
	                                    } catch (NumberFormatException e) {
	                                        showAlert("Error", "Invalid length input. Please enter a valid number.");
	                                    }
	                                });
	                            } catch (NumberFormatException e) {
	                                showAlert("Error", "Invalid Y Coordinate. Please enter a valid number.");
	                            }
	                        });
	                    } catch (NumberFormatException e) {
	                        showAlert("Error", "Invalid X Coordinate. Please enter a valid number.");
	                    }
	                });
	            } catch (NumberFormatException e) {
	                showAlert("Error", "Invalid price. Please enter a valid number.");
	            }
	        });
	    });
	}
	

	
	private List<double[]> reserveAdjacentGrids(double startX, double startY, int gridSpan) {
        List<double[]> reservedGrids = new ArrayList<>();

        for (int i = 0; i < gridSpan; i++) {
            double newX = startX + (i * CELL_WIDTH);

            // Abort if the grid is out of bounds
            if (newX >= visualizationPane.getPrefWidth()) {
                return new ArrayList<>();
            }

            double[] grid = {newX, startY};

            // Ensure the grid is not already occupied
            boolean isAvailable = availableGridPositions.stream()
                    .anyMatch(pos -> pos[0] == grid[0] && pos[1] == grid[1]);

            if (!isAvailable) {
                return new ArrayList<>(); // Abort if any grid is unavailable
            }

            reservedGrids.add(grid);
        }

        // Remove reserved grids from availableGridPositions
        availableGridPositions.removeIf(pos -> reservedGrids.stream()
                .anyMatch(reserved -> reserved[0] == pos[0] && reserved[1] == pos[1]));

        return reservedGrids;
    }




	// Method for adding an item with additional fields
	public void handleAddItem() {
	    TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    // Check that a container is selected and not the root or null
	    if (selectedContainer == null || selectedContainer == rootItem) {
	        showAlert("Error", "Please select a container to add the item to.");
	        return;
	    }
	
	    // Prompt for item name
	    TextInputDialog nameDialog = new TextInputDialog("Item");
	    nameDialog.setTitle("Add Item");
	    nameDialog.setHeaderText("Enter the name of the new item:");
	    Optional<String> nameResult = nameDialog.showAndWait();
	
	    nameResult.ifPresent(name -> {
	        // Prompt for price
	        TextInputDialog priceDialog = new TextInputDialog("0");
	        priceDialog.setTitle("Add Price");
	        priceDialog.setHeaderText("Enter the price of the item:");
	        Optional<String> priceResult = priceDialog.showAndWait();
	
	        priceResult.ifPresent(priceStr -> {
	            try {
	                double price = Double.parseDouble(priceStr);
	
	                // Prompt for relative X and Y coordinates
	                TextInputDialog xDialog = new TextInputDialog("0");
	                xDialog.setTitle("Item Position");
	                xDialog.setHeaderText("Enter the X Coordinate of the item (relative to the container):");
	                Optional<String> xResult = xDialog.showAndWait();
	
	                xResult.ifPresent(xStr -> {
	                    try {
	                        double relativeX = Double.parseDouble(xStr);
	
	                        TextInputDialog yDialog = new TextInputDialog("0");
	                        yDialog.setTitle("Item Position");
	                        yDialog.setHeaderText("Enter the Y Coordinate of the item (relative to the container):");
	                        Optional<String> yResult = yDialog.showAndWait();
	
	                        yResult.ifPresent(yStr -> {
	                            try {
	                                double relativeY = Double.parseDouble(yStr);
	
	                                // Locate the container's rectangle
	                                Rectangle containerRect = visualizationMap.get(selectedContainer.getValue());
	                                if (containerRect == null) {
	                                    showAlert("Error", "The selected container does not have a visual representation.");
	                                    return;
	                                }
	
	                                // Prompt for dimensions
	                                TextInputDialog lengthDialog = new TextInputDialog("50");
	                                lengthDialog.setTitle("Item Dimensions");
	                                lengthDialog.setHeaderText("Enter the length of the item:");
	                                Optional<String> lengthResult = lengthDialog.showAndWait();
	
	                                lengthResult.ifPresent(lengthStr -> {
	                                    try {
	                                        double length = Double.parseDouble(lengthStr);
	
	                                        TextInputDialog widthDialog = new TextInputDialog("30");
	                                        widthDialog.setTitle("Item Dimensions");
	                                        widthDialog.setHeaderText("Enter the width of the item:");
	                                        Optional<String> widthResult = widthDialog.showAndWait();
	
	                                        widthResult.ifPresent(widthStr -> {
	                                            try {
	                                                double width = Double.parseDouble(widthStr);
	                                                
	                                                // Calculate absolute position and boundaries
	                                                double absoluteX_boundry = containerRect.getLayoutX() + relativeX;
	                                                double absoluteY_boundry = containerRect.getLayoutY() + relativeY;
	                                                double itemRight = absoluteX_boundry + length;
	                                                double itemBottom = absoluteY_boundry + width;

	                                                // Check if the item exceeds the container's boundaries
	                                                if (itemRight > containerRect.getLayoutX() + containerRect.getWidth() ||
	                                                    itemBottom > containerRect.getLayoutY() + containerRect.getHeight()) {
	                                                    showAlert("Boundary Error", "Item exceeds the container's boundaries.");
	                                                    return; // Abort adding the item
	                                                }
	
	                                                
	                                                // Create a temporary rectangle to check overlap
	                                                Rectangle tempRect = new Rectangle(length, width);
	                                                tempRect.setLayoutX(absoluteX_boundry);
	                                                tempRect.setLayoutY(absoluteY_boundry);

	                                                if (isOverlapping(tempRect)) {
	                                                    showAlert("Overlap Error", "The new item overlaps with an existing item or container.");
	                                                    return; // Abort adding the item
	                                                }
	                                                
	                                                
	                                                TextInputDialog heightDialog = new TextInputDialog("20");
	                                                heightDialog.setTitle("Item Height");
	                                                heightDialog.setHeaderText("Enter the height of the item:");
	                                                Optional<String> heightResult = heightDialog.showAndWait();
	
	                                                heightResult.ifPresent(heightStr -> {
	                                                    try {
	                                                        double height = Double.parseDouble(heightStr);
	
	                                                        // Create and position the item relative to the container
	                                                        double absoluteX = containerRect.getLayoutX() + relativeX;
	                                                        double absoluteY = containerRect.getLayoutY() + relativeY;
	
	                                                        Rectangle itemRect = new Rectangle(length, height, Color.BEIGE);
	                                                        itemRect.setStroke(Color.BLACK);
	
	                                                        Text itemLabel = new Text(name + " ($" + price + ")");
	                                                        itemLabel.setX(absoluteX + (length / 2) - (itemLabel.getBoundsInLocal().getWidth() / 2));
	                                                        itemLabel.setY(absoluteY - 10);
	
	                                                        itemRect.setLayoutX(absoluteX);
	                                                        itemRect.setLayoutY(absoluteY);
	                                                        visualizationPane.getChildren().addAll(itemRect, itemLabel);
	
	                                                        // Add the item to the TreeView and visualization map
	                                                        TreeItem<String> itemTreeItem = new TreeItem<>(name);
	                                                        selectedContainer.getChildren().add(itemTreeItem);
	                                                        
	                                                         // Expand the rootItem to display the new container
	                                                        rootItem.setExpanded(true);
	                                                        
	                                                        visualizationMap.put(name, itemRect);
	                                                        labelMap.put(name, itemLabel);
	                                                    } catch (NumberFormatException e) {
	                                                        showAlert("Error", "Invalid height input. Please enter a valid number.");
	                                                    }
	                                                });
	                                            } catch (NumberFormatException e) {
	                                                showAlert("Error", "Invalid width input. Please enter a valid number.");
	                                            }
	                                        });
	                                    } catch (NumberFormatException e) {
	                                        showAlert("Error", "Invalid length input. Please enter a valid number.");
	                                    }
	                                });
	                            } catch (NumberFormatException e) {
	                                showAlert("Error", "Invalid Y Coordinate. Please enter a valid number.");
	                            }
	                        });
	                    } catch (NumberFormatException e) {
	                        showAlert("Error", "Invalid X Coordinate. Please enter a valid number.");
	                    }
	                });
	            } catch (NumberFormatException e) {
	                showAlert("Error", "Invalid price. Please enter a valid number.");
	            }
	        });
	    });
	}
		


	private boolean expandContainerHorizontally(Rectangle containerRect, String containerName) {
        double startX = containerRect.getLayoutX() + containerRect.getWidth();
        double startY = containerRect.getLayoutY();

        if (startX + CELL_WIDTH > visualizationPane.getPrefWidth()) {
            return false; // No space to expand horizontally
        }

        double[] newPosition = {startX, startY};
        if (!availableGridPositions.contains(newPosition)) {
            return false; // Adjacent grid is already occupied
        }

        // Reserve the new grid and expand the container
        availableGridPositions.remove(newPosition);
        containerRect.setWidth(containerRect.getWidth() + CELL_WIDTH);
        return true;
    }
    
	// Method to delete a container and all its items
	public void handleDeleteContainer() {
	    TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedContainer == null || selectedContainer == rootItem) {
	        showAlert("Error", "Please select a valid container to delete.");
	        return;
	    }
	
	    // Confirm deletion
	    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmation.setTitle("Confirm Deletion");
	    confirmation.setHeaderText("Are you sure you want to delete this container and all its items?");
	    confirmation.setContentText("This action cannot be undone.");
	    Optional<javafx.scene.control.ButtonType> result = confirmation.showAndWait();
	
	    if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
	        return;
	    }
	
	    // Recursively delete all items inside the container
	    deleteItemsWithinContainer(selectedContainer);
	
	    // Remove the container itself
	    TreeItem<String> parent = selectedContainer.getParent();
	    if (parent != null) {
	        parent.getChildren().remove(selectedContainer);
	    }
	
	    // Remove the container's visual representation
	    String containerName = selectedContainer.getValue();
	    removeContainerItemVisualRepresentation(containerName);
	
	    System.out.println("Deleted container: " + containerName);
	}
	
	// Recursive method to delete all items within a container
	private void deleteItemsWithinContainer(TreeItem<String> container) {
	    // Iterate over a copy of the children to avoid ConcurrentModificationException
	    List<TreeItem<String>> childrenCopy = new ArrayList<>(container.getChildren());
	
	    for (TreeItem<String> child : childrenCopy) {
	        String childName = child.getValue();
	
	        // If the child has its own children (nested items), delete them first
	        if (!child.getChildren().isEmpty()) {
	            deleteItemsWithinContainer(child);
	        }
	
	        // Remove the child's visual representation
	        removeContainerItemVisualRepresentation(childName);
	
	        // Remove the child from the container
	        container.getChildren().remove(child);
	    }
	}
	
	
	
	// Helper method to remove visual elements (Rectangle and Text) for a given name
	private void removeContainerItemVisualRepresentation(String name) {
	    // Retrieve and remove the item's rectangle
	    Rectangle rect = visualizationMap.get(name);
	    if (rect != null) {
	        System.out.println("Removing Rectangle for: " + name);
	        visualizationPane.getChildren().remove(rect);
	        visualizationMap.remove(name); // Ensure it's removed from the map
	    }
	
	    // Retrieve and remove the item's label
	    Text label = labelMap.get(name);
	    if (label != null) {
	        System.out.println("Removing Label for: " + name);
	        visualizationPane.getChildren().remove(label);
	        labelMap.remove(name); // Ensure it's removed from the map
	    }
	
	    // Debug: Check if the element was successfully removed
	    System.out.println("VisualizationMap keys after removal: " + visualizationMap.keySet());
	    System.out.println("LabelMap keys after removal: " + labelMap.keySet());
	    System.out.println("VisualizationPane children: " + visualizationPane.getChildren());
	}


	// Recursive method to delete all items and sub-containers
	private void deleteContainerAndItems(TreeItem<String> container) {
	    // Iterate over a copy of the children to avoid ConcurrentModificationException
	    List<TreeItem<String>> childrenCopy = new ArrayList<>(container.getChildren());
	
	    for (TreeItem<String> child : childrenCopy) {
	        // Recursively delete child items or sub-containers
	        deleteContainerAndItems(child);
	
	        // Remove visual elements of the child
	        String itemName = child.getValue();
	        removeVisualElements(itemName);
	    }
	
	    // After all children are processed, clear the container's children
	    container.getChildren().clear();
	}

	
	
	// Helper method to recursively delete all items within a container
    private void deleteItemsRecursively(TreeItem<String> container) {
        for (TreeItem<String> childItem : new ArrayList<>(container.getChildren())) {
            String itemName = childItem.getValue();

            // Remove visual representation
            Rectangle itemRect = visualizationMap.remove(itemName);
            Text itemLabel = labelMap.remove(itemName);

            if (itemRect != null) {
                visualizationPane.getChildren().remove(itemRect);
            }
            if (itemLabel != null) {
                visualizationPane.getChildren().remove(itemLabel);
            }

            // Recursively delete nested items
            if (!childItem.getChildren().isEmpty()) {
                deleteItemsRecursively(childItem);
            }
            container.getChildren().remove(childItem);
        }
    }


    
    private boolean isItem(TreeItem<String> selectedItem) {
        if (selectedItem == null || selectedItem.getParent() == null) {
            return false; // Not valid or is the root
        }

        // An item is a leaf node in the TreeView (no children)
        return selectedItem.getChildren().isEmpty();
    }
    
    
    private boolean isContainer(TreeItem<String> selectedItem) {
        if (selectedItem == null) {
            return false;
        }

        // A container is a node with children (not a leaf node)
        return !selectedItem.getChildren().isEmpty();
    }
    

    // Method to rename selected item or container
	public void handleRename() {
	    TreeItem<String> selectedItem = itemsTreeView.getSelectionModel().getSelectedItem();
	    
	    if (selectedItem == null) {
	        showAlert("Error", "Please select an item or container to rename.");
	        return;
	    }
	
	    // Check if it's an item
	    if (!isItem(selectedItem)) {
	        showAlert("Error", "Please select an item, not a container.");
	        return;
	    }
	
	    TextInputDialog dialog = new TextInputDialog(selectedItem.getValue());
	    dialog.setTitle("Rename");
	    dialog.setHeaderText("Enter a new name:");
	    Optional<String> result = dialog.showAndWait();
	
	    result.ifPresent(name -> { // Use 'name' here instead of 'newName'
	        String oldName = selectedItem.getValue();
	        selectedItem.setValue(name);
	
	        // Update the name in the visualization maps and update label if it exists
	        Rectangle rect = visualizationMap.get(oldName);
	        Text label = labelMap.get(oldName);
	
	        if (rect != null && label != null) {
	        	
	        	// Extract price from the existing label, if any
	            String priceText = label.getText().replaceAll(".*\\(\\$|\\)", "");
	            double price = priceText.matches("\\d+(\\.\\d+)?") ? Double.parseDouble(priceText) : 0.0;
	        	
	            // Update maps
	            visualizationMap.remove(oldName);
	            visualizationMap.put(name, rect);

	            labelMap.remove(oldName);
	            labelMap.put(name, label);
	            
	            // Update the label text in the visualization pane
	            label.setText(name + " ($" + price + ")");
	
	            // Optionally reposition the label if needed
	            label.setX(rect.getLayoutX() + rect.getWidth() / 2 - label.getBoundsInLocal().getWidth() / 2);
	            label.setY(rect.getLayoutY() - 10); // Keep it slightly above the rectangle
	        } else {
	            // Handle cases where no visual element exists
	            showAlert("Warning", "Selected item/container has no visual representation.");
	        }
	    });
	}

    
	// Method to rename a selected container
    public void handleRenameContainer() {
        TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();
        
        if (selectedContainer == null) {
            showAlert("Error", "Please select a container to rename.");
            return;
        }

        // Check if it's a container
        if (!isContainer(selectedContainer)) {
            showAlert("Error", "Please select a container, not an item.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedContainer.getValue());
        dialog.setTitle("Rename Container");
        dialog.setHeaderText("Enter a new name for the container:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newName -> {
            String oldName = selectedContainer.getValue();
            selectedContainer.setValue(newName);

            // Update visualization
            Rectangle rect = visualizationMap.get(oldName);
            Text label = labelMap.get(oldName);

            if (rect != null && label != null) {
            	// Get the dimensions of the container
                double length = rect.getWidth();
                double width = rect.getHeight();

                // Update maps
                visualizationMap.remove(oldName);
                visualizationMap.put(newName, rect);

                labelMap.remove(oldName);
                labelMap.put(newName, label);

                // Update the label text
                label.setText(newName + " (" + (int) length + " x " + (int) width + ")");
                label.setX(rect.getLayoutX() + rect.getWidth() / 2 - label.getBoundsInLocal().getWidth() / 2);
                label.setY(rect.getLayoutY() - 10);
            } else {
                showAlert("Warning", "No visual representation found for the selected container.");
            }

            //System.out.println("Container renamed from " + oldName + " to " + newName);
        });
    }


	// Method to delete an item
	public void handleDeleteItem() {
	    TreeItem<String> selectedItem = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    // Check if the selected item is valid and not the root
	    if (selectedItem == null || selectedItem == rootItem) {
	        showAlert("Error", "Please select a valid item to delete.");
	        return;
	    }
	
	    // Confirm deletion
	    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmation.setTitle("Confirm Deletion");
	    confirmation.setHeaderText("Are you sure you want to delete this item?");
	    confirmation.setContentText("This action cannot be undone.");
	    Optional<javafx.scene.control.ButtonType> result = confirmation.showAndWait();
	
	    if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
	        return;
	    }
	
	    // Get the item's name
	    String itemName = selectedItem.getValue();
	
	    // Remove the item from the TreeView
	    TreeItem<String> parent = selectedItem.getParent();
	    if (parent != null) {
	        parent.getChildren().remove(selectedItem);
	    }
	
	    // Ensure the item's visual elements are removed
	    removeVisualElements(itemName);
	
	    // Reposition remaining items in the parent container, if applicable
	    if (parent != null) {
	        repositionItemsWithinContainer(parent);
	    }
	
	    System.out.println("Deleted item: " + itemName);
	}
	
	// Helper method to remove visual elements
	private void removeVisualElements(String name) {
	    // Retrieve and remove the item's rectangle
	    Rectangle itemRect = visualizationMap.get(name);
	    if (itemRect != null) {
	        System.out.println("Removing Rectangle for: " + name);
	        visualizationPane.getChildren().remove(itemRect);
	        visualizationMap.remove(name); // Ensure it's removed from the map
	    }
	
	    // Retrieve and remove the item's label
	    Text itemLabel = labelMap.get(name);
	    if (itemLabel != null) {
	        System.out.println("Removing Label for: " + name);
	        visualizationPane.getChildren().remove(itemLabel);
	        labelMap.remove(name); // Ensure it's removed from the map
	    }
	
	    // Debug: Check if the element was successfully removed
	    System.out.println("VisualizationMap keys after removal: " + visualizationMap.keySet());
	    System.out.println("LabelMap keys after removal: " + labelMap.keySet());
	    System.out.println("VisualizationPane children: " + visualizationPane.getChildren());
	}
	
	// Helper method to reposition items within the container after one is deleted
	private void repositionItemsWithinContainer(TreeItem<String> container) {
	    String containerName = container.getValue();
	    Rectangle containerRect = visualizationMap.get(containerName);
	
	    if (containerRect != null) {
	        double startX = containerRect.getLayoutX() + 10; // Margin within the container
	        double startY = containerRect.getLayoutY() + 10;
	
	        // Iterate over remaining items in the container
	        for (TreeItem<String> item : container.getChildren()) {
	            String itemName = item.getValue();
	            Rectangle itemRect = visualizationMap.get(itemName);
	            Text itemLabel = labelMap.get(itemName);
	
	            if (itemRect != null) {
	                itemRect.setLayoutX(startX);
	                itemRect.setLayoutY(startY);
	                startY += itemRect.getHeight() + 5; // Stack items vertically with spacing
	            }
	
	            if (itemLabel != null) {
	                itemLabel.setX(startX + 5);
	                itemLabel.setY(startY - itemRect.getHeight() / 2);
	            }
	        }
	    }
	}
	


	// Method to change price
	public void handleChangePrice() {
	    TreeItem<String> selectedItem = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedItem == null || !isItem(selectedItem)) {
	        showAlert("Error", "Please select a valid item to change the price.");
	        return;
	    }
	
	    TextInputDialog dialog = new TextInputDialog("0.0");
	    dialog.setTitle("Change Price");
	    dialog.setHeaderText("Enter a new price for the item:");
	    Optional<String> result = dialog.showAndWait();
	
	    result.ifPresent(priceStr -> {
	        try {
	            double price = Double.parseDouble(priceStr);
	
	            String oldName = selectedItem.getValue();
	            selectedItem.setValue(oldName + " ($" + price + ")");
	
	            // Update the label on the visualization pane
	            Text label = labelMap.get(oldName);
	            if (label != null) {
	                label.setText(oldName + " ($" + price + ")");
	            }
	
	            System.out.println("Price for item set to: $" + price);
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid price. Please enter a valid number.");
	        }
	    });
	}
	    

	// Separate handler for changing container price
	public void handleChangePriceContainer() {
	    TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();

	    if (selectedContainer == null || !isContainer(selectedContainer)) {
	        showAlert("Error", "Please select a valid container to change the price.");
	        return;
	    }

	    TextInputDialog dialog = new TextInputDialog("0.0");
	    dialog.setTitle("Change Price");
	    dialog.setHeaderText("Enter a new price for the container:");
	    Optional<String> result = dialog.showAndWait();

	    result.ifPresent(priceStr -> {
	        try {
	            double price = Double.parseDouble(priceStr);

	            String oldName = selectedContainer.getValue();
	            selectedContainer.setValue(oldName + " ($" + price + ")");

	            // Update the label on the visualization pane
	            Text label = labelMap.get(oldName);
	            if (label != null) {
	                label.setText(oldName + " ($" + price + ")");
	            }

	            System.out.println("Price for container set to: $" + price);
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid price. Please enter a valid number.");
	        }
	    });
	}


    // Method to change location
	public void handleChangeLocation() {
	    TreeItem<String> selectedItem = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedItem == null || !isItem(selectedItem)) {
	        showAlert("Error", "Please select a valid item to change the location.");
	        return;
	    }
	
	    TextInputDialog dialogX = new TextInputDialog("0");
	    dialogX.setTitle("Change Location");
	    dialogX.setHeaderText("Enter the X coordinate:");
	    Optional<String> resultX = dialogX.showAndWait();
	
	    TextInputDialog dialogY = new TextInputDialog("0");
	    dialogY.setTitle("Change Location");
	    dialogY.setHeaderText("Enter the Y coordinate:");
	    Optional<String> resultY = dialogY.showAndWait();
	
	    if (resultX.isPresent() && resultY.isPresent()) {
	        try {
	            double x = Double.parseDouble(resultX.get());
	            double y = Double.parseDouble(resultY.get());
	
	            Rectangle rect = visualizationMap.get(selectedItem.getValue());
	            if (rect != null) {
	                rect.setLayoutX(x);
	                rect.setLayoutY(y);
	            }
	
	            Text label = labelMap.get(selectedItem.getValue());
	            if (label != null) {
	                label.setX(x + 5);
	                label.setY(y - 10);
	            }
	
	            System.out.println("Location for item set to: (" + x + ", " + y + ")");
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid coordinates. Please enter valid numbers.");
	        }
	    }
	}

    
	// Separate handler for changing container location
	public void handleChangeLocationContainer() {
	    TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedContainer == null || !isContainer(selectedContainer)) {
	        showAlert("Error", "Please select a valid container to change the location.");
	        return;
	    }
	
	    TextInputDialog dialogX = new TextInputDialog("0");
	    dialogX.setTitle("Change Location");
	    dialogX.setHeaderText("Enter the X coordinate:");
	    Optional<String> resultX = dialogX.showAndWait();
	
	    TextInputDialog dialogY = new TextInputDialog("0");
	    dialogY.setTitle("Change Location");
	    dialogY.setHeaderText("Enter the Y coordinate:");
	    Optional<String> resultY = dialogY.showAndWait();
	
	    if (resultX.isPresent() && resultY.isPresent()) {
	        try {
	            double x = Double.parseDouble(resultX.get());
	            double y = Double.parseDouble(resultY.get());
	
	            Rectangle rect = visualizationMap.get(selectedContainer.getValue());
	            if (rect != null) {
	                rect.setLayoutX(x);
	                rect.setLayoutY(y);
	            }
	
	            Text label = labelMap.get(selectedContainer.getValue());
	            if (label != null) {
	                label.setX(x + rect.getWidth() / 2 - label.getBoundsInLocal().getWidth() / 2);
	                label.setY(y - 10);
	            }
	
	            System.out.println("Location for container set to: (" + x + ", " + y + ")");
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid coordinates. Please enter valid numbers.");
	        }
	    }
	}
	
	    
    
	// Method to change dimensions
	public void handleChangeDimensions() {
	    TreeItem<String> selectedItem = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedItem == null || !isItem(selectedItem)) {
	        showAlert("Error", "Please select a valid item to change dimensions.");
	        return;
	    }
	
	    TextInputDialog dialogLength = new TextInputDialog("50");
	    dialogLength.setTitle("Change Dimensions");
	    dialogLength.setHeaderText("Enter the length:");
	    Optional<String> resultLength = dialogLength.showAndWait();
	
	    TextInputDialog dialogWidth = new TextInputDialog("30");
	    dialogWidth.setTitle("Change Dimensions");
	    dialogWidth.setHeaderText("Enter the width:");
	    Optional<String> resultWidth = dialogWidth.showAndWait();
	
	    TextInputDialog dialogHeight = new TextInputDialog("20");
	    dialogHeight.setTitle("Change Dimensions");
	    dialogHeight.setHeaderText("Enter the height:");
	    Optional<String> resultHeight = dialogHeight.showAndWait();
	
	    if (resultLength.isPresent() && resultWidth.isPresent() && resultHeight.isPresent()) {
	        try {
	            double length = Double.parseDouble(resultLength.get());
	            double width = Double.parseDouble(resultWidth.get());
	            double height = Double.parseDouble(resultHeight.get());
	
	            Rectangle rect = visualizationMap.get(selectedItem.getValue());
	            if (rect != null) {
	                rect.setWidth(width);
	                rect.setHeight(height);
	            }
	
	            System.out.println("Dimensions for item set to: (" + length + ", " + width + ", " + height + ")");
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid dimensions. Please enter valid numbers.");
	        }
	    }
	}

    
	
	// Separate handler for changing container dimensions
	public void handleChangeDimensionsContainer() {
	    TreeItem<String> selectedContainer = itemsTreeView.getSelectionModel().getSelectedItem();
	
	    if (selectedContainer == null || !isContainer(selectedContainer)) {
	        showAlert("Error", "Please select a valid container to change dimensions.");
	        return;
	    }
	
	    TextInputDialog dialogLength = new TextInputDialog("100");
	    dialogLength.setTitle("Change Dimensions");
	    dialogLength.setHeaderText("Enter the length:");
	    Optional<String> resultLength = dialogLength.showAndWait();
	
	    TextInputDialog dialogWidth = new TextInputDialog("100");
	    dialogWidth.setTitle("Change Dimensions");
	    dialogWidth.setHeaderText("Enter the width:");
	    Optional<String> resultWidth = dialogWidth.showAndWait();
	
	    TextInputDialog dialogHeight = new TextInputDialog("50");
	    dialogHeight.setTitle("Change Dimensions");
	    dialogHeight.setHeaderText("Enter the height:");
	    Optional<String> resultHeight = dialogHeight.showAndWait();
	
	    if (resultLength.isPresent() && resultWidth.isPresent() && resultHeight.isPresent()) {
	        try {
	            double length = Double.parseDouble(resultLength.get());
	            double width = Double.parseDouble(resultWidth.get());
	            double height = Double.parseDouble(resultHeight.get());
	
	            Rectangle rect = visualizationMap.get(selectedContainer.getValue());
	            if (rect != null) {
	                rect.setWidth(width);
	                rect.setHeight(height);
	            }
	
	            System.out.println("Dimensions for container set to: (" + length + ", " + width + ", " + height + ")");
	        } catch (NumberFormatException e) {
	            showAlert("Error", "Invalid dimensions. Please enter valid numbers.");
	        }
	    }
	}
	
	    
	    
    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    private boolean isOverlapping(Rectangle newRect) {
        for (Rectangle existingRect : visualizationMap.values()) {
            if (existingRect.getBoundsInParent().intersects(newRect.getBoundsInParent())) {
                return true; // Overlap detected
            }
        }
        return false; // No overlap
    }
}

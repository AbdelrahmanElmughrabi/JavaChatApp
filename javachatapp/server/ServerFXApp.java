package javachatapp.server;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX frontend for the chat server.
 */
public class ServerFXApp extends Application {

    private HostServer serverGUI;

    private TextField portField;
    private Label statusLabel;
    private Label clientCountLabel;
    private Button startButton;
    private Button stopButton;

    private Timeline clientCountTimeline;

    @Override
    public void start(Stage primaryStage) {
        this.serverGUI = new HostServer();

        VBox root = buildLayout();

        Scene scene = new Scene(root, 420, 200);
        primaryStage.setTitle("Java Chat Server");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(380);
        primaryStage.setMinHeight(180);

        // Ensure server stops cleanly when window is closed
        primaryStage.setOnCloseRequest(event -> {
            stopServer();
            stopClientCountUpdater();
            Platform.exit();
        });

        primaryStage.show();
    }

    /**
     * Build the JavaFX layout for the server window.
     */
    private VBox buildLayout() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));

        // Port input row
        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER_LEFT);

        Label portLabel = new Label("Port:");
        portField = new TextField();
        portField.setPromptText("1024 - 65535");
        portField.setPrefColumnCount(10);

        HBox.setHgrow(portField, Priority.NEVER);
        portBox.getChildren().addAll(portLabel, portField);

        // Start / Stop buttons
        startButton = new Button("Start Server");
        stopButton = new Button("Stop Server");
        stopButton.setDisable(true); // cannot stop before we start

        startButton.setOnAction(e -> startServer());
        stopButton.setOnAction(e -> stopServer());

        HBox buttonBox = new HBox(10, startButton, stopButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Status labels
        statusLabel = new Label("Status: Stopped");
        clientCountLabel = new Label("Connected clients: 0");

        VBox infoBox = new VBox(5, statusLabel, clientCountLabel);

        root.getChildren().addAll(portBox, buttonBox, new Separator(), infoBox);
        return root;
    }

    /**
     * Attempt to start the server using the port in the text field.
     */
    private void startServer() {
        String portText = portField.getText() != null
                ? portField.getText().trim()
                : "";

        if (portText.isEmpty()) {
            showError("Invalid Port", "Please enter a port number.");
            return;
        }

        if (!HostServer.isValidPort(portText)) {
            showError("Invalid Port", "Port must be a number between 1024 and 65535.");
            return;
        }

        int port = Integer.parseInt(portText);

        boolean started = serverGUI.startServer(port);
        if (!started) {
            showError("Start Failed", "Could not start server on port " + port + ".\n"
                    + "The port might already be in use or unavailable.");
            return;
        }

        // Update UI state
        statusLabel.setText("Status: Running on port " + serverGUI.getCurrentPort());
        startButton.setDisable(true);
        portField.setDisable(true);
        stopButton.setDisable(false);

        startClientCountUpdater();

        // Show success message
        showInfo("Server Started", "Server successfully started on port " + port);
    }

    /**
     * Stop the server if it is running.
     */
    private void stopServer() {
        boolean wasRunning = false;
        if (serverGUI != null && serverGUI.isServerRunning()) {
            serverGUI.stopServer();
            wasRunning = true;
        }

        // Reset labels even if it wasn't running
        statusLabel.setText("Status: Stopped");
        clientCountLabel.setText("Connected clients: 0");

        startButton.setDisable(false);
        portField.setDisable(false);
        stopButton.setDisable(true);

        // Show confirmation if server was actually running
        if (wasRunning) {
            showInfo("Server Stopped", "Server has been stopped successfully.");
        }
    }

    /**
     * Periodically update the client count label while the server is running.
     * Uses a Timeline that fires every second.
     */
    private void startClientCountUpdater() {
        stopClientCountUpdater(); // avoid multiple timelines

        clientCountTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateClientCountLabel())
        );
        clientCountTimeline.setCycleCount(Timeline.INDEFINITE);
        clientCountTimeline.play();
    }

    /**
     * Stop the Timeline that updates the client count (if any).
     */
    private void stopClientCountUpdater() {
        if (clientCountTimeline != null) {
            clientCountTimeline.stop();
            clientCountTimeline = null;
        }
    }

    /**
     * Read the client count from the backend and update the label.
     */
    private void updateClientCountLabel() {
        if (serverGUI != null && serverGUI.isServerRunning()) {
            int clientCount = serverGUI.getClientCount();
            clientCountLabel.setText("Connected clients: " + clientCount);
        } else {
            clientCountLabel.setText("Connected clients: 0");
        }
    }

    /**
     * Show an error dialog with the given title and message.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an information dialog with the given title and message.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * JavaFX entry point.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

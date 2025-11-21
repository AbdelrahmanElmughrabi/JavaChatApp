package javachatapp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX frontend for the chat client.
 *
 * Responsibilities:
 * <ul>
 *     <li>Collect server address, port, and username from the user.</li>
 *     <li>Validate inputs using {@link ClientBackend} helper methods.</li>
 *     <li>Connect to the backend {@link ClientBackend}.</li>
 *     <li>Display the main chat UI (user list, chat history, message input).</li>
 *     <li>Update UI based on {@link ClientGUI.MessageHandler} callbacks.</li>
 * </ul>
 *
 * All networking and protocol logic is handled by {@link ClientBackend} and {@link ChatClient}.
 */
public class ClientFXApp extends Application implements ClientBackend.MessageHandler {

    private Stage primaryStage;

    // Scenes
    private Scene hostScene;
    private Scene portScene;
    private Scene usernameScene;
    private Scene chatScene;

    // Connection-flow controls
    private TextField hostField;
    private TextField portField;
    private TextField usernameField;

    // Chat UI controls
    private ListView<String> userListView;
    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private boolean manualDisconnect = false;


    // Backend
    private ClientBackend clientBackend;

    private String serverAddress;
    private int serverPort;
    private String username;
    private String lastErrorCode;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.clientBackend = new ClientBackend();

        this.hostScene = buildHostScene();
        this.portScene = buildPortScene();
        this.usernameScene = buildUsernameScene();
        this.chatScene = buildChatScene();

        primaryStage.setTitle("Java Chat Client - Connect");
        primaryStage.setScene(hostScene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);

        primaryStage.setOnCloseRequest(event -> {
            if (clientBackend != null && clientBackend.isConnected()) {
                clientBackend.disconnect();
            }
            Platform.exit();
        });

        primaryStage.show();
    }

    // ======================
    //  STEP 1: HOST SCENE
    // ======================

    private Scene buildHostScene() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Step 1 of 3: Server Address");
        Label infoLabel = new Label("Enter the server IP address or 'localhost'.");

        hostField = new TextField();
        hostField.setPromptText("e.g. localhost or 127.0.0.1");
        HBox.setHgrow(hostField, Priority.ALWAYS);

        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> handleHostNext());

        VBox.setMargin(nextButton, new Insets(10, 0, 0, 0));

        root.getChildren().addAll(titleLabel, infoLabel, hostField, nextButton);
        return new Scene(root, 420, 200);
    }

    private void handleHostNext() {
        String host = hostField.getText() != null ? hostField.getText().trim() : "";

        if (host.isEmpty()) {
            showError("Invalid Address", "Please enter the server address.");
            return;
        }

        if (!ClientBackend.isValidIP(host)) {
            showError("Invalid Address", "Please enter a valid IP address or 'localhost'.");
            return;
        }

        this.serverAddress = host;
        primaryStage.setScene(portScene);
        primaryStage.setTitle("Java Chat Client - Port");
    }

    // ======================
    //  STEP 2: PORT SCENE
    // ======================

    private Scene buildPortScene() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Step 2 of 3: Server Port");
        Label infoLabel = new Label("Enter the server port (1024 - 65535).");

        portField = new TextField();
        portField.setPromptText("e.g. 5000");
        portField.setPrefColumnCount(10);

        HBox fieldBox = new HBox(8, new Label("Port:"), portField);
        fieldBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Back");
        Button nextButton = new Button("Next");

        backButton.setOnAction(e -> {
            primaryStage.setScene(hostScene);
            primaryStage.setTitle("Java Chat Client - Connect");
        });
        nextButton.setOnAction(e -> handlePortNext());

        HBox buttonBox = new HBox(10, backButton, nextButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(titleLabel, infoLabel, fieldBox, buttonBox);
        return new Scene(root, 420, 200);
    }

    private void handlePortNext() {
        String portText = portField.getText() != null ? portField.getText().trim() : "";

        if (portText.isEmpty()) {
            showError("Invalid Port", "Please enter the server port.");
            return;
        }

        if (!ClientBackend.isValidPort(portText)) {
            showError("Invalid Port", "Port must be a number between 1024 and 65535.");
            return;
        }

        this.serverPort = Integer.parseInt(portText);
        primaryStage.setScene(usernameScene);
        primaryStage.setTitle("Java Chat Client - Username");
    }

    // ==========================
    //  STEP 3: USERNAME SCENE
    // ==========================

    private Scene buildUsernameScene() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Step 3 of 3: Username");
        Label infoLabel = new Label("Choose a username (max 20 characters).");

        usernameField = new TextField();
        usernameField.setPromptText("Your chat name");

        HBox fieldBox = new HBox(8, new Label("Username:"), usernameField);
        fieldBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Back");
        Button connectButton = new Button("Connect");

        backButton.setOnAction(e -> {
            primaryStage.setScene(portScene);
            primaryStage.setTitle("Java Chat Client - Port");
        });
        connectButton.setOnAction(e -> handleConnect());

        HBox buttonBox = new HBox(10, backButton, connectButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(titleLabel, infoLabel, fieldBox, buttonBox);
        return new Scene(root, 420, 200);
    }

    private void handleConnect() {
        String name = usernameField.getText() != null ? usernameField.getText().trim() : "";

        if (!ClientBackend.isValidUsername(name)) {
            showError("Invalid Username", "Username cannot be empty and must be at most 20 characters.");
            return;
        }

        this.username = name;
        lastErrorCode = null;

        boolean connected = clientBackend.connect(serverAddress, serverPort, username, this);

        if (!connected) {
            showError("Connection Failed", "Unable to connect to the server.\n"
                    + "Please check the address, port, and ensure the server is running.");
            return;
        }

        // Connected successfully – open main chat window
        primaryStage.setScene(chatScene);
        primaryStage.setTitle("Java Chat Client - " + username);
    }

    // ======================
    //  MAIN CHAT SCENE
    // ======================

    private Scene buildChatScene() {

    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));

    // LEFT: user list
    Label usersLabel = new Label("Users");
    userListView = new ListView<>();
    userListView.setPrefWidth(150);

    // Default Broadcast entry so user can send immediately
    userListView.getItems().setAll("Broadcast");
    userListView.getSelectionModel().select("Broadcast");

    VBox usersBox = new VBox(5, usersLabel, userListView);
    usersBox.setPrefWidth(180);
    VBox.setVgrow(userListView, Priority.ALWAYS);
    root.setLeft(usersBox);
    BorderPane.setMargin(usersBox, new Insets(0, 10, 0, 0));

    // CENTER: chat area
    Label chatLabel = new Label("Chat");
    chatArea = new TextArea();
    chatArea.setEditable(false);
    chatArea.setWrapText(true);

    VBox chatBox = new VBox(5, chatLabel, chatArea);
    VBox.setVgrow(chatArea, Priority.ALWAYS);
    root.setCenter(chatBox);

    // BOTTOM: message input + Send + Logout
    messageField = new TextField();
    messageField.setPromptText("Type a message and press Enter...");
    HBox.setHgrow(messageField, Priority.ALWAYS);

    sendButton = new Button("Send");
    sendButton.setOnAction(e -> handleSendMessage());

    // Enter key sends
    messageField.setOnAction(e -> handleSendMessage());

    Button logoutButton = new Button("Log Out");
    logoutButton.setOnAction(e -> handleLogout());

    HBox inputBox = new HBox(8);
    inputBox.setAlignment(Pos.CENTER_LEFT);
    inputBox.setPadding(new Insets(10, 0, 0, 0));

    // THIS is the critical part: three children in this *one* HBox
    inputBox.getChildren().addAll(messageField, sendButton, logoutButton);

    root.setBottom(inputBox);

    return new Scene(root, 700, 500);
}


    private void handleSendMessage() {
        if (clientBackend == null || !clientBackend.isConnected()) {
            showError("Not Connected", "You are not connected to any server.");
            return;
        }

        String text = messageField.getText() != null ? messageField.getText().trim() : "";
        if (text.isEmpty()) {
            return; // nothing to send
        }

        String recipient = userListView.getSelectionModel().getSelectedItem();

        if (recipient == null) {
            showError("No Recipient", "Please select a user or 'Broadcast' from the list.");
            return;
        }

        // Actually send via backend
        clientBackend.sendMessage(recipient, text);

        // Show our own message locally
        if ("Broadcast".equals(recipient)) {
            appendChatLine("Me (Broadcast): " + text);
        } else {
            appendChatLine("Me -> " + recipient + ": " + text);
        }

        messageField.clear();
    }

    private void handleLogout() {
        manualDisconnect = true;  // mark that this was intentional

        if (clientBackend != null && clientBackend.isConnected()) {
            clientBackend.disconnect();
        }

        // Reset UI
        chatArea.clear();
        userListView.getItems().setAll("Broadcast");
        userListView.getSelectionModel().select("Broadcast");

        primaryStage.setScene(hostScene);
        primaryStage.setTitle("Java Chat Client - Connect");
    }

    // ======================
    //  MessageHandler impl
    // ======================

    @Override
    public void onMessageReceived(String sender, String content) {
        // Called from network listener thread – wrap in Platform.runLater
        Platform.runLater(() -> appendChatLine(sender + ": " + content));
    }

    @Override
    public void onUserListUpdated(List<String> users) {
        // Called from network listener thread – wrap in Platform.runLater
        Platform.runLater(() -> {
            List<String> displayUsers = new ArrayList<>();
            displayUsers.add("Broadcast");

            for (String user : users) {
                // Optionally hide current user from list
                if (!user.equals(username)) {
                    displayUsers.add(user);
                }
            }

            userListView.getItems().setAll(displayUsers);

            // Ensure something is selected (default to Broadcast)
            if (userListView.getSelectionModel().getSelectedItem() == null) {
                userListView.getSelectionModel().select("Broadcast");
            }
        });
    }

    @Override
    public void onConnectionLost() {
        System.out.println("DEBUG: onConnectionLost() called in FX client");
        
        Platform.runLater(() -> {
            if (manualDisconnect) {
                // We disconnected on purpose – don't show scary popup
                manualDisconnect = false;
            } else {
                showError("Connection Lost", "Connection to the server was lost.");
            }

            if (clientBackend != null && clientBackend.isConnected()) {
                clientBackend.disconnect();
            }

            primaryStage.setScene(hostScene);
            primaryStage.setTitle("Java Chat Client - Connect");
            chatArea.clear();
            userListView.getItems().clear();
        });
    }
    
    public void onError(String errorMessage) {
        Platform.runLater(() -> {
            lastErrorCode = errorMessage;

            if ("USERNAME_TAKEN".equals(errorMessage)) {
                showError("Username In Use", "This username is already taken. Please choose a different one.");

                // Disconnect this temporary connection if still active
                if (clientBackend != null && clientBackend.isConnected()) {
                    clientBackend.disconnect();
                }

                // Return user to username scene (keep host/port filled)
                primaryStage.setScene(usernameScene);
                primaryStage.setTitle("Java Chat Client - Username");
            } else {
                // Generic error case
                showError("Error", errorMessage);
            }
        });
    }


    // ======================
    //  Helpers
    // ======================

    private void appendChatLine(String line) {
        if (chatArea == null) {
            // Should not happen once chatScene is active, but guard anyway
            System.out.println(line);
            return;
        }
        if (!chatArea.getText().isEmpty()) {
            chatArea.appendText("\n");
        }
        chatArea.appendText(line);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * JavaFX entry point for launching the client GUI.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

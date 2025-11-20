//@author [Your Name Here - Backend]
package JavaChatApp.client;

import JavaChatApp.shared.Message;
import JavaChatApp.shared.Message.MessageType;
import java.util.ArrayList;
import java.util.List;

/**
 * ClientGUI - Backend logic for client interface.
 * The frontend developer will add JavaFX UI components.
 *
 * Backend responsibilities:
 * - Connect/disconnect functionality
 * - Send messages to other users or broadcast
 * - Manage user list updates
 * - Handle incoming messages
 */
public class ClientGUI {
    private ChatClient client;
    private String username;
    private List<String> connectedUsers;
    private MessageHandler messageHandler;

    /**
     * Interface for handling UI updates (to be implemented by frontend)
     */
    public interface MessageHandler {
        void onMessageReceived(String sender, String content);
        void onUserListUpdated(List<String> users);
        void onConnectionLost();
    }

    public ClientGUI() {
        this.connectedUsers = new ArrayList<>();
    }

    /**
     * Connect to the server
     * @param serverAddress Server IP address
     * @param serverPort Server port
     * @param username Username for this client
     * @param handler Callback for UI updates
     * @return true if connection successful, false otherwise
     */
    public boolean connect(String serverAddress, int serverPort, String username, MessageHandler handler) {
        this.username = username;
        this.messageHandler = handler;

        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username cannot be empty");
            return false;
        }

        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            System.err.println("Server address cannot be empty");
            return false;
        }

        try {
            client = new ChatClient(serverAddress, serverPort);

            // Connect with message listener
            boolean success = client.connect(username, new ChatClient.MessageListener() {
                @Override
                public void onMessageReceived(Message message) {
                    handleIncomingMessage(message);
                }

                @Override
                public void onConnectionLost() {
                    if (messageHandler != null) {
                        messageHandler.onConnectionLost();
                    }
                }
            });

            return success;

        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle incoming messages from the server
     */
    private void handleIncomingMessage(Message message) {
        switch (message.getType()) {
            case TEXT:
            case PRIVATE_MESSAGE:
            case BROADCAST:
                // Notify UI of new message
                if (messageHandler != null) {
                    messageHandler.onMessageReceived(message.getSender(), message.getContent());
                }
                break;

            case USER_LIST:
                // Update connected users list
                updateUserList(message.getUserList());
                break;

            case DISCONNECT:
                // Server disconnected us
                if (messageHandler != null) {
                    messageHandler.onConnectionLost();
                }
                break;

            default:
                System.err.println("Unhandled message type: " + message.getType());
        }
    }

    /**
     * Update the list of connected users
     */
    private void updateUserList(String[] users) {
        connectedUsers.clear();
        if (users != null) {
            for (String user : users) {
                connectedUsers.add(user);
            }
        }

        // Notify UI of updated user list
        if (messageHandler != null) {
            messageHandler.onUserListUpdated(new ArrayList<>(connectedUsers));
        }
    }

    /**
     * Send a message to a specific user or broadcast
     * @param recipient Username of recipient (or "Broadcast" for all users)
     * @param content Message content
     */
    public void sendMessage(String recipient, String content) {
        if (client == null || !client.isConnected()) {
            System.err.println("Not connected to server");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            System.err.println("Message content cannot be empty");
            return;
        }

        if (recipient.equals("Broadcast")) {
            client.sendBroadcastMessage(content);
        } else {
            client.sendPrivateMessage(recipient, content);
        }
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
        connectedUsers.clear();
    }

    /**
     * Check if connected to server
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Get current username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get list of connected users
     */
    public List<String> getConnectedUsers() {
        return new ArrayList<>(connectedUsers);
    }

    /**
     * Validate IP address format
     * @param ip IP address string
     * @return true if valid format, false otherwise
     */
    public static boolean isValidIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // Accept "localhost" or standard IP format
        if (ip.equals("localhost")) {
            return true;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate port number
     * @param portString Port as string
     * @return true if valid port number, false otherwise
     */
    public static boolean isValidPort(String portString) {
        try {
            int port = Integer.parseInt(portString);
            return port >= 1024 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate username format
     * @param username Username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && username.length() <= 20;
    }
}

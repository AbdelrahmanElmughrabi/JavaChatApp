//@author [Your Name Here - Backend]
package javachatapp.client;

import javachatapp.shared.Message;
import javachatapp.shared.Message.MessageType;
import java.io.*;
import java.net.Socket;

/**
 * ChatClient handles the networking for a single client.
 * Connects to server, sends/receives messages via Java serialization.
 */
public class ChatClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private MessageListener messageListener;
    private boolean connected;
    private Thread listenerThread;

    /**
     * Interface for receiving messages from the server
     */
    public interface MessageListener {
        void onMessageReceived(Message message);
        void onConnectionLost();
    }

    public ChatClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connected = false;
    }

    /**
     * Connect to the server with the specified username
     * @param username The username for this client
     * @param listener Callback for receiving messages
     * @return true if connection successful, false otherwise
     */
    public boolean connect(String username, MessageListener listener) {
        this.username = username;
        this.messageListener = listener;

        try {
            // Connect to server
            socket = new Socket(serverAddress, serverPort);

            // Initialize streams (output first to avoid deadlock)
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Send CONNECT message with username
            Message connectMsg = new Message(MessageType.CONNECT, username);
            sendMessage(connectMsg);

            connected = true;

            // Start listening for messages in a separate thread
            startMessageListener();

            System.out.println("Connected to server as " + username);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * Start a thread to listen for incoming messages
     */
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    Message message = (Message) in.readObject();
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Connection closed by server");
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            } finally {
                if (connected && messageListener != null) {
                    messageListener.onConnectionLost();
                }
                disconnect();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Send a text message to a specific recipient
     * @param recipient Username of recipient (or "Broadcast" for all)
     * @param content The message content
     */
    public void sendTextMessage(String recipient, String content) {
        Message message = new Message(MessageType.TEXT, username, recipient, content);
        sendMessage(message);
    }

    /**
     * Send a private message to a specific user
     * @param recipient Username of recipient
     * @param content The message content
     */
    public void sendPrivateMessage(String recipient, String content) {
        Message message = new Message(MessageType.PRIVATE_MESSAGE, username, recipient, content);
        sendMessage(message);
    }

    /**
     * Send a broadcast message to all users
     * @param content The message content
     */
    public void sendBroadcastMessage(String content) {
        Message message = new Message(MessageType.BROADCAST, username, "Broadcast", content);
        sendMessage(message);
    }

    /**
     * Send a message object to the server
     * @param message The message to send
     */
    private void sendMessage(Message message) {
        try {
            if (out != null && connected) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            disconnect();
        }
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (connected) {
            try {
                // Send disconnect message
                if (out != null) {
                    Message disconnectMsg = new Message(MessageType.DISCONNECT, username);
                    out.writeObject(disconnectMsg);
                    out.flush();
                }
            } catch (IOException e) {
                // Ignore errors during disconnect
            }

            connected = false;

            // Close resources
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }

            System.out.println("Disconnected from server");
        }
    }

    /**
     * Check if client is connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get server address
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Get server port
     */
    public int getServerPort() {
        return serverPort;
    }
}

//@author [Your Name Here]
package JavaChatApp.server;

import JavaChatApp.shared.Message;
import JavaChatApp.shared.Message.MessageType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatServer handles multiple client connections and routes messages between them.
 * Uses thread-per-client model via ClientHandler.
 */
public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, ClientHandler> clients;
    private boolean running;

    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.running = false;
    }

    /**
     * Start the server and listen for client connections
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server started on port " + port);

            // Accept client connections in a loop
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getInetAddress());

                    // Create and start a new thread for this client
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    Thread clientThread = new Thread(handler);
                    clientThread.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Stop the server and close all connections
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Disconnect all clients
            for (ClientHandler handler : clients.values()) {
                handler.sendMessage(new Message(MessageType.DISCONNECT, "Server"));
            }
            clients.clear();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Add a client to the server's client list
     */
    public synchronized void addClient(String username, ClientHandler handler) {
        // Check if username already exists
        if (clients.containsKey(username)) {
            System.err.println("Username " + username + " already exists!");
            // You might want to handle this differently (e.g., reject connection)
            return;
        }
        clients.put(username, handler);
        System.out.println("Client added: " + username + " (Total: " + clients.size() + ")");
    }

    /**
     * Remove a client from the server's client list
     */
    public synchronized void removeClient(String username) {
        if (clients.remove(username) != null) {
            System.out.println("Client removed: " + username + " (Total: " + clients.size() + ")");
        }
    }

    /**
     * Route a message to the appropriate recipient(s)
     */
    public void routeMessage(Message message) {
        String recipient = message.getRecipient();

        if (recipient == null || recipient.equals("Broadcast")) {
            // Send to all clients
            broadcast(message);
        } else {
            // Send to specific client
            ClientHandler targetClient = clients.get(recipient);
            if (targetClient != null) {
                targetClient.sendMessage(message);
                System.out.println("Message routed from " + message.getSender() + " to " + recipient);
            } else {
                System.err.println("Recipient not found: " + recipient);
                // Optionally notify sender that recipient doesn't exist
            }
        }
    }

    /**
     * Broadcast a message to all connected clients
     */
    public void broadcast(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
        System.out.println("Broadcast message from " + message.getSender() + " to " + clients.size() + " clients");
    }

    /**
     * Send updated user list to all connected clients
     */
    public void broadcastUserList() {
        ArrayList<String> usernames = new ArrayList<>(clients.keySet());
        String[] userArray = usernames.toArray(new String[0]);

        Message userListMsg = new Message(MessageType.USER_LIST, userArray);
        broadcast(userListMsg);
        System.out.println("User list broadcast: " + usernames);
    }

    /**
     * Get the current port
     */
    public int getPort() {
        return port;
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get number of connected clients
     */
    public int getClientCount() {
        return clients.size();
    }
}

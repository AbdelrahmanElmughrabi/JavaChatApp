//@author [Your Name Here]
package javachatapp.server;

import javachatapp.shared.Message;
import javachatapp.shared.Message.MessageType;
import java.io.*;
import java.net.Socket;

/**
 * ClientHandler manages communication with a single connected client.
 * Each client gets its own thread via this Runnable implementation.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean cleanedUp = false;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Initialize streams (output first to avoid deadlock)
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Wait for client to send username via CONNECT message
            Message connectMsg = (Message) in.readObject();
            if (connectMsg.getType() == MessageType.CONNECT) {
                username = connectMsg.getSender();

                // Check if username is already taken
                if (server.isUsernameTaken(username)) {
                    System.err.println("Username " + username + " already exists! Rejecting connection.");
                    // Send error message to client
                    sendMessage(new Message(Message.MessageType.ERROR, "Server", username, "USERNAME_TAKEN"));
                    // Don't add client or broadcast user list - client will retry with different username
                    // Keep connection open for retry
                } else {
                    server.addClient(username, this);
                    System.out.println(username + " connected from " + socket.getInetAddress());

                    // Broadcast updated user list to all clients
                    server.broadcastUserList();

                    // Notify all users that someone joined
                    Message joinNotification = new Message(Message.MessageType.BROADCAST, "System", "Broadcast", username + " has joined the chat");
                    server.broadcast(joinNotification);
                }

                // Listen for messages from this client
                while (true) {
                    Message message = (Message) in.readObject();
                    handleMessage(message);
                }
            }
        } catch (EOFException e) {
            // Client disconnected normally
            System.out.println(username + " disconnected (EOF)");
        } catch (IOException e) {
            System.err.println("IO Error with client " + username + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Invalid message format from " + username);
        } finally {
            cleanup();
        }
    }

    /**
     * Handles incoming messages from the client
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case DISCONNECT:
                cleanup();
                break;

            case TEXT:
            case PRIVATE_MESSAGE:
                // Route message through server
                server.routeMessage(message);
                break;

            case BROADCAST:
                // Send to all connected clients
                server.broadcast(message);
                break;

            default:
                System.err.println("Unhandled message type: " + message.getType());
        }
    }

    /**
     * Sends a message to this client
     */
    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + username + ": " + e.getMessage());
            cleanup();
        }
    }

    /**
     * Cleanup resources and remove client from server
     */
    private void cleanup() {
        // Prevent duplicate cleanup (can be called multiple times)
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;

        try {
            if (username != null) {
                server.removeClient(username);

                // Notify all users that someone left
                Message leaveNotification = new Message(Message.MessageType.BROADCAST, "System", "Broadcast", username + " has left the chat");
                server.broadcast(leaveNotification);

                server.broadcastUserList();
                System.out.println(username + " removed from server");
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}

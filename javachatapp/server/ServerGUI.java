//@author [Your Name Here - Backend]
package javachatapp.server;

/**
 * ServerGUI - Backend logic for server interface.
 * The frontend developer will add JavaFX UI components.
 *
 * Backend responsibilities:
 * - Start/stop server functionality
 * - Port validation
 * - Server instance management
 */
public class ServerGUI {
    private ChatServer server;
    private int currentPort;

    /**
     * Start the server on the specified port
     * @param port The port number (should be 1024-65535)
     * @return true if started successfully, false otherwise
     */
    public boolean startServer(int port) {
        // Validate port range
        if (port < 1024 || port > 65535) {
            System.err.println("Invalid port. Must be between 1024 and 65535.");
            return false;
        }

        try {
            server = new ChatServer(port);
            currentPort = port;

            // Run server in a separate thread
            Thread serverThread = new Thread(() -> server.start());
            serverThread.setDaemon(true);
            serverThread.start();

            System.out.println("Server started successfully on port " + port);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stop the running server
     */
    public void stopServer() {
        if (server != null && server.isRunning()) {
            server.stop();
            System.out.println("Server stopped");
        }
    }

    /**
     * Check if server is currently running
     */
    public boolean isServerRunning() {
        return server != null && server.isRunning();
    }

    /**
     * Get current port number
     */
    public int getCurrentPort() {
        return currentPort;
    }

    /**
     * Get number of connected clients
     */
    public int getClientCount() {
        return server != null ? server.getClientCount() : 0;
    }

    /**
     * Validate port number
     * @param portString String to validate
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
}

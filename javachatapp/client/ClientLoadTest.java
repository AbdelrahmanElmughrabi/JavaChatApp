package javachatapp.client;

import java.util.ArrayList;
import java.util.List;

public class ClientLoadTest {

    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "localhost";
        int serverPort = 5000;
        int numClients = 100;

        List<ClientBackend> clients = new ArrayList<>();

        // Create dummy message handler
        ClientBackend.MessageHandler dummyHandler = new ClientBackend.MessageHandler() {
            @Override
            public void onMessageReceived(String sender, String content) {
                // Silent or print if needed
            }

            @Override
            public void onUserListUpdated(List<String> users) {
                // Silent
            }

            @Override
            public void onConnectionLost() {
                System.out.println("Connection lost");
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("Error: " + errorMessage);
            }
        };

        // Connect 100 clients
        for (int i = 0; i < numClients; i++) {
            ClientBackend client = new ClientBackend();
            String username = "TestUser" + i;

            boolean connected = client.connect(serverAddress, serverPort, username, dummyHandler);

            if (connected) {
                clients.add(client);
                System.out.println("Connected: " + username);
            } else {
                System.err.println("Failed to connect: " + username);
            }

            Thread.sleep(50); // Small delay to avoid overwhelming server
        }

        System.out.println("\nTotal connected: " + clients.size());

        // Keep alive for a bit
        Thread.sleep(100000);

        // Disconnect all
        for (ClientBackend client : clients) {
            client.disconnect();
        }
    }
}

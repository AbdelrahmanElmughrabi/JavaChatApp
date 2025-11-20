package javachatapp;
import javachatapp.server.ServerGUI;
import javachatapp.client.ClientGUI;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based launcher for testing the backend
 * @author Abdelrahman
 */
public class JavaChatApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Java Chat Application - Backend Test");
        System.out.println("1. Run as Server");
        System.out.println("2. Run as Client");
        System.out.print("Choose option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            runServer(scanner);
        } else if (choice == 2) {
            runClient(scanner);
        } else {
            System.out.println("Invalid option");
        }
    }

    private static void runServer(Scanner scanner) {
        ServerGUI serverGUI = new ServerGUI();

        System.out.print("Enter port number (1024-65535): ");
        String portString = scanner.nextLine();

        if (!ServerGUI.isValidPort(portString)) {
            System.out.println("Invalid port number");
            return;
        }

        int port = Integer.parseInt(portString);
        if (serverGUI.startServer(port)) {
            System.out.println("Server started successfully on port " + port);
            System.out.println("Press Enter to stop server...");
            scanner.nextLine();
            serverGUI.stopServer();
        } else {
            System.out.println("Failed to start server");
        }
    }

    private static void runClient(Scanner scanner) {
        ClientGUI clientGUI = new ClientGUI();

        System.out.print("Enter server address (e.g., localhost): ");
        String serverAddress = scanner.nextLine();

        System.out.print("Enter server port: ");
        String portString = scanner.nextLine();

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        if (!ClientGUI.isValidIP(serverAddress)) {
            System.out.println("Invalid server address");
            return;
        }

        if (!ClientGUI.isValidPort(portString)) {
            System.out.println("Invalid port number");
            return;
        }

        if (!ClientGUI.isValidUsername(username)) {
            System.out.println("Invalid username (max 20 characters)");
            return;
        }

        int port = Integer.parseInt(portString);
        boolean connected = clientGUI.connect(serverAddress, port, username, new ClientGUI.MessageHandler() {
            @Override
            public void onMessageReceived(String sender, String content) {
                System.out.println("\n[" + sender + "]: " + content);
                System.out.print("> ");
            }

            @Override
            public void onUserListUpdated(List<String> users) {
                System.out.println("\nConnected users: " + users);
                System.out.print("> ");
            }

            @Override
            public void onConnectionLost() {
                System.out.println("\nConnection lost!");
            }
        });

        if (connected) {
            System.out.println("Connected successfully as " + username);
            System.out.println("Commands: 'Broadcast <message>' or '<username> <message>' or 'exit'");

            while (clientGUI.isConnected()) {
                System.out.print("> ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) {
                    clientGUI.disconnect();
                    break;
                }

                if (input.trim().isEmpty()) {
                    continue;
                }

                String[] parts = input.split(" ", 2);
                if (parts.length < 2) {
                    System.out.println("Invalid format. Use: 'recipient message'");
                    continue;
                }

                String recipient = parts[0];
                String message = parts[1];
                clientGUI.sendMessage(recipient, message);
            }

            System.out.println("Disconnected");
        } else {
            System.out.println("Failed to connect to server");
        }
    }
}

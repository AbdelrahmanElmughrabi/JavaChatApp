# Java Chat Application

A multi-threaded client-server chat application built with Java and JavaFX, supporting real-time messaging, private messaging, and broadcast capabilities.

## Features

- **Real-time Communication**: Instant message delivery between connected clients
- **Broadcast Messaging**: Send messages to all connected users simultaneously
- **Private Messaging**: Direct one-to-one messaging between users
- **User Management**: Real-time user list updates showing all connected clients
- **Username Validation**: Prevents duplicate usernames and validates user input
- **JavaFX GUI**: Modern, user-friendly graphical interface for both client and server
- **Console Mode**: Alternative console-based interface for testing and debugging
- **Multi-threaded Server**: Handles multiple concurrent client connections efficiently
- **Connection Management**: Automatic notification when users join or leave

## Architecture

### Server Components

- **ChatServer**: Core server logic handling client connections and message routing
- **ClientHandler**: Manages individual client connections in separate threads
- **HostServer**: Backend logic for server management and port validation
- **ServerFXApp**: JavaFX-based server GUI with real-time client count display

### Client Components

- **ChatClient**: Networking layer handling server communication
- **ClientBackend**: Backend logic managing connection, messages, and user lists
- **ClientFXApp**: JavaFX-based client GUI with multi-step connection wizard
- **ClientLoadTest**: Stress testing utility for server performance

### Shared Components

- **Message**: Serializable message object supporting multiple message types (TEXT, BROADCAST, PRIVATE_MESSAGE, USER_LIST, CONNECT, DISCONNECT, ERROR)

## Requirements

- Java 8 or higher
- JavaFX (included in Java 8, separate module in Java 11+)
- NetBeans IDE (recommended) or any Java IDE

## Getting Started

### Running the Server

**Option 1: JavaFX GUI**
```bash
java javachatapp.server.ServerFXApp
```
1. Enter a port number (1024-65535)
2. Click "Start Server"
3. Monitor connected clients in real-time

**Option 2: Console Mode**
```bash
java javachatapp.JavaChatApp
```
1. Select option "1" for server
2. Enter desired port number

### Running the Client

**Option 1: JavaFX GUI**
```bash
java javachatapp.client.ClientFXApp
```
1. **Step 1**: Enter server address (e.g., `localhost` or IP address)
2. **Step 2**: Enter server port
3. **Step 3**: Choose a unique username (max 20 characters)
4. Start chatting!

**Option 2: Console Mode**
```bash
java javachatapp.JavaChatApp
```
1. Select option "2" for client
2. Enter server address, port, and username
3. Use commands:
   - `Broadcast <message>` - Send to all users
   - `<username> <message>` - Send private message
   - `exit` - Disconnect

### Load Testing

Test server performance with multiple concurrent connections:
```bash
java javachatapp.client.ClientLoadTest
```
Default: Connects 100 simulated clients to `localhost:5000`

## Usage

### Sending Messages

- **Broadcast**: Select "Broadcast" from the user list and type your message
- **Private Message**: Select a specific user from the list and type your message
- **Keyboard Shortcut**: Press Enter to send messages quickly

### User Interface

- **User List**: Displays all connected users (excluding yourself)
- **Chat Area**: Shows all received messages with sender identification
- **Message Field**: Type messages here
- **Send Button**: Click to send (or press Enter)
- **Log Out**: Disconnect from the server

## Technical Details

### Communication Protocol

- Uses Java Object Serialization for message transmission
- TCP/IP sockets for reliable client-server communication
- Thread-per-client model for handling multiple connections
- Concurrent collections for thread-safe client management

### Message Types

1. **CONNECT**: Client registration with username
2. **DISCONNECT**: Clean client disconnection
3. **TEXT**: Regular chat messages
4. **PRIVATE_MESSAGE**: Direct messages to specific users
5. **BROADCAST**: Messages to all users
6. **USER_LIST**: Server-sent user list updates
7. **ERROR**: Server error notifications (e.g., username taken)

### Port Requirements

- Valid port range: 1024-65535
- Ports below 1024 require administrator privileges
- Default testing port: 5000

### Security Features

- Input validation for usernames, ports, and IP addresses
- Username uniqueness enforcement
- Maximum username length: 20 characters
- Proper resource cleanup on disconnection

## Project Structure

```
JavaChatApp/
├── src/javachatapp/
│   ├── JavaChatApp.java          # Console launcher (runs without the GUI)
│   ├── client/
│   │   ├── ChatClient.java       # Client networking layer
│   │   ├── ClientBackend.java    # Client business logic
│   │   ├── ClientFXApp.java      # Client JavaFX GUI
│   │   └── ClientLoadTest.java   # Load testing utility
│   ├── server/
│   │   ├── ChatServer.java       # Server core logic
│   │   ├── ClientHandler.java    # Per-client thread handler
│   │   ├── HostServer.java       # Server backend
│   │   └── ServerFXApp.java      # Server JavaFX GUI
│   └── shared/
│       └── Message.java          # Message protocol
└── README.md
```

## Known Limitations

- No message history persistence (messages exist only in memory)
- No encryption (messages sent in plain text)
- No authentication beyond username uniqueness
- No file transfer support
- Server must be restarted to change port

## Future Enhancements

- Message encryption for secure communication
- Persistent message history with database integration
- User authentication and account management
- File transfer capabilities
- Typing indicators
- Message timestamps
- Emoji support
- Custom themes and UI customization
- Group chat rooms

## Troubleshooting

### "Port already in use"
- Ensure no other application is using the selected port
- Try a different port number
- Check that previous server instance was properly stopped

### "Connection refused"
- Verify server is running before connecting client
- Check firewall settings
- Confirm correct IP address and port number

### "Username already taken"
- Choose a different username
- Verify no other client is using the same name

### Client disconnects unexpectedly
- Check network stability
- Ensure server is still running
- Review console output for error messages

## Author

Abdelrahman Elmughrabi

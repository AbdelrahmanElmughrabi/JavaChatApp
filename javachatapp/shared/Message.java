//@author [Your Name Here]
package javachatapp.shared;

import java.io.Serializable;

/**
 * Serializable Message object for communication between client and server.
 * Supports different message types for various chat operations.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // Message types
    public enum MessageType {
        CONNECT,           // Client connecting with username
        DISCONNECT,        // Client disconnecting
        TEXT,              // Regular chat message
        USER_LIST,         // Server sending list of connected users
        PRIVATE_MESSAGE,   // Direct message to specific user
        BROADCAST          // Message to all users
    }

    private MessageType type;
    private String sender;
    private String recipient;  // null for broadcast, username for private
    private String content;
    private String[] userList; // For USER_LIST type

    // Constructor for text messages
    public Message(MessageType type, String sender, String recipient, String content) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    // Constructor for connection/disconnection
    public Message(MessageType type, String sender) {
        this.type = type;
        this.sender = sender;
    }

    // Constructor for user list updates
    public Message(MessageType type, String[] userList) {
        this.type = type;
        this.userList = userList;
    }

    // Getters
    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public String[] getUserList() {
        return userList;
    }

    // Setters
    public void setType(MessageType type) {
        this.type = type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserList(String[] userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

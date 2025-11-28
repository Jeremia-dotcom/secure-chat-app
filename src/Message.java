/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Contains message content, sender info, encryption status, and timestamp
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String content;
    private boolean encrypted;
    private LocalDateTime timestamp;
    
    //Creates a new message with current timestamp
    public Message(String sender, String content, boolean encrypted) {
        this.sender = sender;
        this.content = content;
        this.encrypted = encrypted;
        this.timestamp = LocalDateTime.now();
    }
    // Getter methods - provide read-only access to message properties
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isEncrypted() { return encrypted; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    //Used for display in chat User Interface and debugging
    @Override
    public String toString() {
        String time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return String.format("[%s] %s: %s", time, sender, content);
    }
}

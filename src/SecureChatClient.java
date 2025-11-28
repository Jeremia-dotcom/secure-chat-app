/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

//Handles client-side network communication for the chat application
public class SecureChatClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private EncryptionUtil encryptionUtil;
    private BlockingQueue<Message> incomingMessages;
    private volatile boolean connected;
    private Thread receiverThread;
    
    //Initializes client with username and prepares communication components
    public SecureChatClient(String username) {
        this.username = username;
        this.encryptionUtil = new EncryptionUtil();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.connected = false;
    }
    
    //Establishes connection to the chat server
    public boolean connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // Start message receiver thread
            startReceiverThread();
            
            // Send login message
            Message loginMessage = new Message(username, "connected", false);
            output.writeObject(loginMessage);
            
            System.out.println("Connected to server: " + serverAddress + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }
    
    //Starts background thread that continuously listens for incoming messages from server
    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            while (connected) {
                try {
                    Message message = (Message) input.readObject();
                    incomingMessages.put(message);
                } catch (IOException e) {
                    if (connected) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                    break;
                } catch (ClassNotFoundException | InterruptedException e) {
                    System.err.println("Error in receiver thread: " + e.getMessage());
                    break;
                }
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }
    
    //Sends a message to the chat server with optional encryption
    public void sendMessage(String content, boolean encrypt) {
        if (!connected) {
            System.err.println("Cannot send message - not connected to server");
            return;
        }
        
        try {
            String messageContent = encrypt ? encryptionUtil.encrypt(content) : content;
            Message message = new Message(username, messageContent, encrypt);
            output.writeObject(message);
            output.flush();
            
            System.out.println("Message sent (encrypted: " + encrypt + ")");
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    //Used by UI thread to check for new messages without freezing the interface
    public Message receiveMessage() {
        try {
            return incomingMessages.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    //Decrypts an encrypted message using the client's encryption utility
    public String decryptMessage(String encryptedContent) {
        return encryptionUtil.decrypt(encryptedContent);
    }
    
    //Closes all network connections and stops background threads
    public void disconnect() {
        connected = false;
        try {
            if (receiverThread != null && receiverThread.isAlive()) {
                receiverThread.interrupt();
            }
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    //Checks if client is currently connected to the server
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    public String getUsername() {
        return username;
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

//Manages client connections, message broadcasting, and real-time communication
public class ChatServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ExecutorService threadPool;
    private boolean isRunning;
    
    //Constructor - Initializes the chat server on specified port
    public ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = Collections.synchronizedList(new ArrayList<>());
            threadPool = Executors.newCachedThreadPool();
            isRunning = true;
            System.out.println("Chat server started on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    /*Starts the server main loop to accept client connections
    and Runs continuously until server is stopped*/
    
    public void start() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
                System.out.println("New client connected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    //Broadcasts a message to all connected clients except the sender
    public void broadcastMessage(Message message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }
    
    //Removes a client from the active clients list
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected");
    }
    
    //shuts down the server and closes all connections
    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
            threadPool.shutdown();
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.close();
                }
            }
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    //ClientHandler - Inner class to handle individual client connections
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private ChatServer server;
        private String username;
        
        //Sets up client connection streams
        public ClientHandler(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error creating client handler: " + e.getMessage());
            }
        }
        
        //Listens for incoming messages and handles client communication
        @Override
        public void run() {
            try {
                // First message should be username
                Message loginMessage = (Message) input.readObject();
                this.username = loginMessage.getSender();
                
                System.out.println("User logged in: " + username);
                
                while (true) {
                    Message message = (Message) input.readObject();
                    server.broadcastMessage(message, this);
                }
            } catch (EOFException e) {
                // Client disconnected normally
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Client connection error: " + e.getMessage());
            } finally {
                close();
                server.removeClient(this);
            }
        }
        
        //Sends a message to a specific client
        public void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error sending message to client: " + e.getMessage());
            }
        }
        //Closes all client connection resources
        public void close() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
    
    //Entry point for starting the chat server
    public static void main(String[] args) {
        ChatServer server = new ChatServer(12345);
        server.start();
    }
}

# Secure Chat Application

A real-time encrypted chat application built with Java Swing, featuring end-to-end AES encryption and multi-client support.

## 🔒 Features

- **End-to-End Encryption**: AES-128 encryption for secure messaging
- **Real-Time Communication**: Socket-based client-server architecture
- **Multi-Client Support**: Concurrent user handling with thread pools
- **Intuitive GUI**: Modern Swing interface with message bubbles
- **Encryption Toggle**: Choose to send encrypted or plain messages
- **Message Broadcasting**: Server broadcasts to all connected clients

## 🛠️ Tech Stack

- **Language**: Java (JDK 8+)
- **GUI**: Swing/AWT
- **Encryption**: AES-128 with Base64 encoding
- **Networking**: Java Sockets, ObjectInputStream/ObjectOutputStream
- **Concurrency**: ExecutorService, BlockingQueue

## 📋 Prerequisites

- Java Development Kit (JDK) 8 or higher
- IDE (NetBeans, IntelliJ IDEA, or Eclipse)

## 🚀 How to Run

### Start the Server
```bash
javac ChatServer.java Message.java
java ChatServer
```
Server will start on port 12345

### Start Client(s)
```bash
javac ChatFrame.java SecureChatClient.java EncryptionUtil.java Message.java
java ChatFrame
```

### Connect to Server
1. Enter username
2. Server address: `localhost` (or IP address)
3. Port: `12345`
4. Click "Connect"

## 🔐 How Encryption Works

1. Client checks "Encrypt" checkbox
2. Message is encrypted using AES-128 before sending
3. Encrypted message displays with 🔒 icon
4. Recipients click "Decrypt" to read encrypted messages
5. All clients share the same encryption key for interoperability

## 📸 Screenshots

### Main Chat Interface
![Chat Interface](screenshots/chat-interface.png)

### Encrypted Messages
![Encrypted Message](screenshots/encrypted-message-1.png)
![Encrypted Message](screenshots/encrypted-message-2.png)

## 🏗️ Architecture

### Components

**ChatServer.java**
- Manages client connections
- Broadcasts messages to all clients
- Handles multi-threading with ExecutorService

**ChatFrame.java**
- GUI implementation with Swing
- Message display with bubbles
- Connection and encryption controls

**SecureChatClient.java**
- Client-server communication
- Background message receiver thread
- Encryption/decryption interface

**EncryptionUtil.java**
- AES-128 encryption/decryption
- Base64 encoding for transmission
- Shared key management

**Message.java**
- Serializable message object
- Contains sender, content, timestamp, encryption status

## 🔧 Key Features Explained

### Real-Time Messaging
- Timer polls for new messages every 1 second
- Background thread continuously listens for incoming messages
- BlockingQueue ensures thread-safe message handling

### Message Bubbles
- Dynamic width based on viewport
- Color-coded: Green (own messages), White (others)
- Auto-scroll to latest message

### Encryption
- Fixed 16-byte AES key (shared across clients)
- Base64 encoding for safe transmission
- Lock icon indicates encrypted messages

## 🐛 Known Limitations

- Shared encryption key (not ideal for production)
- No user authentication
- Messages not persisted (in-memory only)
- Single server instance

## 🚀 Future Enhancements

- [ ] RSA key exchange for unique per-client encryption
- [ ] User authentication and registration
- [ ] Message history persistence (database)
- [ ] File sharing capability
- [ ] Voice/video chat integration
- [ ] Mobile app version

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

**Ntshwane Jeremia Mphorane**
- GitHub: [@jeremia-dotcom](https://github.com/jeremia-dotcom)
- Portfolio: [jerere's portfolio](https://main.d2k1aud08zsf7o.amplifyapp.com/))
- LinkedIn: [Your Name](https://linkedin.com/in/yourusername)

## 🙏 Acknowledgments

- Built as part of learning secure communication protocols
- Inspired by modern messaging applications
- Thanks to the Java community for excellent documentation

---

**Note**: This is an educational project. For production use, implement proper key exchange protocols (like Diffie-Hellman), user authentication, and secure key storage.

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.swing.SwingWorker;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
/**
 *
 * @author Liyap
 */
public class ChatFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ChatFrame.class.getName());

    // Client connection and messaging components
    private SecureChatClient client;
    private Timer messageTimer;
    private List<JPanel> messageBubbles = new ArrayList<>();
    
    //Creates new form ChatFrame
    public ChatFrame() {
    
        initComponents();
        forceVerticalLayout(); // Fix chat layout to prevent horizontal scrolling
        fixChatLayout(); // Apply chat styling and layout
        setupEventListeners(); // Set up button and resize listeners
        setupMessagePolling(); // Start checking for incoming messages
        fixChatWidth(); // Ensure proper width constraints
         messageBubbles = new ArrayList<>(); // Initialize message storage
        
    }
    //Overrides NetBeans-generated layout to ensure vertical message flow
    private void forceVerticalLayout() {
    jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
    
    // Force the scroll pane to never show horizontal scrollbar
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    // Set a maximum width constraint on the chat panel
    jPanel4.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    
    // Remove any horizontal expansion
    jPanel4.setAlignmentX(Component.LEFT_ALIGNMENT);
} 
    
   //Configures chat panel width constraints to prevent horizontal scrolling
   private void fixChatWidth() {
    // Completely disable horizontal scrolling
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // Force the viewport to match the scroll pane width
    jScrollPane1.getViewport().setPreferredSize(
        new Dimension(jScrollPane1.getWidth(), jScrollPane1.getViewport().getHeight())
    );
}
 //Applies visual styling and layout to the chat area
 private void fixChatLayout() {
    // Change the chat panel to vertical layout
    jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
    
    // Remove any horizontal scrolling
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    //  Add some padding and background color
    jPanel4.setBackground(new Color(240, 240, 240));
    jPanel4.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
}
   //Adds system messages (connection status, errors) to chat 
    private void addSystemMessage(String message) {
    SwingUtilities.invokeLater(() -> {
        JLabel systemLabel = new JLabel(message, JLabel.CENTER);
        systemLabel.setFont(systemLabel.getFont().deriveFont(Font.ITALIC));
        systemLabel.setForeground(Color.GRAY);
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        jPanel4.add(systemLabel);
        jPanel4.add(Box.createVerticalStrut(5));
        jPanel4.revalidate();
        jPanel4.repaint();
    });
}
//Decrypts all encrypted messages in the chat when decrypt button is clicked
private void decryptSelectedMessage() {
    if (client == null) {
        JOptionPane.showMessageDialog(this, "Not connected to server");
        return;
    }
    
    int decryptedCount = 0;
    StringBuilder debugInfo = new StringBuilder();
    
    
    // Scan all components in the chat panel
    for (Component comp : jPanel4.getComponents()) {
        if (comp instanceof JPanel) {
            JPanel bubbleContainer = (JPanel) comp;
            debugInfo.append("Found bubble container\n");
            
            // Look for message panels
            for (Component innerComp : bubbleContainer.getComponents()) {
                if (innerComp instanceof JPanel) {
                    JPanel messagePanel = (JPanel) innerComp;
                    Boolean isEncrypted = (Boolean) messagePanel.getClientProperty("isEncrypted");
                    
                    debugInfo.append("Checking panel - isEncrypted: " + isEncrypted + "\n");
                    
                    if (isEncrypted != null && isEncrypted) {
                        String encryptedContent = (String) messagePanel.getClientProperty("encryptedContent");
                        debugInfo.append("Encrypted content: " + encryptedContent + "\n");
                        
                        if (encryptedContent != null && !encryptedContent.isEmpty()) {
                            try {
                                debugInfo.append("Attempting decryption...\n");
                                String decrypted = client.decryptMessage(encryptedContent);
                                debugInfo.append("Decryption result: " + decrypted + "\n");
                                
                                if (!decrypted.contains("Cannot decrypt") && !decrypted.contains("Decryption Failed")) {
                                    // Success!
                                    SwingUtilities.invokeLater(() -> {
                                        updateMessageToDecrypted(messagePanel, decrypted);
                                    });
                                    decryptedCount++;
                                    debugInfo.append("SUCCESS: Decrypted message\n");
                                } else {
                                    debugInfo.append("FAILED: " + decrypted + "\n");
                                }
                                
                            } catch (Exception e) {
                                debugInfo.append("ERROR: " + e.getMessage() + "\n");
                            }
                        } else {
                            debugInfo.append("No encrypted content found\n");
                        }
                    }
                }
            }
        }
    }
    
    // Show debug info
    System.out.println(debugInfo.toString());
    
    // Show results to user
    if (decryptedCount > 0) {
        JOptionPane.showMessageDialog(this, 
            "Successfully decrypted " + decryptedCount + " message(s)!", 
            "Decryption Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, 
            "No messages were decrypted. Check console for details.\n\nDebug Info:\n" + debugInfo.toString(),
            "Decryption Failed", 
            JOptionPane.WARNING_MESSAGE);
    }
}
//Updates a message panel to show decrypted content
private void updateMessageToDecrypted(JPanel messagePanel, String decryptedText) {
    messagePanel.removeAll();
    messagePanel.setLayout(new BorderLayout(5, 2));
    messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setOpaque(false);

    JLabel senderLabel = new JLabel("You");
    senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 12f));
    senderLabel.setForeground(new Color(0, 100, 0));

    JLabel messageLabel = new JLabel("<html><body style='width: 250px; margin: 0; padding: 0;'>" + decryptedText + "</body></html>");
    messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 13f));

    JLabel decryptedLabel = new JLabel("🔓 Decrypted");
    decryptedLabel.setFont(decryptedLabel.getFont().deriveFont(Font.ITALIC, 10f));
    decryptedLabel.setForeground(Color.GREEN.darker());

    contentPanel.add(senderLabel);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(messageLabel);
    contentPanel.add(Box.createVerticalStrut(2));
    contentPanel.add(decryptedLabel);

    messagePanel.add(contentPanel, BorderLayout.CENTER);
    messagePanel.putClientProperty("isEncrypted", false);
    
    messagePanel.revalidate();
    messagePanel.repaint();
}
//Creates and displays a message bubble in the chat
private void addMessageBubble(String sender, String message, boolean isOwnMessage, boolean isEncrypted) {
    SwingUtilities.invokeLater(() -> {
        // Get the actual viewport width (this is the available space)
        int viewportWidth = jScrollPane1.getViewport().getWidth();
        int maxBubbleWidth = Math.max(viewportWidth - 250, 150); // Minimum 300px, maximum viewport width minus padding
        
        // Create main bubble container that fills available width
        JPanel bubbleContainer = new JPanel(new BorderLayout());
        bubbleContainer.setMaximumSize(new Dimension(viewportWidth, Integer.MAX_VALUE));
        bubbleContainer.setPreferredSize(new Dimension(viewportWidth, bubbleContainer.getPreferredSize().height));

        // Create message panel with strict width control
        JPanel messagePanel = new JPanel(new BorderLayout(5, 2));
        messagePanel.setMaximumSize(new Dimension(maxBubbleWidth, Integer.MAX_VALUE));
        messagePanel.setPreferredSize(new Dimension(maxBubbleWidth, messagePanel.getPreferredSize().height));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Set colors
        Color bgColor = isOwnMessage ? new Color(220, 248, 198) : new Color(255, 255, 255);
        messagePanel.setBackground(bgColor);
        messagePanel.setOpaque(true);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Create content panel with constrained width
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setMaximumSize(new Dimension(maxBubbleWidth - 40, Integer.MAX_VALUE));

        // Sender label
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 12f));
        senderLabel.setForeground(isOwnMessage ? new Color(0, 100, 0) : new Color(100, 0, 0));

        // Message label with strict width control
        String displayText = isEncrypted ? "🔒 [Encrypted Message]" : message;
        int textWidth = maxBubbleWidth - 50; // Account for padding
        
        JLabel messageLabel = new JLabel("<html><body style='width: " + textWidth + "px; margin: 0; padding: 0; word-wrap: break-word; overflow-wrap: break-word;'>" + displayText + "</body></html>");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 13f));

        contentPanel.add(senderLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(messageLabel);

        // Add encryption indicator if encrypted
        if (isEncrypted) {
            JLabel encryptLabel = new JLabel("Click Decrypt button to read");
            encryptLabel.setFont(encryptLabel.getFont().deriveFont(Font.ITALIC, 10f));
            encryptLabel.setForeground(Color.GRAY);
            contentPanel.add(Box.createVerticalStrut(2));
            contentPanel.add(encryptLabel);
            
            // Store encrypted content
            messagePanel.putClientProperty("encryptedContent", message);
            messagePanel.putClientProperty("isEncrypted", true);
        }

        messagePanel.add(contentPanel, BorderLayout.CENTER);

        // Add to bubble container with proper alignment
        if (isOwnMessage) {
            bubbleContainer.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            bubbleContainer.add(messagePanel, BorderLayout.EAST);
        } else {
            bubbleContainer.add(messagePanel, BorderLayout.WEST);
            bubbleContainer.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }

        // Add to chat panel
        jPanel4.add(bubbleContainer);
        jPanel4.add(Box.createVerticalStrut(8));
        
        // Force layout update
        jPanel4.revalidate();
        jPanel4.repaint();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = jScrollPane1.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    });
}
//Sets up timer to check for incoming messages from server
private void setupMessagePolling() {
    messageTimer = new Timer(1000, e -> {
        if (client != null && client.isConnected()) {
            Message receivedMessage = client.receiveMessage();
            if (receivedMessage != null) {
                String content = receivedMessage.getContent();
                String sender = receivedMessage.getSender();
                
                // Check if message is from another user
                if (!sender.equals(client.getUsername())) {
                    boolean isEncrypted = receivedMessage.isEncrypted();
                    String displayContent = isEncrypted ? "🔒 " + content : content;
                    // Display other user's message on LEFT side
                    addMessageBubble(sender, displayContent, false, isEncrypted);
                }
            }
        }
    });
    messageTimer.start();
}
//Sends a message to the chat server
private void sendMessage() {
    if (client == null || !client.isConnected()) {
        JOptionPane.showMessageDialog(this, "Not connected to server");
        return;
    }
    
    String message = jTextField4.getText().trim();
    if (message.isEmpty()) {
        return;
    }
    
    boolean encrypt = jCheckBox1.isSelected();
    client.sendMessage(message, encrypt);
    
    // Display own message on RIGHT side
    String displayMessage = encrypt ? "🔒 " + message : message;
    addMessageBubble("You", displayMessage, true, encrypt);
    
    jTextField4.setText("");
}
//Establishes connection to chat server
    private void connectToServer() {
    String username = jTextField1.getText().trim();
    String server = jTextField2.getText().trim();
    int port;
    
    try {
        port = Integer.parseInt(jTextField3.getText().trim());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter a valid port number");
        return;
    }
    
    if (username.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a username");
        return;
    }
    
    client = new SecureChatClient(username);
    
    SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
        @Override
        protected Boolean doInBackground() throws Exception {
            return client.connect(server, port);
        }
        
        @Override
        protected void done() {
            try {
                if (get()) {
                    jButton1.setEnabled(false);
                    jTextField1.setEnabled(false);
                    jTextField2.setEnabled(false);
                    jTextField3.setEnabled(false);
                    addSystemMessage("Connected to server as " + username);
                } else {
                    JOptionPane.showMessageDialog(ChatFrame.this, 
                        "Failed to connect to server. Make sure server is running.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(ChatFrame.this, 
                    "Connection error: " + e.getMessage());
            }
        }
    };
    worker.execute();
}
//Sets up all button click listeners and window resize handler
private void setupEventListeners() {
    // Connect button
    jButton1.addActionListener(e -> connectToServer());
    
    // Send button
    jButton3.addActionListener(e -> sendMessage());
    
    // Enter key in message field
    jTextField4.addActionListener(e -> sendMessage());
    
    // Decrypt button
    jButton2.addActionListener(e -> decryptSelectedMessage());
    addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            fixChatWidth();
            jPanel4.revalidate();
            jPanel4.repaint();
        }
    });
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jTextField4 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Username :");
        jPanel2.add(jLabel1);

        jTextField1.setText("*********");
        jPanel2.add(jTextField1);

        jLabel2.setText("Server :");
        jPanel2.add(jLabel2);

        jTextField2.setText("localhost");
        jPanel2.add(jTextField2);

        jLabel3.setText("port :");
        jPanel2.add(jLabel3);

        jTextField3.setText("12345");
        jPanel2.add(jTextField3);

        jButton1.setText("Connect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jCheckBox1.setText("Encrypt");

        jButton2.setText("Decrypt");

        jButton3.setText("Send");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addGap(0, 80, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextField4)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));
        jScrollPane1.setViewportView(jPanel4);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ChatFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}

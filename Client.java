// Client.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static String serverAddress = "localhost";
    private static final int PORT = 12345;
    private static String clientName;
    private static Socket socket;
    private static java.io.PrintWriter out;
    private static ClientGUI clientGUI;

    public static void main(String[] args) {
        // Get client name
        clientName = JOptionPane.showInputDialog("Enter your name:");
        if (clientName == null || clientName.trim().isEmpty()) {
            clientName = "Anonymous";
        }

        clientGUI = new ClientGUI(clientName);
        connectToServer();
    }

    private static void connectToServer() {
        try {
            socket = new Socket(serverAddress, PORT);
            out = new java.io.PrintWriter(socket.getOutputStream(), true);

            // Send client name first
            out.println(clientName);

            // Start thread to listen for messages from server
            new Thread(new IncomingReader()).start();

            clientGUI.appendSystemMessage("Connected to server as " + clientName);
        } catch (IOException e) {
            clientGUI.appendSystemMessage("Error connecting to server: " + e.getMessage());
            JOptionPane.showMessageDialog(clientGUI, "Cannot connect to server", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            clientGUI.appendMessage(clientName, message, false);
        }
    }

    static class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(socket.getInputStream()));

                String formattedMessage;
                while ((formattedMessage = in.readLine()) != null) {
                    String[] parts = formattedMessage.split("\\|", 3);
                    String sender = parts[0];
                    String message = parts[1];
                    clientGUI.appendMessage(sender, message, true);
                }
            } catch (IOException e) {
                clientGUI.appendSystemMessage("Error reading from server: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    clientGUI.appendSystemMessage("Error closing connection: " + e.getMessage());
                }
                clientGUI.appendSystemMessage("Disconnected from server");
                clientGUI.setConnected(false);
            }
        }
    }
}

class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JPanel inputPanel;
    private JLabel statusLabel;
    private boolean isConnected = true;

    public ClientGUI(String clientName) {
        setTitle("WhatsApp-like Chat - " + clientName);
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Status bar
        statusLabel = new JLabel(" Online ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 180, 0));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        add(statusPanel, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(new Color(229, 221, 213));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(new Color(229, 221, 213));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        sendButton = new JButton("Send");
        styleSendButton();

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Event listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void styleSendButton() {
        sendButton.setBackground(new Color(37, 211, 102)); // WhatsApp green
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        sendButton.setFocusPainted(false);
    }

    private void sendMessage() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            Client.sendMessage(message);
            inputField.setText("");
        }
    }

    public void appendMessage(String sender, String message, boolean isReceived) {
        SwingUtilities.invokeLater(() -> {
            if (sender.equals("Server")) {
                chatArea.append("\n[Server] " + message + "\n");
            } else {
                String displayName = isReceived ? sender : "You";
                Color bubbleColor = isReceived ? new Color(255, 255, 255) : new Color(220, 248, 198);

                // Create a styled message bubble
                JTextPane bubble = createMessageBubble(displayName, message, bubbleColor, isReceived);

                // Add to chat area (simplified version - in real app you'd use a JPanel with proper layout)
                chatArea.append("\n" + displayName + ": " + message + "\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private JTextPane createMessageBubble(String sender, String message, Color color, boolean isReceived) {
        JTextPane bubble = new JTextPane();
        bubble.setEditable(false);
        bubble.setBackground(color);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        bubble.setContentType("text/html");
        bubble.setText("<html><body style='margin:0;padding:0;'>" +
                "<b>" + sender + ":</b> " + message + "</body></html>");
        return bubble;
    }

    public void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("\n[System] " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
        statusLabel.setText(connected ? " Online" : " Offline");
        statusLabel.setBackground(connected ? new Color(0, 180, 0) : Color.RED);

        sendButton.setEnabled(connected);
        inputField.setEnabled(connected);
    }
}
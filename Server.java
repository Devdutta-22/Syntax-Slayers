// Server.java
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static ServerGUI serverGUI;

    public static void main(String[] args) {
        serverGUI = new ServerGUI();
        startServer();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverGUI.appendServerMessage("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverGUI);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            serverGUI.appendServerMessage("Server error: " + e.getMessage());
        }
    }

    public static void broadcastMessage(String senderName, String message, ClientHandler sender) {
        String formattedMessage = formatMessage(senderName, message, true);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(formattedMessage);
            }
        }
        serverGUI.appendChatMessage(formattedMessage);
    }

    public static String formatMessage(String senderName, String message, boolean isReceived) {
        return senderName + "|" + message + "|" + isReceived;
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        serverGUI.appendServerMessage(client.getClientName() + " disconnected. Total clients: " + clients.size());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ServerGUI serverGUI;
    private String clientName;
    private java.io.PrintWriter out;

    public ClientHandler(Socket socket, ServerGUI serverGUI) {
        this.socket = socket;
        this.serverGUI = serverGUI;
    }

    @Override
    public void run() {
        try {
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            out = new java.io.PrintWriter(socket.getOutputStream(), true);

            // First message is the client's name
            clientName = in.readLine();
            serverGUI.appendServerMessage(clientName + " has joined the chat");
            Server.broadcastMessage("Server", clientName + " joined the chat", this);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                Server.broadcastMessage(clientName, inputLine, this);
            }
        } catch (IOException e) {
            serverGUI.appendServerMessage("Error with client " + clientName + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                serverGUI.appendServerMessage("Error closing socket: " + e.getMessage());
            }
            Server.removeClient(this);
            Server.broadcastMessage("Server", clientName + " left the chat", this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }
}

class ServerGUI extends JFrame {
    private JTextArea serverLogArea;
    private JTextArea chatArea;
    private JTabbedPane tabbedPane;

    public ServerGUI() {
        setTitle("WhatsApp-like Chat Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Server log tab
        serverLogArea = new JTextArea();
        serverLogArea.setEditable(false);
        styleTextArea(serverLogArea);
        JScrollPane serverScrollPane = new JScrollPane(serverLogArea);
        tabbedPane.addTab("Server Log", serverScrollPane);

        // Chat tab
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        styleTextArea(chatArea);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        tabbedPane.addTab("Chat", chatScrollPane);

        add(tabbedPane);

        setVisible(true);
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(new Color(240, 242, 245));
        area.setMargin(new Insets(10, 10, 10, 10));
    }

    public void appendServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            serverLogArea.append("[SERVER] " + message + "\n");
            serverLogArea.setCaretPosition(serverLogArea.getDocument().getLength());
        });
    }

    public void appendChatMessage(String formattedMessage) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = formattedMessage.split("\\|", 3);
            String sender = parts[0];
            String message = parts[1];
            boolean isReceived = Boolean.parseBoolean(parts[2]);

            chatArea.append(formatMessageForDisplay(sender, message, isReceived) + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private String formatMessageForDisplay(String sender, String message, boolean isReceived) {
        if (sender.equals("Server")) {
            return "[" + sender + "]: " + message;
        }
        return (isReceived ? "[" + sender + "]: " : "[You]: ") + message;
    }
}
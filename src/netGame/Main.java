package netGame;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.LineBorder;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Create the main frame or 'window'
        JFrame frame = new JFrame("Tank Game");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Decide whether this client is the server or the client
        String[] options = {"Host Game", "Join Game"};
        int choice = JOptionPane.showOptionDialog(frame,
                "Do you want to host or join a game?",
                "Network Setup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        boolean isHost = (choice == JOptionPane.YES_OPTION);

        // Initialize the NetworkManager
        NetworkManager networkManager = new NetworkManager();

        if (isHost) {
            // Host Game
            networkManager.startServer(12345);
            JOptionPane.showMessageDialog(frame, "Hosting game... Waiting for opponent to join.");
        } else {
            // Join Game
            String serverIP = JOptionPane.showInputDialog(frame, "Enter server IP address:");
            try {
                networkManager.startClient(serverIP, 12345);
                JOptionPane.showMessageDialog(frame, "Connected to server.");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Network Error: " + e.getMessage());
                System.exit(1);
            }
        }

        // Create game and chat panels
        GamePanel gamePanel = new GamePanel(isHost);
        ChatPanel chatPanel = new ChatPanel();
        chatPanel.setBorder(new LineBorder(Color.BLACK));

        // Pass the GamePanel reference to the ChatPanel
        chatPanel.setGamePanel(gamePanel);

        // Add panels to the frame
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(chatPanel, BorderLayout.EAST);

        // Pass the NetworkManager to the panels
        gamePanel.setNetworkManager(networkManager);
        chatPanel.setNetworkManager(networkManager);

        // Set sizes and make frame visible
        frame.pack();
        frame.setVisible(true);
    }
}

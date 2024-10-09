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

        // Create game and chat panels
        GamePanel gamePanel = new GamePanel();
        ChatPanel chatPanel = new ChatPanel();
        chatPanel.setBorder(new LineBorder(Color.BLACK));

        // Pass the GamePanel reference to the ChatPanel
        chatPanel.setGamePanel(gamePanel);

        // Add panels to the frame
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(chatPanel, BorderLayout.EAST);

        // Initialize the NetworkManager
        NetworkManager networkManager = new NetworkManager();

        try {
            // Decide whether this client is the server or the client
            // For simplicity, you can use command-line arguments or a dialog box
            String[] options = {"Host Game", "Join Game"};
            int choice = JOptionPane.showOptionDialog(frame,
                    "Do you want to host or join a game?",
                    "Network Setup",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                // Host Game
                networkManager.startServer(12345);
            } else {
                // Join Game
                String serverIP = JOptionPane.showInputDialog(frame, "Enter server IP address:");
                networkManager.startClient(serverIP, 12345);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Network Error: " + e.getMessage());
            System.exit(1);
        }

        // Pass the NetworkManager to the panels
        gamePanel.setNetworkManager(networkManager);
        chatPanel.setNetworkManager(networkManager);

        // Set sizes and make frame visible
        frame.pack();
        frame.setVisible(true);
    }
}

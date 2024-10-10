package netGame;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ChatPanel extends JPanel {
    public JTextArea chatArea;
    public JScrollPane scrollPane;
    public JTextField chatInput;

    private GamePanel gamePanel;
    private NetworkManager networkManager;

    public ChatPanel() {
        // Creates chat components
        super(new BorderLayout());
        this.chatArea = new JTextArea(20, 50);
        chatArea.setEditable(false); // Player can't edit
        chatArea.setLineWrap(true);  // Wraps text to fit the area
        chatArea.setWrapStyleWord(true);
        this.scrollPane = new JScrollPane(this.chatArea);
        this.chatInput = new JTextField(50);

        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatInput.getText();
                if (!message.isEmpty()) {
                    chatArea.append("You: " + message + "\n");
                    chatInput.setText("");

                    // Send chat message
                    if (networkManager != null) {
                        ChatMessage chatMsg = new ChatMessage(message);
                        try {
                            networkManager.sendMessage(chatMsg);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    // Request focus back to the game panel
                    if (gamePanel != null) {
                        gamePanel.requestFocusInWindow();
                    }
                }
            }
        });

        // Adds components to chatPanel
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(chatInput, BorderLayout.SOUTH);
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;

        // Start listening for incoming chat messages
        new Thread(() -> {
            while (true) {
                try {
                    Object message = networkManager.receiveMessage();
                    if (message instanceof ChatMessage) {
                        ChatMessage chatMsg = (ChatMessage) message;
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append("Opponent: " + chatMsg.message + "\n");
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

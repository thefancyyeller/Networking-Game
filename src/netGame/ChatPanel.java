package netGame;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatPanel extends JPanel {
    public JTextArea chatArea;
    public JScrollPane scrollPane;
    public JTextField chatInput;
    private GamePanel gamePanel;

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

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
        this.add(new JLabel("Hello, world!"), BorderLayout.NORTH);
    }
    public void onMessageGet(String message){
        System.out.println("We just got a message: " + message);
    }
    public void callMeToSendAMessage(String message){
        System.out.println("You just sent a message: " + message);
    }
}

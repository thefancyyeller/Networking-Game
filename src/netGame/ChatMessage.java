package netGame;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public String message;

    public ChatMessage(String message) {
        this.message = message;
    }
}

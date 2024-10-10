package netGame;

import java.io.Serializable;

public class TankDestroyedMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public int tankId;

    public TankDestroyedMessage(int tankId) {
        this.tankId = tankId;
    }
}

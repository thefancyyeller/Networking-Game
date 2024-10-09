package netGame;

import java.io.Serializable;

public class BulletFiredMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public float x;
    public float y;
    public float angle;

    public BulletFiredMessage(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}

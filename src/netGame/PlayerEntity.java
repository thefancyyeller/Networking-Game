package netGame;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class PlayerEntity extends Entity{
    public HashSet<Integer> heldKeys = new HashSet<>();
    public float[] movementVec;// Specifically used to track forces applied to player from inputs
    public boolean isDestroyed = false;
    private BufferedImage sprite;
    private final String initialSpritePath;
    public PlayerEntity(String spritePath){
        this.initialSpritePath = spritePath;
        this.physVecs.add(new float[]{0,0});
        this.movementVec = this.physVecs.get(this.physVecs.size() - 1);
    }
    @Override
    public BufferedImage getSprite() {
        try{
            if(!isDestroyed)
                this.sprite = ImageIO.read(new File(initialSpritePath));
            else
                this.sprite = ImageIO.read(new File("./data/Bullet.png"));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return this.sprite;
    }
}

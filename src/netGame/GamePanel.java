package netGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class GamePanel extends JPanel{
    WorldContext ctx = new WorldContext();
    PlayerEntity player;
    long lastTick; // Helper var for physics engine

    int GAME_WIDTH = 571;
    int GAME_HEIGHT = 600;

    public GamePanel(){
        super();
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setPreferredSize(new Dimension(800, 600));
        // Spawn player
        this.ctx.tanks.add(new PlayerEntity("./data/greentank.png"));
        this.player = this.ctx.tanks.get(0);
        this.player.physVecs.add(new float[] {0,0});
        player.x = 100;
        player.y = 100;

        // Listen for inputs
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                player.heldKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                player.heldKeys.remove(e.getKeyCode());
            }
        });
        // Schedule physics engine to run every N seconds
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                physUpdate();
            }
        };
        // Schedule physics engine to run  10 microseconds
        Timer physTimer = new Timer(10, action);
        physTimer.start();
    }
    // Render the game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2d = (Graphics2D) g;
        // Render the tanks
        for (var entity : this.ctx.tanks) {
            BufferedImage sprite = entity.getSprite();
            double angle = Math.toRadians(entity.angle);

            // Scale the sprite
            BufferedImage scaledSprite = new BufferedImage(entity.width, entity.height, sprite.getType());
            Graphics2D g2dScaled = scaledSprite.createGraphics();
            g2dScaled.drawImage(sprite, 0, 0, entity.width, entity.height, null);
            g2dScaled.dispose();

            // New values after rotation occurs
            double sin = Math.abs(Math.sin(angle));
            double cos = Math.abs(Math.cos(angle));
            int newWidth = (int) Math.floor(entity.width * cos + entity.height * sin);
            int newHeight = (int) Math.floor(entity.height * cos + entity.width * sin);

            // Create rotated version of sprite
            BufferedImage rotated = new BufferedImage(newWidth, newHeight, sprite.getType());
            Graphics2D g2dRotated = rotated.createGraphics();
            AffineTransform rotation = new AffineTransform();
            rotation.rotate(angle, entity.width / 2.0, entity.height / 2.0);
            g2dRotated.drawRenderedImage(scaledSprite, rotation);

            // Draw the transformed image
            g2d.drawImage(rotated, (int) Math.floor(entity.x - (double) entity.width /2), (int) Math.floor(entity.y - (double) entity.height /2), null);
        }
        for(var bullet : this.ctx.bullets){
            BufferedImage sprite = bullet.getSprite();
            double angle = Math.toRadians(bullet.angle);

            // Scale the sprite
            BufferedImage scaledSprite = new BufferedImage(bullet.width, bullet.height, sprite.getType());
            Graphics2D g2dScaled = scaledSprite.createGraphics();
            g2dScaled.drawImage(sprite, 0, 0, bullet.width, bullet.height, null);
            g2dScaled.dispose();

            // New values after rotation occurs
            double sin = Math.abs(Math.sin(angle));
            double cos = Math.abs(Math.cos(angle));
            int newWidth = (int) Math.floor(bullet.width * cos + bullet.height * sin);
            int newHeight = (int) Math.floor(bullet.height * cos + bullet.width * sin);

            // Create rotated version of sprite
            BufferedImage rotated = new BufferedImage(newWidth, newHeight, sprite.getType());
            Graphics2D g2dRotated = rotated.createGraphics();
            AffineTransform rotation = new AffineTransform();
            rotation.rotate(angle, bullet.width / 2.0, bullet.height / 2.0);
            g2dRotated.drawRenderedImage(scaledSprite, rotation);

            // Draw the transformed image
            g2d.drawImage(rotated, (int) Math.floor(bullet.x - (double) bullet.width /2), (int) Math.floor(bullet.y - (double) bullet.height /2), null);
        }
        // Draw the border
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
    }

    private void physUpdate(){
        //Process player inputs
        for(var player : ctx.tanks){
            var angle = Math.toRadians(player.angle);
            double playerSpeed = 1.2;

            for(Integer key : player.heldKeys){
                if(key == KeyEvent.VK_RIGHT) {
                    player.rotationVec = 1.0f;
                }
                if(key == KeyEvent.VK_LEFT) {
                    player.rotationVec = -1.0f;
                }
                if(key == KeyEvent.VK_UP){
                    player.movementVec[0] = (float) (playerSpeed);
                    player.movementVec[1] = (float) (playerSpeed * -1);
                }
                if(key == KeyEvent.VK_DOWN){
                    player.movementVec[0] = (float) (playerSpeed * -1);
                    player.movementVec[1] = (float) (playerSpeed);
                }
                if(key == KeyEvent.VK_SPACE){
                    // Spawn a bullet
                    ctx.bullets.add(new BulletEntity());
                    var bullet = ctx.bullets.get(ctx.bullets.size()-1);
                    bullet.x = player.x;
                    bullet.y = player.y;
                    bullet.angle = player.angle;
                    var bulletSpeed = 10;
                    bullet.physVecs.add(new float[] {(float)bulletSpeed,(float)-1 * bulletSpeed});
                }
            }

        }
        if(this.lastTick == 0){
            this.lastTick = System.currentTimeMillis();
        }
        // Physics engine...
        double FRICTION_COEFFICIENT = 0.95;
        double STOP_POINT = 0.6;
        // Push objects...
        long curTime = System.currentTimeMillis();
        long delta = curTime - lastTick;
        lastTick = curTime;
        float moveAmount = (float) delta/6;

        for (int i = this.ctx.tanks.size() - 1 + this.ctx.bullets.size(); i >= 0; i--){
            Entity entity;
            if(i < ctx.tanks.size()){
                entity = ctx.tanks.get(i);
            }
            else{
                entity = ctx.bullets.get(i - ctx.tanks.size());
            }
            var sin = Math.sin(Math.toRadians(entity.angle));
            var cos = Math.cos(Math.toRadians(entity.angle));
            if(entity.rotationVec != 0){
                entity.angle += moveAmount * entity.rotationVec;
                entity.rotationVec *= (float) FRICTION_COEFFICIENT;
                if(Math.abs(entity.rotationVec) < STOP_POINT )
                    entity.rotationVec = 0;
                entity.angle %= 360;
            }
            // Process all phys vectors on each entity
            for (int j = entity.physVecs.size() - 1; j >= 0; j--) {
                float[] vec = entity.physVecs.get(j).clone();
                entity.x += (float) (moveAmount * vec[0] * sin);
                entity.y += (float) (moveAmount * vec[1] * cos);
                float magnitude = Math.abs(vec[0]) + Math.abs(vec[1]);
                entity.physVecs.get(j)[0] *= (float) FRICTION_COEFFICIENT;
                entity.physVecs.get(j)[1] *= (float) FRICTION_COEFFICIENT;
                if(magnitude < STOP_POINT){
                    if(entity instanceof PlayerEntity){
                        if(entity.physVecs.get(j) == ((PlayerEntity)entity).movementVec){
                            continue;
                        }
                    }
                    entity.physVecs.remove(j);
                }
            }
            // Clamp position within the map
            if(entity.x < 0 || entity.x > GAME_WIDTH || entity.y < 0 || entity.y > GAME_HEIGHT){
                if(entity instanceof BulletEntity){
                    for(int j = 0; j < entity.physVecs.size(); j++){
                        var vec = entity.physVecs.get(j);
                    }
                    entity.angle = (entity.angle + 270) % 360;
                }
                if(entity instanceof PlayerEntity){
                    entity.x = Math.max(entity.x, 0);
                    entity.x = Math.min(entity.x, GAME_WIDTH);
                    entity.y = Math.max(entity.y, 0);
                    entity.y = Math.min(entity.y, GAME_HEIGHT);
                }
            }
            // TODO: Check for collisions
            // Bullet Cleanup
            if(entity instanceof BulletEntity && (entity.physVecs.isEmpty())){
                ctx.bullets.remove(entity);
            }
        }
        super.repaint();
    }
}

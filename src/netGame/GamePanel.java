package netGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GamePanel extends JPanel {
    WorldContext ctx = new WorldContext();
    PlayerEntity ctrlPlayer;
    long lastTick; // Helper variable for physics engine

    int GAME_WIDTH = 571;
    int GAME_HEIGHT = 600;

    private NetworkManager networkManager;
    private boolean isHost;
    private long lastBullet = 0;
    private long mpb = 5000;

    public GamePanel(boolean isHost) {
        super();
        this.isHost = isHost;
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setPreferredSize(new Dimension(800, 600));

        // Initialize players
        PlayerEntity player = new PlayerEntity("./data/greentank.png");
        ctx.tanks.add(player);
        player.physVecs.add(new float[]{0, 0});
        player.x = 100;
        player.y = 100;

        // Initialize opponent player (will be updated upon receiving data)
        PlayerEntity opponentPlayer = new PlayerEntity("./data/redtank.png");
        ctx.opponentPlayer = opponentPlayer;
        ctx.tanks.add(opponentPlayer);

        ctx.player = player;
        ctx.opponentPlayer = opponentPlayer;

        if(isHost) {
            ctrlPlayer = player;
        }
        else{
            ctrlPlayer = opponentPlayer;
        }

        // Listen for inputs
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ctrlPlayer.heldKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                ctrlPlayer.heldKeys.remove(e.getKeyCode());
            }
        });

        // Schedule physics engine to run every 10 milliseconds
        Timer physTimer = new Timer(10, e -> physUpdate());
        physTimer.start();
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;

        // Start listening for incoming messages
        new Thread(() -> {
            while (true) {
                try {
                    Object message = networkManager.receiveMessage();
                    if (message instanceof GameStateUpdate) {
                        // Process entity moves
                        GameStateUpdate update = (GameStateUpdate) message;
                        PlayerEntity updatedPlayer;
                        if(isHost){
                            updatedPlayer = ctx.opponentPlayer;
                        }
                        else
                            updatedPlayer = ctx.player;
                        updatedPlayer.x = update.x;
                        updatedPlayer.y = update.y;
                        updatedPlayer.angle = update.angle;
                    }
                    if (message instanceof BulletFiredMessage) {
                        BulletFiredMessage bulletMsg = (BulletFiredMessage) message;
                        BulletEntity bullet = new BulletEntity();
                        ctx.bullets.add(bullet);
                        bullet.x = bulletMsg.x;
                        bullet.y = bulletMsg.y;
                        bullet.angle = bulletMsg.angle;
                        var bulletSpeed = 10;
                        bullet.physVecs.add(new float[] {(float)bulletSpeed,(float)-1 * bulletSpeed});
                        repaint();
                        System.out.println("Bullet message recieved");
                    }
                    if (message instanceof TankDestroyedMessage) {
                        if(isHost)
                            ctx.opponentPlayer.isDestroyed = true;
                        else
                            ctx.player.isDestroyed = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleTankDestroyed(TankDestroyedMessage msg) {
        if(isHost){
            ctx.opponentPlayer.isDestroyed = true;
        }
        else
            ctx.player.isDestroyed = true;
        repaint();
    }



    // Render the game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2d = (Graphics2D) g;

        // Render the tanks
        for (var entity : this.ctx.tanks) {
            if (entity.isDestroyed) continue;
            BufferedImage sprite = entity.getSprite();
            double angle = Math.toRadians(entity.angle);

            // Scale the sprite
            BufferedImage scaledSprite = new BufferedImage(entity.width, entity.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2dScaled = scaledSprite.createGraphics();
            g2dScaled.drawImage(sprite, 0, 0, entity.width, entity.height, null);
            g2dScaled.dispose();

            // Rotate the sprite
            AffineTransform transform = new AffineTransform();
            transform.translate(entity.x - entity.width / 2.0, entity.y - entity.height / 2.0);
            transform.rotate(angle, entity.width / 2.0, entity.height / 2.0);

            g2d.drawImage(scaledSprite, transform, null);
        }

        // Render the bullets
        for (var bullet : this.ctx.bullets) {
            BufferedImage sprite = bullet.getSprite();
            double angle = Math.toRadians(bullet.angle);

            // Scale the sprite
            BufferedImage scaledSprite = new BufferedImage(bullet.width, bullet.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2dScaled = scaledSprite.createGraphics();
            g2dScaled.drawImage(sprite, 0, 0, bullet.width, bullet.height, null);
            g2dScaled.dispose();

            // Rotate the sprite
            AffineTransform transform = new AffineTransform();
            transform.translate(bullet.x - bullet.width / 2.0, bullet.y - bullet.height / 2.0);
            transform.rotate(angle, bullet.width / 2.0, bullet.height / 2.0);

            g2d.drawImage(scaledSprite, transform, null);
        }

        // Draw the border
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
    }

    private void physUpdate() {
        // Process player inputs
        for (var player : ctx.tanks) {
            var angle = Math.toRadians(player.angle);
            double playerSpeed = 1.2;

            for (Integer key : player.heldKeys) {
                if (player.isDestroyed) continue;
                if(player != ctrlPlayer) continue;

                if (key == KeyEvent.VK_RIGHT) {
                    ctrlPlayer.rotationVec = 1.0f;
                }
                if (key == KeyEvent.VK_LEFT) {
                    ctrlPlayer.rotationVec = -1.0f;
                }
                if (key == KeyEvent.VK_UP) {
                    ctrlPlayer.movementVec[0] = (float) (playerSpeed);
                    ctrlPlayer.movementVec[1] = (float) (playerSpeed * -1);
                }
                if (key == KeyEvent.VK_DOWN) {
                    ctrlPlayer.movementVec[0] = (float) (playerSpeed * -1);
                    ctrlPlayer.movementVec[1] = (float) (playerSpeed);
                }
                if (key == KeyEvent.VK_SPACE) {
                    if(System.currentTimeMillis() > (lastBullet+mpb)){
                        lastBullet = System.currentTimeMillis();
                        // Spawn Player bullet
                        ctx.bullets.add(new BulletEntity());
                        var bullet = ctx.bullets.get(ctx.bullets.size()-1);
                        bullet.x = player.x;
                        bullet.y = player.y;
                        bullet.angle = player.angle;
                        var bulletSpeed = 10;
                        bullet.physVecs.add(new float[] {(float)bulletSpeed,(float)-1 * bulletSpeed});
                        // Send msg
                        BulletFiredMessage msg = new BulletFiredMessage(bullet.x, bullet.y, bullet.angle);
                        try {
                            networkManager.sendMessage(msg);
                        }
                        catch(Exception e){
                            ctx.bullets.remove(bullet);
                            System.out.println("Failed to send bullet message.");
                        }
                        repaint();
                    }
                }
            }
        }

        if (this.lastTick == 0) {
            this.lastTick = System.currentTimeMillis();
        }

        // Physics engine...
        double FRICTION_COEFFICIENT = 0.95;
        double STOP_POINT = 0.6;
        // Push objects...
        long curTime = System.currentTimeMillis();
        long delta = curTime - lastTick;
        lastTick = curTime;
        float moveAmount = (float) delta / 6;

        for (int i = this.ctx.tanks.size() - 1 + this.ctx.bullets.size(); i >= 0; i--) {
            Entity entity;
            if (i < ctx.tanks.size()) {
                entity = ctx.tanks.get(i);
            } else {
                entity = ctx.bullets.get(i - ctx.tanks.size());
            }
            var sin = Math.sin(Math.toRadians(entity.angle));
            var cos = Math.cos(Math.toRadians(entity.angle));
            if (entity.rotationVec != 0) {
                entity.angle += moveAmount * entity.rotationVec;
                entity.rotationVec *= (float) FRICTION_COEFFICIENT;
                if (Math.abs(entity.rotationVec) < STOP_POINT)
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
                if (magnitude < STOP_POINT) {
                    if (entity instanceof PlayerEntity) {
                        if (entity.physVecs.get(j) == ((PlayerEntity) entity).movementVec) {
                            continue;
                        }
                    }
                    entity.physVecs.remove(j);
                }
            }
            // Clamp position within the map
            if (entity.x < 0 || entity.x > GAME_WIDTH || entity.y < 0 || entity.y > GAME_HEIGHT) {
                if (entity instanceof BulletEntity) {
                    // Remove bullet if it goes out of bounds
                    ctx.bullets.remove(entity);
                }
                if (entity instanceof PlayerEntity) {
                    entity.x = Math.max(entity.x, 0);
                    entity.x = Math.min(entity.x, GAME_WIDTH);
                    entity.y = Math.max(entity.y, 0);
                    entity.y = Math.min(entity.y, GAME_HEIGHT);
                }
            }
            // Check for collisions
            if (entity instanceof BulletEntity) {
                for (var tank : ctx.tanks) {
                    if (!tank.isDestroyed && entity.checkCollision(tank)) {
                        tank.isDestroyed = true;
                    }
                }
            }

        }

        // Send player's state to opponent
        if (networkManager != null && networkManager.out != null) {
            GameStateUpdate update = new GameStateUpdate(ctrlPlayer.x, ctrlPlayer.y, ctrlPlayer.angle);
            try {
                networkManager.sendMessage(update);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        repaint();
    }
}

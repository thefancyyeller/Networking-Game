package netGame;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class NetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private CopyOnWriteArrayList<Consumer<Object>> listeners = new CopyOnWriteArrayList<>();

    public void startServer(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Server started. Waiting for a client to connect...");
                socket = serverSocket.accept();
                System.out.println("Client connected.");
                initializeStreams();
                startListening();

                // Inform the user that the client has connected
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Opponent has joined the game!");
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Connected to server.");
        initializeStreams();
        startListening();
    }

    public void addMessageListener(Consumer<Object> listener) {
        listeners.add(listener);
    }

    private void initializeStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }


    private void startListening() {
        new Thread(() -> {
            try {
                Object message;
                while ((message = in.readObject()) != null) {
                    System.out.println("Received message from input stream: " + message.getClass().getSimpleName() + " - " + message.toString());
                    for (Consumer<Object> listener : listeners) {
                        listener.accept(message);
                    }
                   // messageQueue.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("Sent message: " + message.getClass().getSimpleName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // public Object receiveMessage() throws InterruptedException {
    //    Object message = messageQueue.take();
     //   System.out.println("Received message: " + message.getClass().getSimpleName() + " - " + message.toString());
    //    return message;
    //}

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}

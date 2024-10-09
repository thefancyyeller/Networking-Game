package netGame;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    public void startServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        initializeStreams();
        startListening();
    }

    public void startClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        initializeStreams();
        startListening();
    }

    private void initializeStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void startListening() {
        new Thread(() -> {
            try {
                Object message;
                while ((message = in.readObject()) != null) {
                    messageQueue.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(Object message) throws IOException {
        out.writeObject(message);
    }

    public Object receiveMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}

package HW1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Server {

    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    public static final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                handleConnection(socket);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleConnection(Socket socket) {
        ClientHandler clientHandler = new ClientHandler(socket);
        threadPool.submit(clientHandler);
    }


    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }


}


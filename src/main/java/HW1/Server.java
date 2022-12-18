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

    private ServerSocket serverSocket;
    private ExecutorService threadPool = Executors.newFixedThreadPool(64);
    public static final Map<String, Map<String, Handler>> handlers = new HashMap<>();

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


    public void addHandler(String get, String s, Handler handler) {
        System.out.println(s);
        Map<String, Handler> handlerMap = new HashMap<>();
        handlerMap.put(s, handler);
        handlers.put(get, handlerMap);
    }


}


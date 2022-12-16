package HW1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {

    private ServerSocket serverSocket;
    private  ExecutorService threadPool = Executors.newFixedThreadPool(64);

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

    private void handleConnection(Socket socket){
        ClientHandler clientHandler = new ClientHandler(socket);
        threadPool.submit(clientHandler, "OK");
    }


}


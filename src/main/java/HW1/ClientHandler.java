package HW1;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedOutputStream out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.in = createReader();
        this.out = createOutputStream();
    }

    private BufferedReader createReader() {
        try {
            return new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedOutputStream createOutputStream() {
        try {
            return new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            try {
                final String requestLine = in.readLine();

                if (requestLine == null) {
                    continue;
                }

                final String[] parts = requestLine.split(" ");

                System.out.println(requestLine);

                if (parts.length != 3) {
                    // just close socket
                    continue;
                }

                Request request = new Request(parts[0], parts[1]);

                synchronized (Server.handlers) {
                    try {
                        Server.handlers.get(request.getMethod()).get("/messages").handle(request, out);
                    } catch (NullPointerException ex) {
                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Content-Length: 0\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


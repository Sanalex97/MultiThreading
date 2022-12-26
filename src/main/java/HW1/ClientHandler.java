package HW1;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        createHttpClient();
        this.in = createReader();
        this.out = createOutputStream();
    }

    private static void createHttpClient() {

    }

    private BufferedInputStream createReader() {
        try {
            return new BufferedInputStream(clientSocket.getInputStream());
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
                int limit = 4096;

                in.mark(limit);
                byte[] buffer = new byte[limit];
                int read = in.read(buffer);

                // ищем request line
                byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
                int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

                if (requestLineEnd == -1) {
                    badRequest(out);
                    continue;
                }

                // читаем request line
                String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

                String method = requestLine[0];
                String path = requestLine[1].split("\\?")[0];

                String paramQueryString = requestLine[1].split("\\?")[1];


                if (!path.startsWith("/")) {
                    badRequest(out);
                    continue;
                }

                HashMap<String, List<String>> queryParams = new HashMap<>();
                List<NameValuePair> queryStringParams = URLEncodedUtils.parse(paramQueryString, StandardCharsets.UTF_8);
                for (NameValuePair queryStringParam : queryStringParams) {

                    String[] arrQueryParams = queryStringParam.toString().split("=");

                    List<String> listValue = queryParams.get(arrQueryParams[0]);

                    if (listValue == null) {
                        listValue = new ArrayList<>();
                    }
                    listValue.add(arrQueryParams[1]);

                    queryParams.put(arrQueryParams[0], listValue);
                }


                Request request = new Request(method, path, queryParams);

                System.out.println("Query параметры из request: " + request.getQueryParams());

                synchronized (Server.handlers) {
                    try {

                        if (method.equals("GET")) {
                            Server.handlers.get(request.getMethod()).get(path).handle(request, out);
                        } else if (method.equals("POST")) {
                            Server.handlers.get(request.getMethod()).get(path).handle(request, out);
                        }

                    } catch (NullPointerException ex) {
                        badRequest(out);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        clientSocket.close();
    }

}


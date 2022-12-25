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
                final int limit = 4096;

                in.mark(limit);
                final byte[] buffer = new byte[limit];
                final int read = in.read(buffer);

                // ищем request line
                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

                if (requestLineEnd == -1) {
                    badRequest(out);
                    continue;
                }

                // читаем request line
                final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

                final String method = requestLine[0];
                final String paramQueryString = requestLine[1];


                if (!paramQueryString.startsWith("/")) {
                    badRequest(out);
                    continue;
                }

                HashMap<String, List<String>> queryParams = new HashMap<>();
                List<NameValuePair> queryStringParams = URLEncodedUtils.parse(requestLine[1], StandardCharsets.UTF_8);
                for (int i = 0; i < queryStringParams.size(); i++) {

                    String[] arrQueryParams = queryStringParams.get(i).toString().split("=");

                    List<String> listValue = queryParams.get(arrQueryParams[0]);

                    if (listValue == null) {
                        listValue = new ArrayList<>();
                    }
                    listValue.add(arrQueryParams[1]);

                    queryParams.put(arrQueryParams[0], listValue);
                }

                Request request = new Request(method, queryParams);

                System.out.println("Query параметры из request: " + request.getQueryParams());


                synchronized (Server.handlers) {
                    try {
                        String path = getPath(request);

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

    private static String getPath(Request request) {
        String path = null;
        for (String key :
                request.getQueryParams().keySet()) {

            if (key.charAt(0) == '/') {
                path = key;
                break;
            }
        }

        assert path != null;
        path = path.split("=")[0].replace("?", "");

        return path;
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


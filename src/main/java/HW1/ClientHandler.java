package HW1;

import org.apache.commons.fileupload.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
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
                System.out.println(Thread.currentThread().getName());

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
                HashMap<String, List<String>> queryParams = getParams(paramQueryString);


                if (!path.startsWith("/")) {
                    badRequest(out);
                    continue;
                }


                // ищем заголовки
                byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                int headersStart = requestLineEnd + requestLineDelimiter.length;
                int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                if (headersEnd == -1) {
                    badRequest(out);
                    continue;
                }

                // отматываем на начало буфера
                in.reset();
                // пропускаем requestLine
                in.skip(headersStart);

                byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
                List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

                Request request = null;
                // для GET тела нет
                if (!method.equals("GET")) {
                    in.skip(headersDelimiter.length);
                    // вычитываем Content-Length, чтобы прочитать body
                    Optional<String> contentLength = extractHeader(headers, "Content-Length");
                    if (contentLength.isPresent()) {
                        int length = Integer.parseInt(contentLength.get());
                        byte[] bodyBytes = in.readNBytes(length);

                        String body = new String(bodyBytes);

                        request = new Request(method, path, headers, body);

                        request.getParts();

                    }
                }


                synchronized (Server.handlers) {
                    try {
                        if (request != null) {
                            if (method.equals("POST")) {
                                Server.handlers.get(request.getMethod()).get(path).handle(request, out);
                            }
                        }

                    } catch (NullPointerException ex) {
                        badRequest(out);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FileUploadException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static HashMap<String, List<String>> getParams(String params) {
        HashMap<String, List<String>> queryParams = new HashMap<>();
        List<NameValuePair> queryStringParams = URLEncodedUtils.parse(params, StandardCharsets.UTF_8);


        for (NameValuePair queryStringParam : queryStringParams) {

            String[] arrQueryParams = queryStringParam.toString().split("=");

            List<String> listValue = queryParams.get(arrQueryParams[0]);

            if (listValue == null) {
                listValue = new ArrayList<>();
            }

            try {
                listValue.add(arrQueryParams[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }


            queryParams.put(arrQueryParams[0], listValue);
        }
        return queryParams;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
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


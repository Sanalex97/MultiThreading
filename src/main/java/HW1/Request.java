package HW1;

import java.nio.file.Path;

public class Request {
    private String method;
    private String path;
    private String header; //host, User-Agent, Accept

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader() {
        return header;
    }

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }
}

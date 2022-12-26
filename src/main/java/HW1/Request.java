package HW1;

import java.util.HashMap;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private HashMap<String, List<String>> queryParams;


    public Request(String method, String path, HashMap<String, List<String>> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.get(name);
    }

    public HashMap<String, List<String>> getQueryParams() {
        return queryParams;
    }
}

package HW1;

import java.util.HashMap;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private HashMap<String, List<String>> queryParams;

    private HashMap<String, List<String>> bodyParams;


    public Request(String method, String path, HashMap<String, List<String>> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
    }

    public void setBodyParams(HashMap<String, List<String>> bodyParams) {
        this.bodyParams = bodyParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getParam(TypeParams typeParam, String name) {
        if (typeParam.equals(TypeParams.QUERY)) {
            return queryParams.get(name);
        } else if (typeParam.equals(TypeParams.BODY)) {
            return bodyParams.get(name);
        } else {
            return null;
        }
    }

    public HashMap<String, List<String>> getParams() {
        return queryParams;
    }


}

package HW1;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request implements RequestContext {
    private String method;
    private String path;
    private List<String> headers;
    private String body;
    private HashMap<String, List<String>> queryParams;

    private HashMap<String, List<String>> bodyParams;


    public Request(String method, String path, HashMap<String, List<String>> queryParams, List<String> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
    }

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
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

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }

    public Map<String, List<String>> getParts() throws FileUploadException {
        Map<String, List<String>> allParts = new HashMap<>();

        if (ServletFileUpload.isMultipartContent(this)) {
            System.out.println(this);
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(this);

            Iterator<FileItem> iter = items.iterator();

            while (iter.hasNext()) {
                List<String> elementParts = new ArrayList<>();
                FileItem item = iter.next();
                if (item.isFormField()) {
                    elementParts.add("Field name = " + item.getFieldName());
                    elementParts.add("Content = " + item.getString());
                    elementParts.add("Content size = " + item.getSize());
                    System.out.println("ITEM " + item.getFieldName());
                    allParts.put(item.getString(), elementParts);
                } else {
                    elementParts.add("Field name = " + item.getFieldName());
                    elementParts.add("Content Type  = " + item.getContentType());
                    elementParts.add("Content = " + item.getString());
                    elementParts.add("Content size = " + item.getSize());
                    elementParts.add("File name = " + item.getName());
                    allParts.put(item.getName(), elementParts);
                    System.out.println("ITEM " + item.getFieldName());
                }

                System.out.println("Map = " + allParts);
            }
        }
        System.out.println(allParts);
        return allParts;
    }


    @Override
    public String getCharacterEncoding() {
        return "UTF_8";
    }

    @Override
    public String getContentType() {
        String result = null;
        for (String header :
                headers) {
            if (header.contains("Content-Type")) {
                String[] bufPartsHeader = header.split(":");
                result = bufPartsHeader[1].trim();
                break;
            }
        }
        return result;
    }

    @Override
    public int getContentLength() {
       /* for (String header :
                headers) {
            if (header.contains("Content-Length")) {
                String[] bufPartsHeader = header.split(":");
                return Integer.parseInt(bufPartsHeader[1]);
            }
        }
        */
        return 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }
}

package HW1;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.http.protocol.RequestContent;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Request implements RequestContext {
    private String method;
    private String path;
    private String body;
    private List<String> headers;
    private HashMap<String, List<String>> queryParams;

    private HashMap<String, List<String>> bodyParams;

    //list<String>headers, body,


    public Request(String method, String path, HashMap<String, List<String>> queryParams, List<String > headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
    }

    public void setBodyParams(String body) {
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
        System.out.println(this);
      //  DiskFileItemFactory factory = new DiskFileItemFactory();
        System.out.println(ServletFileUpload.isMultipartContent(this));
        if (ServletFileUpload.isMultipartContent(this)) {
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
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }
}

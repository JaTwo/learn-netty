package httpService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/25
 * Time: 21:07
 * Description:
 */
public class SimpleRequest {
    /*请求报文如下所示：

    第一行请求方法（post、get等），url，http版本 根据空格分割。末尾是换行和回车
    第二行开始是N个key：value，代表着N个请求头和对应的值
    之后是一行换行回车 也就是空行
    最后是请求体*/
    private String method; //请求方法 GET或者POST
    private String url; //请求url
    private String version; //http版本
    private Map<String, String> heads; //请求头
    private String body;//请求体
    private Map<String, String> params;//请求参数

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeads() {
        return heads;
    }

    public void setHeads(Map<String, String> heads) {
        this.heads = heads;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}

package httpService;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/25
 * Time: 21:07
 * Description:
 */
public class CommonUtil {

    //解码
    public static SimpleRequest decode(byte[] bytes) {
        SimpleRequest simpleRequest = new SimpleRequest();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

            String firstLine = bufferedReader.readLine();
            String[] first = firstLine.split(" ");//第一行以空格分割
            simpleRequest.setMethod(first[0]);
            simpleRequest.setUrl(first[1]);
            simpleRequest.setVersion(first[2]);

            //提取请求头
            Map<String, String> heads = new HashMap<>(); //请求头是key-value 所以用hashMap来保存
            while (true) {
                String twoLine = bufferedReader.readLine();
                if (twoLine.trim().equals("")) {
                    break;
                }
                String[] two = twoLine.split(":");
                heads.put(two[0], two[1]);
            }

            simpleRequest.setHeads(heads);//设置请求头
            simpleRequest.setParams(getUrlParams(simpleRequest.getUrl()));//从url中截取参数
            // 读取请求体
            simpleRequest.setBody(bufferedReader.readLine());//最后一行是请求体
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return simpleRequest;
    }

    //编码
    public static byte[] encode(SimpleResponse response) {
        StringBuilder builder = new StringBuilder(1024);
        //第一行是http版本信息 + 状态码 + 状态码信息 Code中记录了状态码常量和对应的状态码信息
        builder.append(response.getVersion()).append(" ").append(response.getCode()).append("\r\n");

        if (response.getBody() != null && response.getBody().length() != 0) {//如果有响应体 那么就需要加上内容的长度以及内容的格式
            builder.append("Content-Length: ").append(response.getBody().length()).append("\r\n").append("Contetn-Type: text/html\r\n");
        }
        if (response.getHeads() != null) {//如果有响应头，遍历响应头，将key-value，拼接成字符串key：value的形式
            String headStr = response.getHeads().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("\r\n"));
            builder.append(headStr + "\r\n");
        }

        builder.append(response.getBody());//加上响应体
        return builder.toString().getBytes();
    }

    private static Map<String, String> getUrlParams(String url) {
        Map<String, String> map = new HashMap<>();
        if (!url.contains("?"))
            return map;//url中带有"?"说明有参数

        if (url.split("\\?").length > 0) {//根据"?"分割
            String[] arr = url.split("\\?")[1].split("&");//"?"之后的为参数，根据"&"将参数分开来
            for (String s : arr) {//参数是key=value的形式出现的
                if (s.contains("=")) {//所以根据"="进行分割
                    String key = s.split("=")[0];//参数名
                    String value = s.split("=")[1];//参数值
                    map.put(key, value);
                } else {
                    map.put(s, null);
                }
            }
        }
        return map;
    }
}

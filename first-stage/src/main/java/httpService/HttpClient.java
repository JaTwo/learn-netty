package httpService;


import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/25
 * Time: 21:06
 * Description:
 */
public class HttpClient {
    @Test
    public void httpClientTest() throws IOException, InterruptedException {
        HttpServer httpServer = new HttpServer(8080, new SimpleHttpServlet() {

            @Override
            void doGet(SimpleRequest request, SimpleResponse response) {
                try {
                    Thread.sleep(1_000);
                    System.out.println("模拟业务处理");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                response.setBody("hello world");
                response.setCode(200);
                response.setVersion("HTTP/1.1");
                response.setHeads(new HashMap<>());
                if (request.getParams().containsKey("short")) {//短连接
                    response.getHeads().put("Connection", "close");
                } else if (request.getParams().containsKey("long")) {//长连接
                    response.getHeads().put("Connection", "keep-alive");
                    response.getHeads().put("Keep-Alive", "timeout=30,max=300");
                }
            }

            @Override
            void doPost(SimpleRequest request, SimpleResponse response) {

            }
        });
        httpServer.start().join();
    }
}

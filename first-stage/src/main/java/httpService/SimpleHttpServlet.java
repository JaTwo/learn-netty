package httpService;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/25
 * Time: 23:08
 * Description:
 */
public abstract class SimpleHttpServlet {
    abstract void doGet(SimpleRequest request, SimpleResponse response);

    abstract void doPost(SimpleRequest request, SimpleResponse response);
}

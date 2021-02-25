package httpService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/25
 * Time: 21:06
 * Description:
 */
public class HttpServer {
    private final Selector selector;//NIO的选择器
    private final SimpleHttpServlet servlet;
    private final ExecutorService executors;//工作线程池
    private int port;
    private ServerSocketChannel listenerChannel; //用于接收连接通道

    public HttpServer(int port, SimpleHttpServlet servlet) throws IOException {
        this.port = port;
        this.servlet = servlet;
        this.selector = Selector.open();//开启选择器
        this.listenerChannel = ServerSocketChannel.open();//开启通道
        listenerChannel.bind(new InetSocketAddress(port));
        listenerChannel.configureBlocking(false);//设置为非阻塞模式
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.executors = Executors.newFixedThreadPool(5);//初始化工作线程
    }

    public Thread start() {
        Thread thread = new Thread( () -> {
            while (true) {
                try {
                    dispatch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    private void dispatch() throws IOException {
        selector.select(500);
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (!key.isValid()) {
                continue;
            }
            if (key.isAcceptable()) {//可接受状态
                SocketChannel socketChannel = listenerChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            } else if (key.isReadable()) {//可读状态
                SocketChannel channel = (SocketChannel) key.channel();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                while (channel.read(buffer) > 0) {
                    buffer.flip();
                    out.write(buffer.array(), 0 , buffer.limit());
                    buffer.clear();
                }
                if (out.size() == 0) {
                    channel.close();
                    continue;
                }
                //2.进行解码
                SimpleRequest req = CommonUtil.decode(out.toByteArray());
                SimpleResponse resp = new SimpleResponse();
                //3.业务处理
                executors.execute(() -> {
                    if (req.getMethod().equalsIgnoreCase("GET")) {
                        this.servlet.doGet(req, resp);
                    } else {
                        this.servlet.doPost(req, resp);
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();//唤醒IO线程
                });
                key.attach(resp);
            } else if (key.isWritable()) {//可写状态
                SimpleResponse resp = (SimpleResponse) key.attachment();
                //4.编码
                byte[] bytes = CommonUtil.encode(resp);
                SocketChannel channel = (SocketChannel)key.channel();
                //5.写入结果
                channel.write(ByteBuffer.wrap(bytes));
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }


}

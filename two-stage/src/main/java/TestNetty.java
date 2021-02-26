import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/26
 * Time: 21:53
 * Description:
 */
public class TestNetty {

    @Test
    public void test() throws IOException {
        //1.初始化
        NioEventLoopGroup boss = new NioEventLoopGroup(1); //主
        final NioEventLoopGroup workers = new NioEventLoopGroup();//从，默认是CPU的两倍

        NioServerSocketChannel serverChannel = new NioServerSocketChannel();
        boss.register(serverChannel);
        serverChannel.bind(new InetSocketAddress(8080)); //提交任务到eventLoop

        //2.处理Accept事件==注册新管道
        serverChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);// 这里的msg是NioSocketChannel 底层是使用ServerSocketChannel.accept()得到的
                System.out.println("已建立连接");
                handlerAccept(workers, msg);
            }
        });
        System.in.read();
    }

    private void handlerAccept(NioEventLoopGroup workers, final Object msg) {
        NioSocketChannel channel = (NioSocketChannel) msg;
        EventLoop loop = workers.next();

        loop.register(channel);//轮训线程池中的线程，每次注册到NioSocketChannel就会绑定一个线程。 例如这一次绑定了一个线程id为1的线程 下一个就会绑定线程id为2的线
        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
                System.out.println(msg.toString());
            }
        });
    }
}

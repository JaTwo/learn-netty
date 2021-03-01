import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/3/1
 * Time: 22:11
 * Description:
 */
public class DaoyouServer {

    private ServerBootstrap bootstrap;

    @Before
    public void init() {
        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);
    }

    @After
    public void start() throws InterruptedException {
        ChannelFuture future = bootstrap.bind(8080);
        System.out.println("启动成功！");
        future.sync().channel().closeFuture().sync();
    }

    @Test
    public void test() {
        bootstrap.childHandler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new DaoyouProtocol());
                channel.pipeline().addLast(new TrackHandler());
            }
        });
    }

    private class TrackHandler extends SimpleChannelInboundHandler {
        int i = 0;
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            System.out.println(String.format("消息%s:%s", i++, o));
            channelHandlerContext.writeAndFlush("返回消息");
        }
    }
}

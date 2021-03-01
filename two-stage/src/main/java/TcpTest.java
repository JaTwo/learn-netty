import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.junit.Before;
import org.junit.Test;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/28
 * Time: 19:52
 * Description:
 */
public class TcpTest {
    ServerBootstrap bootstrap;
    @Before
    public void init() {
        bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);
    }

    @Test
    public void start() throws InterruptedException {
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                //channel.pipeline().addLast(new FixedLengthFrameDecoder(5));//固定包长的解码
                //channel.pipeline().addLast(new LineBasedFrameDecoder(10));//换行解码，如果一行超过10个字节，则报异常
                ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{'$'});
                //以指定字符解码。第二个参数为丢弃指定字符
                channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, true, buf));
                channel.pipeline().addLast(new TrackHandler());

            }
        });
        ChannelFuture future = bootstrap.bind(8080).sync();
        future.channel().closeFuture().sync();//同步阻塞
    }

    private class TrackHandler extends SimpleChannelInboundHandler {
        int count;
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            ByteBuf buf = (ByteBuf)o;
            String message = buf.toString(Charset.defaultCharset());
            System.out.println(String.format("消息s%:s%", ++count, message));
        }
    }
}

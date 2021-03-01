import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/3/1
 * Time: 22:05
 * Description:
 */
public class DaoyouClient {
    private Bootstrap bootstrap;
    private Channel channel;

    public void start() throws InterruptedException {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new DaoyouProtocol());
            }
        });
        ChannelFuture future = bootstrap.connect("127.0.0.1", 8080);
        channel = future.sync().channel();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        DaoyouClient client = new DaoyouClient();
        client.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            client.channel.writeAndFlush(line);
        }
    }
}

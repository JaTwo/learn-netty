import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/2/26
 * Time: 22:33
 * Description:
 */
public class BootstrapTest {
    public void open(int port) {
        ServerBootstrap boot = new ServerBootstrap();
        NioEventLoopGroup boos = new NioEventLoopGroup(1);
        NioEventLoopGroup work = new NioEventLoopGroup(8);

        boot.group(boos, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast("decode", new HttpRequestDecoder());
                        channel.pipeline().addLast("aggregator", new HttpObjectAggregator(65535));
                        channel.pipeline().addLast("servlet", new MyServlet());
                        channel.pipeline().addLast("encode", new HttpResponseEncoder());
                    }
                });
        ChannelFuture future = boot.bind(port);
        future.addListener(future1 -> System.out.println("注册成功"));
    }

    private class MyServlet extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                System.out.println("url:" + request.uri());
                System.out.println(request.content().toString());

                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("hello".getBytes());
                ChannelFuture future = ctx.writeAndFlush(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                System.out.println("当前请求:" + request.uri());
            }
            if (msg instanceof HttpContent) {
                // 写入文件流
                ByteBuf content = ((HttpContent) msg).content();
                OutputStream out = new FileOutputStream("E:\\code\\learn-netty\\test.txt", true);
                content.readBytes(out, content.readableBytes());
                out.close();
            }
            if (msg instanceof LastHttpContent) {//上传大文件 要分开发 最后一个包是lastHttpContent
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("上传完毕".getBytes());
                ChannelFuture future = ctx.writeAndFlush(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new BootstrapTest().open(8080);
        System.in.read();
    }
}

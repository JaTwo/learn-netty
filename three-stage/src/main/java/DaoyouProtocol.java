import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jtwo_
 * Date: 2021/3/1
 * Time: 21:49
 * Description: Daoyou协议
 * ================================================================
 *             协议标识4字节   消息体长度4字节   消息体
 * ================================================================
 */
public class DaoyouProtocol extends ByteToMessageCodec<String> {
    static int MAGIC = 0xDADA;//协议的标识
    static ByteBuf MAGIC_BUF = Unpooled.copyInt(MAGIC);
    //编码
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, ByteBuf byteBuf) throws Exception {
        byte[] bytes = s.getBytes();
        byteBuf.writeInt(MAGIC);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int index = indexOf(byteBuf, MAGIC_BUF); //读取协议标识的起始位置
        if (index < 0) {
            return;//需要更多的字节
        }
        if (!byteBuf.isReadable(index + 8)) {
            return;//标识位 + 消息体长度 = 8； 需要更多的字节
        }
        int length = byteBuf.slice(index + 4, 4).readInt();
        if (!byteBuf.isReadable(index + 8 + length)) {
            return;
        }
        byteBuf.skipBytes(index + 8);
        ByteBuf buf = byteBuf.readRetainedSlice(length);
        String message = buf.toString(Charset.defaultCharset());
        list.add(message);
    }

    private static int indexOf(ByteBuf haystack, ByteBuf needle) {
        for(int i = haystack.readerIndex(); i < haystack.writerIndex(); ++i) {
            int haystackIndex = i;

            int needleIndex;
            for(needleIndex = 0; needleIndex < needle.capacity() && haystack.getByte(haystackIndex) == needle.getByte(needleIndex); ++needleIndex) {
                ++haystackIndex;
                if (haystackIndex == haystack.writerIndex() && needleIndex != needle.capacity() - 1) {
                    return -1;
                }
            }

            if (needleIndex == needle.capacity()) {
                return i - haystack.readerIndex();
            }
        }

        return -1;
    }
}

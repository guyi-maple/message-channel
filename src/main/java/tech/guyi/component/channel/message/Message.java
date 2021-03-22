package tech.guyi.component.channel.message;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 消息实体
 * @author guyi
 * @version 2021/1/12 13:12
 */
public class Message {

    /**
     * 消息内容
     */
    @Getter
    private final byte[] content;
    /**
     * 消息来源地址
     */
    @Getter
    private final InetSocketAddress source;
    /**
     * 管道处理上下文
     */
    @Getter
    private final ChannelHandlerContext context;

    /**
     * 消息回复处理
     */
    private final MessageReplier replier;


    public Message(byte[] content, InetSocketAddress source, ChannelHandlerContext context, MessageReplier replier) {
        this.content = content;
        this.source = source;
        this.context = context;
        this.replier = replier;
    }

    /**
     * 向来源地址回复消息
     * @param bytes 回复的消息内容
     */
    public void reply(byte[] bytes){
        this.replier.apply(this,bytes);
    }

    /**
     * 将消息内容转为字符串
     * @param charset 字符编码
     * @return 字符串
     */
    public String toString(Charset charset){
        return new String(this.content,charset);
    }

    /**
     * <p>将消息内容转为字符串</p>
     * <p>字符编码使用UTF-8</p>
     * @return 字符串
     */
    public String toString(){
        return this.toString(StandardCharsets.UTF_8);
    }

    public static Message of(byte[] content, InetSocketAddress source, ChannelHandlerContext context, MessageReplier replier) {
        return new Message(content, source,context,replier);
    }
}

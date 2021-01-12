package tech.guyi.component.channel;

import lombok.Getter;
import tech.guyi.component.channel.handler.OnChannelHandler;
import tech.guyi.component.channel.handler.OnMessageHandler;
import tech.guyi.component.channel.message.Message;

import java.net.InetSocketAddress;
import java.util.function.Function;

/**
 * 消息管道配置项
 * @author guyi
 * @date 2021/1/12 14:10
 */
@Getter
public class MessageChannelOption<T> {

    /**
     * UDP广播地址
     */
    public static final MessageChannelOption<String> BROADCAST_ADDRESS = valueOf("BROADCAST_ADDRESS");
    /**
     * UDP广播端口
     */
    public static final MessageChannelOption<Integer> BROADCAST_PORT = valueOf("BROADCAST_PORT");
    /**
     * 缓冲区大小
     */
    public static final MessageChannelOption<Integer> BUFFER_SIZE = valueOf("BUFFER_SIZE");
    /**
     * 消息到达处理器
     */
    public static final MessageChannelOption<OnMessageHandler> ON_MESSAGE = valueOf("ON_MESSAGE");
    /**
     * 管道建立完成处理器
     */
    public static final MessageChannelOption<OnChannelHandler> ON_CHANNEL = valueOf("ON_CHANNEL");
    /**
     * 是否开启端口共享
     */
    public static final MessageChannelOption<Boolean> REUSEADDR = valueOf("REUSEADDR");
    /**
     * 是否启用TCP
     */
    public static final MessageChannelOption<Boolean> ENABLE_TCP = valueOf("ENABLE_TCP");
    /**
     * 是否启用UDP
     */
    public static final MessageChannelOption<Boolean> ENABLE_UDP = valueOf("ENABLE_UDP");
    /**
     * 树状消息管道上级地址
     */
    public static final MessageChannelOption<InetSocketAddress> TREE_PARENT_ADDRESS = valueOf("TREE_PARENT_ADDRESS");
    /**
     * 排除自身发送的UDP消息
     */
    public static final MessageChannelOption<Function<Message,Boolean>> EXCLUDE_SELF_UDP_MESSAGE = valueOf("TREE_PARENT_ADDRESS");

    /**
     * 创建配置项
     * @param key 键
     * @param <P> 值类型
     * @return 配置项
     */
    public static <P> MessageChannelOption<P> valueOf(String key){
        return new MessageChannelOption<>(key, value -> (P) value);
    }

    /**
     * 键
     */
    private final String key;
    /**
     * 值类型强转处理器
     */
    private final Function<Object,T> cast;
    public MessageChannelOption(String key, Function<Object, T> cast) {
        this.key = key;
        this.cast = cast;
    }
}

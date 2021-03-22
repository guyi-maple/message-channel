package tech.guyi.component.channel;

import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

/**
 * 消息管道抽象
 * @author guyi
 * @version 2021/1/12 12:57
 */
public interface MessageChannel {

    /**
     * 获取配置项的值
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    default <T> Optional<T> getOption(MessageChannelOption<T> key){
        return Optional.ofNullable(this.options().get(key)).map(key.getCast());
    }

    /**
     * 获取管道配置
     * @return 管道配置
     */
    Map<MessageChannelOption<Object>, Object> options();

    /**
     * 批量管道配置
     * @param options 管道集合
     * @return 当前管道
     */
    default MessageChannel options(Map<MessageChannelOption<Object>, Object> options){
        options.forEach(this::option);
        return this;
    }

    /**
     * 管道配置
     * @param key 键
     * @param value 值
     * @param <T> 值类型
     * @return 当前管道
     */
    <T> MessageChannel option(MessageChannelOption<T> key, T value);

    /**
     * 监听消息
     * @param address 监听地址及端口
     */
    void listen(InetSocketAddress address);

    /**
     * 监听消息
     * @param address 监听地址及端口
     * @param group 线程组
     */
    void listen(InetSocketAddress address, EventLoopGroup group);

    /**
     * 发布消息
     * @param target 目标
     * @param bytes 消息内容
     */
    void publish(InetSocketAddress target, byte[] bytes);

    /**
     * <p>发布消息</p>
     * <p>当为UDP管道时表示广播消息</p>
     * <p>当为TCP管道时表示给所有客户端发送消息</p>
     * @param bytes 消息内容
     */
    void publish(byte[] bytes);

    /**
     * 关闭管道
     */
    void close();

}

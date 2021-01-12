package tech.guyi.component.channel.defaults;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import tech.guyi.component.channel.MessageChannel;
import tech.guyi.component.channel.MessageChannelOption;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 消息管道抽象类 <br />
 * 集成较为通用部分功能
 * @author guyi
 * @date 2021/1/12 15:52
 */
public abstract class AbstractMessageChannel implements MessageChannel {

    // 线程组
    private EventLoopGroup group;
    // 配置项集合
    private final Map<MessageChannelOption<Object>, Object> options = new HashMap<>();

    @Override
    public Map<MessageChannelOption<Object>, Object> options() {
        return this.options;
    }

    @Override
    public <T> MessageChannel option(MessageChannelOption<T> key, T value) {
        this.options.put((MessageChannelOption<Object>) key,value);
        return this;
    }

    @Override
    public void listen(InetSocketAddress address) {
        this.group = new NioEventLoopGroup();
        this.listen(address,this.group);
    }

    @Override
    public void close() {
        this.options.clear();
        Optional.ofNullable(this.group)
                .map(EventLoopGroup::shutdownGracefully)
                .ifPresent(future -> {
                    try {
                        future.sync();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}

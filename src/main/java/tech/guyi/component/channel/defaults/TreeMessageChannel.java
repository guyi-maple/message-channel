package tech.guyi.component.channel.defaults;

import io.netty.channel.EventLoopGroup;
import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.connection.TcpConnection;
import tech.guyi.component.channel.handler.OnMessageHandler;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * <p>树状消息管道</p>
 * <p>收到的UDP消息转发到上级节点</p>
 * <p>上级节点发送的消息以UDP方式广播到局域网, 并转发到下级节点</p>
 * @author guyi
 * @version 2021/1/12 19:59
 */
public class TreeMessageChannel extends AbstractMessageChannel {

    // 消息处理器
    private final OnMessageHandler onMessage = message -> {
        this.fusion.publish(message.getContent());
        Optional.ofNullable(this.originOnMessage)
                .ifPresent(on -> on.accept(message));
    };

    // 父节点连接
    private TcpConnection connection;
    // 融合消息通道
    private FusionMessageChannel fusion;
    // 原始消息到达处理器
    private OnMessageHandler originOnMessage;

    @Override
    public void listen(InetSocketAddress address, EventLoopGroup group) {
        this.fusion = new FusionMessageChannel();

        // 如果设置了上级节点, 使用TCP连接到上级节点
        this.getOption(MessageChannelOption.TREE_PARENT_ADDRESS)
                .ifPresent(parent -> {
                    this.connection = new TcpConnection();
                    // 将收到的上级消息转发
                    this.connection.onMessage(this::publish);
                    this.connection.connect(parent, group);
                });

        // 缓存原始消息到达处理器
        this.originOnMessage = this.getOption(MessageChannelOption.ON_MESSAGE).orElse(null);

        this.fusion.options(this.options())
                // 开启TCP
                .option(MessageChannelOption.ENABLE_TCP, true)
                // 开启UDP
                .option(MessageChannelOption.ENABLE_UDP, true)
                // 设置消息到达处理器
                .option(MessageChannelOption.ON_MESSAGE, this.onMessage)
                .listen(address,group);
    }

    @Override
    public void publish(InetSocketAddress target, byte[] bytes) {
        this.fusion.publish(target,bytes);
    }

    @Override
    public void publish(byte[] bytes) {
        this.fusion.publish(bytes);
    }

}

package tech.guyi.component.channel.defaults;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.SneakyThrows;
import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.message.Message;
import tech.guyi.component.channel.message.MessageReplier;

import java.net.InetSocketAddress;

/**
 * @author guyi
 * @version 2021/1/12 12:58
 */
public class UdpMessageChannel extends AbstractMessageChannel {

    // 消息回复处理
    private final MessageReplier replier = (message, bytes) ->
            message.getContext().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes),message.getSource()));

    // 监听的端口
    private int listenPort;
    // 管道对象
    private Channel serverChannel;

    @Override
    @SneakyThrows
    public void listen(InetSocketAddress address, EventLoopGroup group) {
        // 缓存监听端口
        this.listenPort = address.getPort();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                // 允许UDP广播
                .option(ChannelOption.SO_BROADCAST, true)
                // 从配置项获取并配置是否允许端口共享, 默认不允许
                .option(ChannelOption.SO_REUSEADDR, this.getOption(MessageChannelOption.REUSEADDR).orElse(false))
                // 从配置项中获取并配置缓冲区大小, 默认48字节
                .option(ChannelOption.SO_RCVBUF, this.getOption(MessageChannelOption.BUFFER_SIZE).orElse(1024 * 48))
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        serverChannel = channel;
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                DatagramPacket packet = (DatagramPacket) msg;
                                byte[] bytes = new byte[packet.content().readableBytes()];
                                packet.content().readBytes(bytes);
                                getOption(MessageChannelOption.ON_MESSAGE)
                                        .ifPresent(onMessage -> onMessage.accept(Message.of(bytes, packet.sender(),ctx,replier)));
                            }
                        });
                    }
                });

        // 监听端口
        bootstrap.bind(address).sync();
    }

    @Override
    public void publish(InetSocketAddress target, byte[] bytes) {
        this.serverChannel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(bytes),
                target
        ));
    }

    @Override
    public void publish(byte[] bytes) {
        this.publish(
                new InetSocketAddress(
                        this.getOption(MessageChannelOption.BROADCAST_ADDRESS)
                                .orElse("255.255.255.255"),
                        this.getOption(MessageChannelOption.BROADCAST_PORT)
                                .orElse(this.listenPort)
                ),
                bytes
        );
    }

}

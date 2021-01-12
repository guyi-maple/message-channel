package tech.guyi.component.channel.defaults;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;
import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.defaults.AbstractMessageChannel;
import tech.guyi.component.channel.message.Message;
import tech.guyi.component.channel.message.MessageReplier;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TCP消息管道
 * @author guyi
 * @date 2021/1/12 15:50
 */
public class TcpMessageChannel extends AbstractMessageChannel {

    // 消息回复处理
    private final MessageReplier replier = ((message, bytes) -> message.getContext().writeAndFlush(bytes));

    // 客户端集合
    private final Map<String,Channel> clients = new HashMap<>();

    // 获取连接ID
    private String getChannelId(InetSocketAddress address){
        return String.format("%s-%s",address.getAddress().getHostName(),address.getPort());
    }
    // 获取连接ID
    private String getChannelId(Channel channel){
        return this.getChannelId((InetSocketAddress) channel.remoteAddress());
    }

    @Override
    @SneakyThrows
    public void listen(InetSocketAddress address, EventLoopGroup group) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                // 从配置项获取并配置是否允许端口共享, 默认不允许
                .option(ChannelOption.SO_REUSEADDR, this.getOption(MessageChannelOption.REUSEADDR).orElse(false))
                // 从配置项中获取并配置缓冲区大小, 默认48字节
                .option(ChannelOption.SO_RCVBUF, this.getOption(MessageChannelOption.BUFFER_SIZE).orElse(1024 * 48))
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRegistered(ChannelHandlerContext ctx) {
                                // 将客户端连接放入集合
                                clients.put(getChannelId(ctx.channel()),ctx.channel());
                                // 从配置项获取连接建立处理器, 存在则传入客户端连接
                                getOption(MessageChannelOption.ON_CHANNEL)
                                        .ifPresent(onChannel -> onChannel.accept(ctx.channel()));
                            }

                            @Override
                            public void channelUnregistered(ChannelHandlerContext ctx) {
                                // 从客户端连接集合中移除当前连接
                                Channel channel = clients.remove(getChannelId(ctx.channel()));
                                // 关闭连接
                                channel.close();
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 从配置项中获取消息到达处理器, 存在则传入消息
                                getOption(MessageChannelOption.ON_MESSAGE).ifPresent(onMessage -> {
                                    ByteBuf buffer = (ByteBuf) msg;
                                    byte[] bytes = new byte[buffer.readableBytes()];
                                    buffer.readBytes(bytes);
                                    onMessage.accept(Message.of(bytes,(InetSocketAddress) ctx.channel().remoteAddress(),ctx,replier));
                                });
                            }
                        });
                    }
                });

        // 监听端口
        bootstrap.bind(address).sync();
    }

    @Override
    public void publish(InetSocketAddress target, byte[] bytes) {
        Optional.ofNullable(this.clients.get(this.getChannelId(target)))
                .ifPresent(channel -> channel.writeAndFlush(Unpooled.copiedBuffer(bytes)));
    }

    @Override
    public void publish(byte[] bytes) {
        this.clients.values().forEach(channel -> channel.writeAndFlush(Unpooled.copiedBuffer(bytes)));
    }

}

package tech.guyi.component.channel.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author guyi
 * @date 2021/1/12 17:55
 */
public class TcpConnection {

    private SocketChannel channel;
    public EventLoopGroup group;

    private Consumer<byte[]> onMessage;

    public TcpConnection onMessage(Consumer<byte[]> onMessage){
        this.onMessage = onMessage;
        return this;
    }

    public void connect(InetSocketAddress address){
        this.group = new NioEventLoopGroup();
        this.connect(address,this.group);
    }

    @SneakyThrows
    public void connect(InetSocketAddress address, EventLoopGroup group){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        channel = ch;
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                byte[] bytes = new byte[buffer.readableBytes()];
                                buffer.readBytes(bytes);
                                Optional.ofNullable(onMessage).ifPresent(onMessage -> onMessage.accept(bytes));
                            }
                        });
                    }
                });
        bootstrap.connect(address).sync();
    }

    public void publish(byte[] bytes){
        this.channel.writeAndFlush(Unpooled.copiedBuffer(bytes));
    }

    public void close(){
        Optional.ofNullable(this.group).ifPresent(EventLoopGroup::shutdownGracefully);
    }

}

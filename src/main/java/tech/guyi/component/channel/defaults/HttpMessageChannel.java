package tech.guyi.component.channel.defaults;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.SneakyThrows;
import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.message.Message;
import tech.guyi.component.channel.message.MessageReplier;
import tech.guyi.component.channel.message.http.HttpMessage;
import tech.guyi.component.channel.message.http.HttpMessageResponse;

import java.net.InetSocketAddress;

/**
 * @author e-Peng.Zhang2
 * @version 2021/3/22
 */
public class HttpMessageChannel extends AbstractMessageChannel {

    @Override
    @SneakyThrows
    public void listen(InetSocketAddress address, EventLoopGroup group) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, getOption(MessageChannelOption.REUSEADDR).orElse(false))
                .option(ChannelOption.SO_RCVBUF, this.getOption(MessageChannelOption.BUFFER_SIZE).orElse(1024 * 48))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                                  @Override
                                  @SneakyThrows
                                  protected void initChannel(SocketChannel ch) {
                                      ChannelPipeline pipeline = ch.pipeline();
                                      pipeline.addLast("responseEncode", new HttpResponseEncoder());
                                      pipeline.addLast("requestDecode", new HttpRequestDecoder());

                                      pipeline.addLast("objectAggregator", new HttpObjectAggregator(1024));
                                      pipeline.addLast("contentCompressor", new HttpContentCompressor());
                                      pipeline.addLast("HTTPServerHandler", new SimpleChannelInboundHandler<Object>(){
                                          @Override
                                          protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
                                              FullHttpRequest request = (FullHttpRequest) obj;

                                              HttpMessage httpMessage = new HttpMessage();
                                              httpMessage.setUri(request.uri());
                                              request.headers().forEach(header -> httpMessage.getHeaders().put(header.getKey(), header.getValue()));
                                              httpMessage.setMethod(request.method().name());

                                              MessageReplier replier = (msg, bytes) -> {
                                                  HttpMessageResponse resp = HttpMessageResponse.from(bytes);
                                                  DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                                                          HttpVersion.HTTP_1_1,
                                                          HttpResponseStatus.OK,
                                                          Unpooled.copiedBuffer(resp.getBody())
                                                  );
                                                  resp.getHeaders().forEach((key, value) -> response.headers().set(key, value));
                                                  ctx.writeAndFlush(response);
                                              };

                                              Message message = Message.of(httpMessage.toBytes(), (InetSocketAddress) ctx.channel().remoteAddress(), ctx, replier);
                                              getOption(MessageChannelOption.ON_MESSAGE).ifPresent(handler -> handler.accept(message));
                                          }
                                      });
                                  }
                              }
                );
        bootstrap.bind(address).sync();
    }

    @Override
    public void publish(InetSocketAddress target, byte[] bytes) {
        throw new RuntimeException("HTTP管道禁止主动推送消息");
    }

    @Override
    public void publish(byte[] bytes) {
        this.publish(null, bytes);
    }

}

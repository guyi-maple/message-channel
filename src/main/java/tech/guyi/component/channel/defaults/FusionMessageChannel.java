package tech.guyi.component.channel.defaults;

import io.netty.channel.EventLoopGroup;
import tech.guyi.component.channel.MessageChannelOption;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @author guyi
 * @date 2021/1/12 17:31
 */
public class FusionMessageChannel extends AbstractMessageChannel {

    private TcpMessageChannel tcp;
    private UdpMessageChannel udp;

    @Override
    public void listen(InetSocketAddress address, EventLoopGroup group) {
        getOption(MessageChannelOption.ENABLE_TCP)
                .filter(enable -> enable)
                .ifPresent(enable -> {
                    this.tcp = new TcpMessageChannel();
                    this.tcp.options(this.options())
                            .option(MessageChannelOption.REUSEADDR, true)
                            .listen(address,group);
                });
        getOption(MessageChannelOption.ENABLE_UDP)
                .filter(enable -> enable)
                .ifPresent(enable -> {
                    this.udp = new UdpMessageChannel();
                    this.udp.options(this.options())
                            .option(MessageChannelOption.REUSEADDR, true)
                            .listen(address,group);
                });
    }

    @Override
    public void publish(InetSocketAddress target, byte[] bytes) {
        Optional.ofNullable(this.tcp).ifPresent(tcp -> tcp.publish(target,bytes));
        Optional.ofNullable(this.udp).ifPresent(udp -> udp.publish(target,bytes));
    }

    @Override
    public void publish(byte[] bytes) {
        Optional.ofNullable(this.tcp).ifPresent(tcp -> tcp.publish(bytes));
        Optional.ofNullable(this.udp).ifPresent(udp -> udp.publish(bytes));
    }
}

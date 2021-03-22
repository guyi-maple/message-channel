package tech.guyi.component.channel.test;

import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.defaults.FusionMessageChannel;

import java.net.InetSocketAddress;

/**
 * @author guyi
 * @version 2021/1/12 17:49
 */
public class FusionTest {

    public static void main(String[] args) {
        FusionMessageChannel channel = new FusionMessageChannel();
        channel.option(MessageChannelOption.ENABLE_TCP, true)
                .option(MessageChannelOption.ENABLE_UDP, true)
                .option(MessageChannelOption.ON_MESSAGE, System.out::println)
                .listen(new InetSocketAddress(8888));
        System.out.println("服务器开启完成");
    }

}

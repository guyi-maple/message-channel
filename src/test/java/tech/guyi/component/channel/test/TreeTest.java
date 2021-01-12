package tech.guyi.component.channel.test;

import tech.guyi.component.channel.MessageChannel;
import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.defaults.TreeMessageChannel;
import tech.guyi.component.channel.defaults.UdpMessageChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author guyi
 * @date 2021/1/12 20:15
 */
public class TreeTest {

    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

        service.execute(() -> {
            TreeMessageChannel channel = new TreeMessageChannel();
            channel.option(MessageChannelOption.BROADCAST_PORT, 8002);
            channel.listen(new InetSocketAddress(8000));

            service.scheduleWithFixedDelay(() -> {
                channel.publish(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
                System.out.println("发送消息");
            }, 5,5, TimeUnit.SECONDS);

        });

        service.execute(() -> {
            TreeMessageChannel channel = new TreeMessageChannel();
            channel.option(MessageChannelOption.TREE_PARENT_ADDRESS,new InetSocketAddress("127.0.0.1",8000))
                    .option(MessageChannelOption.BROADCAST_PORT, 9000)
                    .listen(new InetSocketAddress(8001));
        });

        MessageChannel channel = new UdpMessageChannel();
        channel.option(MessageChannelOption.ON_MESSAGE, message -> System.out.printf("收到UDP消息 %s \n",new String(message.getContent())))
                .listen(new InetSocketAddress(9000));

    }

}

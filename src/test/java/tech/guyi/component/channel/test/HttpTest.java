package tech.guyi.component.channel.test;

import tech.guyi.component.channel.MessageChannelOption;
import tech.guyi.component.channel.defaults.HttpMessageChannel;
import tech.guyi.component.channel.message.http.HttpMessage;
import tech.guyi.component.channel.message.http.HttpMessageResponse;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author e-Peng.Zhang2
 * @version 2021/3/22
 */
public class HttpTest {

    public static void main(String[] args) {
        HttpMessageChannel channel = new HttpMessageChannel();
        channel.option(MessageChannelOption.ON_MESSAGE, message -> {
            HttpMessage http = HttpMessage.from(message);

            Optional.ofNullable(http.getBody())
                    .map(String::new)
                    .ifPresent(System.out::println);

            HttpMessageResponse response = new HttpMessageResponse("123456".getBytes(StandardCharsets.UTF_8),  null);
            http.reply(response);
        });
        channel.listen(new InetSocketAddress(8080));
    }

}

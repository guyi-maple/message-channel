package tech.guyi.component.channel.message;

/**
 * 消息回复处理
 * @author guyi
 * @date 2021/1/12 13:21
 */
@FunctionalInterface
public interface MessageReplier {

    void apply(Message message, byte[] bytes);

}

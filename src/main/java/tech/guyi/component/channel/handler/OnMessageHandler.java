package tech.guyi.component.channel.handler;

import tech.guyi.component.channel.message.Message;

import java.util.function.Consumer;

/**
 * 消息到达处理器
 * @author guyi
 * @date 2021/1/12 13:58
 */
@FunctionalInterface
public interface OnMessageHandler extends Consumer<Message> {
}

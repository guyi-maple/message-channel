package tech.guyi.component.channel.handler;

import io.netty.channel.Channel;

import java.util.function.Consumer;

/**
 * 管道建立完成处理器
 * @author guyi
 * @date 2021/1/12 13:58
 */
@FunctionalInterface
public interface OnChannelHandler extends Consumer<Channel> {
}

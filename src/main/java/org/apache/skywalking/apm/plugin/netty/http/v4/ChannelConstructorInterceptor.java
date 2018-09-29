/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import java.net.SocketAddress;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ServerChannel;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class ChannelConstructorInterceptor implements InstanceConstructorInterceptor {
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        if (objInst instanceof ServerChannel) {
            return;
        }
        AbstractChannel channel = (AbstractChannel) objInst;
        channel.pipeline().addLast(new ErrorHandler()); /* maybe has order problem */
    }

    private static class ErrorHandler extends ChannelDuplexHandler {
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            TraceHelper.onException(cause, ctx);
            ctx.fireExceptionCaught(cause);
        }

        public void connect(final ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.cause() != null) {
                        TraceHelper.onException(future.cause(), ctx);
                    }
                }
            }));
        }

        public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            ctx.write(msg, promise.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.cause() != null) {
                        TraceHelper.onException(future.cause(), ctx);
                    }
                }
            }));
        }
    }
}
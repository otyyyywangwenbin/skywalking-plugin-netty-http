/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class EncodeInterceptor implements InstanceMethodsAroundInterceptor {
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        ChannelHandlerContext context = (ChannelHandlerContext) allArguments[0];
        Object msg = allArguments[1];
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            TracingHelper.onClientRequest(request, context);
        }
        return;
    }

    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        ChannelHandlerContext context = (ChannelHandlerContext) allArguments[0];
        Object msg = allArguments[1];
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            TracingHelper.onServerResponse(response, context);
        }
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        ChannelHandlerContext context = (ChannelHandlerContext) allArguments[0];
        TracingHelper.onException(t, context);
    }
}

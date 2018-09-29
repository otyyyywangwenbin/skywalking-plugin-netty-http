/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
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

public class DecodeInterceptor implements InstanceMethodsAroundInterceptor {

    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        return;
    }

    @SuppressWarnings("unchecked")
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        ChannelHandlerContext context = (ChannelHandlerContext) allArguments[0];
        List<Object> out = (List<Object>) allArguments[2];
        int size = out.size();
        for (int i = size - 1; i >= 0; i--) {
            Object obj = out.get(i);
            if (obj instanceof HttpRequest) {
                TracingHelper.onServerReceived((HttpRequest) obj, context);
                break;
            } else if (obj instanceof HttpResponse) {
                TracingHelper.onClientReceived((HttpResponse) obj, context);
                break;
            }
        }
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }
}

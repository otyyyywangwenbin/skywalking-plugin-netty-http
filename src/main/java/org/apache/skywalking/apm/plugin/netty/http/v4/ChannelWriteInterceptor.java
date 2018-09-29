/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.KEY_CONTEXT_SNAPSHOT;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import io.netty.channel.Channel;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class ChannelWriteInterceptor implements InstanceMethodsAroundInterceptor {

    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        Channel channel = (Channel) objInst;
        if (channel.attr(KEY_CONTEXT_SNAPSHOT).get() == null && ContextManager.isActive()) {
            ContextSnapshot contextSnapshot = ContextManager.capture();
            channel.attr(KEY_CONTEXT_SNAPSHOT).set(contextSnapshot);
        }
        return;
    }

    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        return;
    }
}
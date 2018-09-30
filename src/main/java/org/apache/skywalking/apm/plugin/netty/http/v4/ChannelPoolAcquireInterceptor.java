/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.KEY_CONTEXT;

import java.lang.reflect.Method;

import org.apache.skywalking.apm.agent.core.context.AbstractTracerContext;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class ChannelPoolAcquireInterceptor implements InstanceMethodsAroundInterceptor {

    @SuppressWarnings("unchecked")
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        Promise<Channel> promise = (Promise<Channel>) allArguments[0];
        promise.addListener(new TracingContextBinder());
        return;
    }

    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        return;
    }

    private static class TracingContextBinder implements GenericFutureListener<Future<Channel>> {

        private AbstractTracerContext tracingContext;

        private TracingContextBinder() {
            if (ContextManager.isActive()) {
                /* 
                 * fixedchannelpool.acquire(promis)的方法实现会调用到channelpool.acquire(promis), 所以该方法会被调用2遍, 第2遍调的时候isActive==false
                 * 为什么不只处理channelpool.acquire(promis)? 因为如果是由fixedchannelpool.acquire(promis)触发的就有可能是跨线程的, 那么这个ContextManager.isActive()就肯定为false
                 */
                this.tracingContext = TracingHelper.getTracingContext();
            }
        }

        public void operationComplete(Future<Channel> future) throws Exception {
            if (tracingContext == null) {
                return;
            }
            if (future.isSuccess()) {
                future.get().attr(KEY_CONTEXT).set(tracingContext);
            } else {
                if (future.cause() != null) {
                    tracingContext.activeSpan().errorOccurred().log(future.cause());
                }
            }
        }
    }
}
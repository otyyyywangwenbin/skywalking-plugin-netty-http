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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class AddListenerInterceptor implements InstanceMethodsAroundInterceptor {

    @SuppressWarnings("unchecked")
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        GenericFutureListener<Future<?>> listener = (GenericFutureListener<Future<?>>) allArguments[0];
        GenericFutureListener<Future<?>> wrapper = GenericFutureListenerWrapper.wrapper(listener);
        if (wrapper != null) {
            allArguments[0] = wrapper;
        }
        return;
    }

    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }

    private static class GenericFutureListenerWrapper implements GenericFutureListener<Future<?>> {
        private GenericFutureListener<Future<?>> delegate;
        private ContextSnapshot contextSnapshot;

        public static GenericFutureListenerWrapper wrapper(GenericFutureListener<Future<?>> delegate) {
            if (delegate.getClass().getName().equals("reactor.ipc.netty.channel.PooledClientContextHandler")) {
                return new GenericFutureListenerWrapper(delegate);
            }
            return null;
        }

        private GenericFutureListenerWrapper(GenericFutureListener<Future<?>> delegate) {
            this.delegate = delegate;
            this.contextSnapshot = ContextManager.capture();
        }

        public void operationComplete(Future<?> future) throws Exception {
            if (future.isSuccess()) {
                ((Channel) future.get()).attr(KEY_CONTEXT_SNAPSHOT).set(contextSnapshot);
            }
            this.delegate.operationComplete(future);
        }
    }

}

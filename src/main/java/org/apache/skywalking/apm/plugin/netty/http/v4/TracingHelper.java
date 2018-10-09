/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.COMPONENT_NETTY_HTTP_CLIENT;
import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.COMPONENT_NETTY_HTTP_SERVER;
import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.KEY_CONTEXT;

import java.lang.reflect.Field;

import org.apache.skywalking.apm.agent.core.context.AbstractTracerContext;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class TracingHelper {
    private static final ILog logger = LogManager.getLogger(TracingHelper.class);

    @SuppressWarnings("unchecked")
    public static AbstractTracerContext getTracingContext() {
        if (ContextManager.isActive()) {
            try {
                Field f = ContextManager.class.getDeclaredField("CONTEXT");
                f.setAccessible(true);
                ThreadLocal<AbstractTracerContext> CONTEXT = (ThreadLocal<AbstractTracerContext>) f.get(ContextManager.class);
                return CONTEXT.get();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void setTracingContext(AbstractTracerContext context) {
        try {
            Field f = ContextManager.class.getDeclaredField("CONTEXT");
            f.setAccessible(true);
            ThreadLocal<AbstractTracerContext> CONTEXT = (ThreadLocal<AbstractTracerContext>) f.get(ContextManager.class);
            CONTEXT.set(context);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void onException(Throwable cause, ChannelHandlerContext context) {
        AbstractTracerContext tracingContext = context.channel().attr(KEY_CONTEXT).get();
        if (tracingContext == null) {
            return;
        }
        tracingContext.activeSpan().errorOccurred().log(cause);
    }

    public static void onServerRequest(HttpRequest request, ChannelHandlerContext context) {
        if (ContextManager.isActive()) {
            setTracingContext(null); /* 说明上一个请求的response还没有发出, 该线程又开始处理新的请求, 需要把线程变量CONTEXT设置成null, 延迟设置null是为了后面的其他plugin还可以使用ContextManager接口 */
        }
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue(request.headers().get(next.getHeadKey()));
        }
        AbstractSpan span = ContextManager.createEntrySpan(request.uri(), contextCarrier);
        Tags.URL.set(span, request.uri());
        Tags.HTTP.METHOD.set(span, request.method().name());
        span.setComponent(COMPONENT_NETTY_HTTP_SERVER);
        SpanLayer.asHttp(span);

        context.channel().attr(KEY_CONTEXT).set(getTracingContext());
    }

    public static void onServerResponse(HttpResponse response, ChannelHandlerContext context) {
        AbstractTracerContext tracingContext = context.channel().attr(KEY_CONTEXT).get();
        if (tracingContext == null) {
            return;
        }
        Tags.STATUS_CODE.set(tracingContext.activeSpan(), String.valueOf(response.status().code()));
        tracingContext.stopSpan(tracingContext.activeSpan()); /*stop entryspan */
    }

    public static void onClientRequest(HttpRequest request, ChannelHandlerContext context) {
        AbstractTracerContext tracingContext = context.channel().attr(KEY_CONTEXT).get();
        ContextCarrier contextCarrier = new ContextCarrier();
        String uri = request.uri();
        String remoteAddress = String.valueOf(context.channel().remoteAddress());
        AbstractSpan span = null;
        if (tracingContext != null) {
            span = tracingContext.createExitSpan(uri, remoteAddress.charAt(0) == '/' ? remoteAddress.substring(1) : remoteAddress);
            tracingContext.inject(contextCarrier);
        } else {
            if (ContextManager.isActive()) {
                setTracingContext(null); /* 说明上一个请求的response还没有收到, 该线程又开始处理新的请求, 需要把线程变量CONTEXT设置成null */
            }
            span = ContextManager.createExitSpan(uri, contextCarrier, remoteAddress.charAt(0) == '/' ? remoteAddress.substring(1) : remoteAddress);
            context.channel().attr(KEY_CONTEXT).set(getTracingContext());
        }
        Tags.URL.set(span, request.uri());
        Tags.HTTP.METHOD.set(span, request.method().name());
        span.setComponent(COMPONENT_NETTY_HTTP_CLIENT);
        SpanLayer.asHttp(span);
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            request.headers().set(next.getHeadKey(), next.getHeadValue());
        }
    }

    public static void onClientResponse(HttpResponse response, ChannelHandlerContext context) {
        AbstractTracerContext tracingContext = context.channel().attr(KEY_CONTEXT).get();
        if (tracingContext == null) {
            return;
        }
        Tags.STATUS_CODE.set(tracingContext.activeSpan(), String.valueOf(response.status().code()));
        tracingContext.stopSpan(tracingContext.activeSpan()); // stop exitspan
    }
}

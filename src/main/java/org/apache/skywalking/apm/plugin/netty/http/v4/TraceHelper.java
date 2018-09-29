/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.COMPONENT_NETTY_HTTP_CLIENT;
import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.COMPONENT_NETTY_HTTP_SERVER;
import static org.apache.skywalking.apm.plugin.netty.http.v4.Constants.KEY_CONTEXT_SNAPSHOT;

import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class TraceHelper {

    public static void onException(Throwable cause, ChannelHandlerContext context) {
        ContextSnapshot contextSnapshot = context.channel().attr(KEY_CONTEXT_SNAPSHOT).get();
        if (contextSnapshot == null) {
            return;
        }
        AbstractSpan span = ContextManager.createLocalSpan("netty-http/error");
        ContextManager.continued(contextSnapshot);
        span.errorOccurred().log(cause);
        ContextManager.stopSpan(); /*stop localspan*/
    }

    public static void onServerReceived(HttpRequest request, ChannelHandlerContext context) {
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

        context.channel().attr(KEY_CONTEXT_SNAPSHOT).set(ContextManager.capture());
    }

    public static void onServerSend(HttpResponse response, ChannelHandlerContext context) {
        ContextSnapshot contextSnapshot = context.channel().attr(KEY_CONTEXT_SNAPSHOT).getAndSet(null);
        if (contextSnapshot == null) {
            return;
        }
        ContextManager.createLocalSpan("netty-http-server/out"); /* only for continued success */
        ContextManager.continued(contextSnapshot);
        ContextManager.stopSpan(); /*stop localspan (netty-http-server/out) */

        Tags.STATUS_CODE.set(ContextManager.activeSpan(), String.valueOf(response.status().code()));
        ContextManager.stopSpan(); /*stop entryspan */
    }

    public static void onClientSend(HttpRequest request, ChannelHandlerContext context) {
        ContextManager.createLocalSpan("netty-http-client/out");
        ContextSnapshot contextSnapshot = context.channel().attr(KEY_CONTEXT_SNAPSHOT).get();
        if (contextSnapshot != null) {
            // 适用于前面没有span, 只是由client开始才创建第一个span
            ContextManager.continued(contextSnapshot);
        }
        ContextCarrier contextCarrier = new ContextCarrier();
        String uri = request.uri();
        String remoteAddress = String.valueOf(context.channel().remoteAddress());
        AbstractSpan span = ContextManager.createExitSpan(uri, contextCarrier, remoteAddress.charAt(0) == '/' ? remoteAddress.substring(1) : remoteAddress);
        Tags.URL.set(span, request.uri());
        Tags.HTTP.METHOD.set(span, request.method().name());
        span.setComponent(COMPONENT_NETTY_HTTP_CLIENT);
        SpanLayer.asHttp(span);
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            request.headers().set(next.getHeadKey(), next.getHeadValue());
        }
        context.channel().attr(KEY_CONTEXT_SNAPSHOT).set(ContextManager.capture());
    }

    public static void onClientReceived(HttpResponse response, ChannelHandlerContext context) {
        ContextSnapshot contextSnapshot = context.channel().attr(KEY_CONTEXT_SNAPSHOT).getAndSet(null);
        if (contextSnapshot == null) {
            return;
        }
        ContextManager.createLocalSpan("netty-http-client/in");
        ContextManager.continued(contextSnapshot);
        ContextManager.stopSpan(); /* stop localspan (netty-http-client/in) */

        Tags.STATUS_CODE.set(ContextManager.activeSpan(), String.valueOf(response.status().code()));
        ContextManager.stopSpan(); // stop exitspan
        ContextManager.stopSpan(); // stop localspan (netty-http-client/out)
    }
}

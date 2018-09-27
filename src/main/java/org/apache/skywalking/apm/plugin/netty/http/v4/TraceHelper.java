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
import io.netty.util.Attribute;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class TraceHelper {
    public static void receivedServerRequest(HttpRequest request, ChannelHandlerContext context) {
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
    }

    public static void sendServerResponse(HttpResponse response, ChannelHandlerContext context) {
        Tags.STATUS_CODE.set(ContextManager.activeSpan(), String.valueOf(response.status().code()));
        ContextManager.stopSpan();
    }

    public static void sendClientRequest(HttpRequest request, ChannelHandlerContext context) {
        Attribute<ContextSnapshot> attr = context.channel().attr(KEY_CONTEXT_SNAPSHOT);
        ContextSnapshot contextSnapshot = attr.get();
        attr.set(null);
        if (contextSnapshot == null) {
            return;
        }
        ContextManager.createLocalSpan("netty-http-client/local");
        ContextManager.continued(contextSnapshot);
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

    public static void receivedClientResponse(HttpResponse response, ChannelHandlerContext context) {
        Tags.STATUS_CODE.set(ContextManager.activeSpan(), String.valueOf(response.status().code()));
        ContextManager.stopSpan(); // stop exitspan
        ContextManager.stopSpan(); // stop localspan
    }
}

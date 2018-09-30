/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4;

import org.apache.skywalking.apm.agent.core.context.AbstractTracerContext;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.network.trace.component.Component;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

import io.netty.util.AttributeKey;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class Constants {
    @Deprecated
    public static final AttributeKey<ContextSnapshot> KEY_CONTEXT_SNAPSHOT = AttributeKey.valueOf("SW_CONTEXT_SNAPSHOT");
    public static final AttributeKey<AbstractTracerContext> KEY_CONTEXT = AttributeKey.valueOf("SW_CONTEXT");

    public static final Component COMPONENT_NETTY_HTTP_SERVER = ComponentsDefine.TOMCAT/*为了支持ui的Topology Map展示*/; /*new OfficialComponent(201, "netty-http-server");*/
    public static final Component COMPONENT_NETTY_HTTP_CLIENT = ComponentsDefine.HTTPCLIENT/*为了支持ui的Topology Map展示*/; /*new OfficialComponent(202, "netty-http-client");*/
}

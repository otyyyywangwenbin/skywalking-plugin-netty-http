/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4.define;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public class ChannelInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String ENHANCE_CLASS = "io.netty.channel.AbstractChannel";
    private static final String CONSTRUCTOR_INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.netty.http.v4.ChannelConstructorInterceptor";
    private static final String CHANNEL_WRITE_INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.netty.http.v4.ChannelWriteInterceptor";

    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[] {
                new ConstructorInterceptPoint() {
                    public String getConstructorInterceptor() {
                        return CONSTRUCTOR_INTERCEPT_CLASS;
                    }

                    public ElementMatcher<MethodDescription> getConstructorMatcher() {
                        return any();
                    }
                } };
    }

    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint() {
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("write").or(named("writeAndFlush"));
                    }

                    public String getMethodsInterceptor() {
                        return CHANNEL_WRITE_INTERCEPT_CLASS;
                    }

                    public boolean isOverrideArgs() {
                        return false;
                    }
                } };
    }
}

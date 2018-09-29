/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4.define;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static org.apache.skywalking.apm.agent.core.plugin.match.MultiClassNameMatch.byMultiClassMatch;

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

public class ChannelPoolInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String[] ENHANCE_CLASSES = new String[] {
            "io.netty.channel.pool.SimpleChannelPool",
            "io.netty.channel.pool.FixedChannelPool" };

    private static final String ACQUIRE_INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.netty.http.v4.ChannelPoolAcquireInterceptor";

    protected ClassMatch enhanceClass() {
        return byMultiClassMatch(ENHANCE_CLASSES);
    }

    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint() {
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("acquire").and(takesArguments(1));
                    }

                    public String getMethodsInterceptor() {
                        return ACQUIRE_INTERCEPT_CLASS;
                    }

                    public boolean isOverrideArgs() {
                        return false;
                    }
                } };
    }
}
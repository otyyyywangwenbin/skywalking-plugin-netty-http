/**
 * 
 */
package org.apache.skywalking.apm.plugin.netty.http.v4.define;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
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

public class HttpObjectEncoderInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    private static final String ENHANCE_CLASS = "io.netty.handler.codec.http.HttpObjectEncoder";
    private static final String ENCODE_INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.netty.http.v4.EncodeInterceptor";

    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint() {
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("encode").and(takesArguments(3));
                    }

                    public String getMethodsInterceptor() {
                        return ENCODE_INTERCEPT_CLASS;
                    }

                    public boolean isOverrideArgs() {
                        return false;
                    }
                } };
    }
}

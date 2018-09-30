## skywalking的netty http的插件

没有使用ContextManager的线程变量来处理AbstractTracerContext, 在netty的io模型中只使用线程变量没有办法正确记录span stack.     
现在是把AbstractTracerContext和netty的`io.netty.channel.AbstractChannel`绑定, 造成的结果是没有多trace segment     


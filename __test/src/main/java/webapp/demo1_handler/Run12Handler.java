package webapp.demo1_handler;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;

/**
 * 简单的http处理
 * */
@Mapping("/demo1/run12/*")
@Component
public class Run12Handler implements Handler {
    @Override
    public void handle(Context cxt) throws Exception {
        cxt.output("你好；");
        cxt.output("你好2；");
    }
}

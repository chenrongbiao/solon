package webapp.demo1_handler;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;

/**
 * 实现简单的 mvc 效果
 * */
@Mapping("/demo1/view/*")
@Component
public class ViewHandler implements Handler {
    @Override
    public void handle(Context cxt) throws Throwable {
        ModelAndView model = new ModelAndView("dock.ftl");
        model.put("title","dock");
        model.put("msg","你好 world! in XController");

        cxt.render(model);
    }
}

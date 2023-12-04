package webapp.demof_websocket;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Http;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Socket;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Context;

@Controller
public class WsDemoController {
    @Socket
    @Mapping("/demof/*/{id}")
    public void test(Context ctx, String id) throws Exception {
        if (ctx == null) {
            return;
        }

        System.out.println("WebSocket-PathVar-Mvc:Id: " + id);

        String msg = ctx.body();

        if (msg.equals("close")) {
            ctx.output("它叫我关了：" + msg);
            System.out.println("它叫我关了：" + msg + "!!!");
            ctx.close();//关掉
        } else {
            System.out.println(">>>>>>>>我收到了：" + msg + ": " + ctx.paramMap().toString());
            ctx.output("我收到了：" + msg + ": " + ctx.paramMap().toString());
        }
    }

    @Http
    @Mapping("/demof/websocket")
    public Object test_client(Context ctx, String id){
        ModelAndView mv = new ModelAndView("demof/websocket.ftl");
        mv.put("app_port", Solon.cfg().serverPort() + 10000);

        return mv;
    }
}

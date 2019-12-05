package webapp.demo5_rpc;

import org.noear.solon.XApp;
import org.noear.solon.annotation.XController;
import org.noear.solon.annotation.XMapping;
import org.noear.solon.core.XContext;
import org.noear.solon.core.XHandler;
import org.noear.solonclient.XProxy;
import org.noear.solonclient.serializer.SnackSerializer;

@XMapping("/demo5/rpctest/")
@XController
public class rpctest implements XHandler {
    @Override
    public void handle(XContext ctx) throws Throwable {

        String url = "http://localhost:" + XApp.global().port();

        rockapi client = new XProxy()
                .serializer(SnackSerializer.instance)
                .upstream(name -> url)
                .create(rockapi.class);

        Object val = client.test5();
        if (val == null) {
            return;
        }

        ctx.render(val);
    }
}

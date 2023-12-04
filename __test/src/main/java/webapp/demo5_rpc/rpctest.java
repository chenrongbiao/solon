package webapp.demo5_rpc;

import org.noear.nami.Nami;
import org.noear.nami.NamiAttachment;
import org.noear.nami.coder.snack3.SnackDecoder;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;

import java.util.HashMap;
import java.util.Map;

@Mapping("/demo5/rpctest/")
@Controller
public class rpctest implements Handler {
    @Override
    public void handle(Context ctx) throws Throwable {
        Map<String, Object> map = new HashMap<>();

        NamiAttachment.put("user_name", "noear");

        map.put("HttpChannel", httpOf());
        map.put("SocketChannel", socketOf());

        ctx.render(map);
    }

    private Object httpOf() {
        String root = "http://localhost:" + Solon.cfg().serverPort();

        rockapi client = Nami.builder()
                .decoder(SnackDecoder.instance)
                .upstream(() -> root)
                .create(rockapi.class);

        return client.test1(12);
    }

    private Object socketOf() {
        int _port = 20000 + Solon.cfg().serverPort();

        rockapi client = Nami.builder().upstream(() -> "tcp://localhost:" + _port)
                .create(rockapi.class);

        return client.test1(12);
    }
}

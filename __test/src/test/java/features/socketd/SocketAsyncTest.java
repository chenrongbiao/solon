package features.socketd;

import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.java_websocket.client.SimpleWebSocketClient;
import org.noear.snack.ONode;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.SimpleListener;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import webapp.App;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author noear 2022/3/11 created
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(App.class)
public class SocketAsyncTest {
    @Test
    public void test_async_message() throws Throwable {
        int _port = 8080 + 20000;

        CompletableFuture<Boolean> check = new CompletableFuture<>();

        Session session = SocketD.createClient("tcp://localhost:" + _port)
                .listen(new SimpleListener(){
                    @Override
                    public void onMessage(Session session, Message message) throws IOException {
                        System.out.println("异步发送::实例监到，收到了：" + message);
                        check.complete(true);
                    }
                })
                .open();


        String root = "tcp://localhost:" + _port;
        Map<String, Object> map = new HashMap<>();
        map.put("name", "noear");
        String map_josn = ONode.stringify(map);

        //异步发
        session.send(root + "/demoh/rpc/hello", new StringEntity(map_josn)
                .meta("Content-Type", "application/json"));

        assert check.get(2, TimeUnit.SECONDS);
    }

    @Test
    public void test_async_message2() throws Throwable {

        CompletableFuture<Boolean> check = new CompletableFuture<>();

        WebSocketClient webSocketClient = new SimpleWebSocketClient(URI.create("ws://127.0.0.1:18080/demof/websocket/12")){
            @Override
            public void onMessage(String message) {
                System.out.println("异步发送-ws::实例监到，收到了：" + message);
                check.complete(true);
            }
        };
        webSocketClient.connectBlocking();

        //异步发
        webSocketClient.send("test");

        assert check.get(2, TimeUnit.SECONDS);
    }


}

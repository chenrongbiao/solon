package org.noear.solon.boot.socketd;

import org.noear.socketd.SocketD;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.transport.server.ServerConfigHandler;
import org.noear.solon.Solon;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.prop.impl.SocketServerProps;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.core.util.RunUtil;
import org.noear.solon.net.socketd.SocketdRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author noear
 * @since 2.6
 */
public class XPluginImpl implements Plugin {
    private List<Server> serverList = new ArrayList<>();
    private SocketdRouter socketdRouter;
    private ServerConfigHandler serverConfigHandler;

    @Override
    public void start(AppContext context) throws Throwable {
        if (Solon.app().enableSocketD() == false) {
            return;
        }

        context.lifecycle(ServerConstants.SIGNAL_LIFECYCLE_INDEX, () -> {
            start0(context);
        });
    }

    private void start0(AppContext context) throws Throwable {
        socketdRouter = SocketdRouter.getInstance();
        serverConfigHandler = context.getBean(ServerConfigHandler.class);

        //初始化属性
        ServerProps.init();


        SocketServerProps serverProps = new SocketServerProps(20000);
        ExecutorService channelExecutor = serverProps.getBioExecutor("Socketd-channelExecutor-");

        Server serverTmp = SocketD.createServerOrNull("sd:tcp");
        if (serverTmp != null) {
            serverTmp.config(c -> c.channelExecutor(channelExecutor));
            startServer1(serverTmp, serverProps, 0);
        }

        serverTmp = SocketD.createServerOrNull("sd:udp");
        if (serverTmp != null) {
            serverTmp.config(c -> c.channelExecutor(channelExecutor));
            startServer1(serverTmp, serverProps, 1);
        }

        serverTmp = SocketD.createServerOrNull("sd:ws");
        if (serverTmp != null) {
            serverTmp.config(c -> c.channelExecutor(channelExecutor));
            startServer1(serverTmp, serverProps, 2);
        }

        if (serverList.size() == 0) {
            channelExecutor.shutdown();
            LogUtil.global().warn("Missing socketd server provider!");
        }
    }

    private void startServer1(Server server, SocketServerProps serverProps, int portAdd) throws Exception {
        long time_start = System.currentTimeMillis();

        int portReal = serverProps.getPort() + portAdd;

        serverList.add(server);

        server.config(c -> c.port(portReal).host(serverProps.getHost()));
        server.listen(socketdRouter.getListener());

        //用户配置扩展
        if (serverConfigHandler != null) {
            server.config(serverConfigHandler);
        }

        EventBus.publish(server);

        server.start();

        final String _wrapHost = serverProps.getWrapHost();
        final int _wrapPort = serverProps.getWrapPort() + portAdd;
        Signal _signal = new SignalSim(serverProps.getName(), _wrapHost, _wrapPort, "socketd", SignalType.SOCKET);
        Solon.app().signalAdd(_signal);

        long time_end = System.currentTimeMillis();

        String serverUrl = server.config().getSchema() + "://localhost:" + portReal;
        LogUtil.global().info("Connector:main: socket.d: Started ServerConnector@{" + serverUrl + "}");
        LogUtil.global().info("Server:main: socket.d: Started (" + server.title() + ") @" + (time_end - time_start) + "ms");
    }

    @Override
    public void stop() throws Throwable {
        for (Server server : serverList) {
            RunUtil.runAndTry(server::stop);
        }
    }
}

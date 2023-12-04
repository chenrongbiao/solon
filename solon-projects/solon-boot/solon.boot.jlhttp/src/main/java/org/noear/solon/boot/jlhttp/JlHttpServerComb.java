package org.noear.solon.boot.jlhttp;

import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.boot.http.HttpServerConfigure;
import org.noear.solon.core.handle.Handler;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 通过组合支持多端口模式
 *
 * @author noear
 * @since 2.2
 */
public class JlHttpServerComb implements HttpServerConfigure, ServerLifecycle {
    private Executor executor;
    private Handler handler;
    protected boolean enableSsl = true;
    protected SSLContext sslContext;
    protected Set<Integer> addHttpPorts = new LinkedHashSet<>();
    protected List<JlHttpServer> servers = new ArrayList<>();

    /**
     * 是否允许Ssl
     */
    @Override
    public void enableSsl(boolean enable, SSLContext sslContext) {
        this.enableSsl = enable;
        this.sslContext = sslContext;
    }

    /**
     * 添加 HttpPort（当 ssl 时，可再开个 http 端口）
     */
    @Override
    public void addHttpPort(int port) {
        addHttpPorts.add(port);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public boolean isSecure() {
        if (servers.size() > 0) {
            return servers.get(0).isSecure();
        } else {
            return false;
        }
    }

    @Override
    public void start(String host, int port) throws Throwable {
        {
            JlHttpServer s1 = new JlHttpServer();
            s1.setExecutor(executor);
            s1.setHandler(handler);
            s1.enableSsl(enableSsl, sslContext);
            s1.start(host, port);

            servers.add(s1);
        }

        for (Integer portAdd : addHttpPorts) {
            JlHttpServer s2 = new JlHttpServer();
            s2.setExecutor(executor);
            s2.setHandler(handler);
            s2.enableSsl(false, null); //只支持http
            s2.start(host, portAdd);

            servers.add(s2);
        }
    }

    @Override
    public void stop() throws Throwable {
        for (ServerLifecycle s : servers) {
            s.stop();
        }
    }
}

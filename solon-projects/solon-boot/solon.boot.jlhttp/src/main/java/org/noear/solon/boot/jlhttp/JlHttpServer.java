package org.noear.solon.boot.jlhttp;

import org.noear.solon.Utils;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.boot.ssl.SslConfig;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.lang.Nullable;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Executor;

/**
 * Jl Http Server（允许被复用）
 * @author noear
 * @since 2.2
 */
public class JlHttpServer implements ServerLifecycle {

    private HTTPServer server = null;
    private Handler handler;
    private Executor executor;
    private SslConfig sslConfig = new SslConfig(ServerConstants.SIGNAL_HTTP);
    private boolean isSecure;

    public boolean isSecure() {
        return isSecure;
    }


    public void enableSsl(boolean enable, @Nullable SSLContext sslContext) {
        sslConfig.set(enable, sslContext);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


    @Override
    public void start(String host, int port) throws Throwable {
        server = new HTTPServer();

        if (sslConfig.isSslEnable()) {
            // enable SSL if configured
            server.setServerSocketFactory(sslConfig.getSslContext().getServerSocketFactory());
            isSecure = true;
        }

        HTTPServer.VirtualHost virtualHost = server.getVirtualHost(null);
        virtualHost.setDirectoryIndex(null);
        virtualHost.addContext("/", new JlHttpContextHandler(handler), "*");

        server.setExecutor(executor);
        server.setPort(port);
        if (Utils.isNotEmpty(host)) {
            server.setHost(host);
        }
        server.start();
    }

    @Override
    public void stop() throws Throwable {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}

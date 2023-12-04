package org.noear.solon.boot.vertx;

import io.vertx.core.Vertx;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.prop.impl.HttpServerProps;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 2.3
 */
public class XPluginImp implements Plugin {
    Vertx vertx;

    @Override
    public void start(AppContext context) throws Throwable {
        if (Solon.app().enableHttp() == false) {
            return;
        }

        vertx = Vertx.vertx();

        context.wrapAndPut(Vertx.class, vertx);

        context.lifecycle(ServerConstants.SIGNAL_LIFECYCLE_INDEX, () -> {
            start0(Solon.app());
        });
    }

    private void start0( SolonApp app) throws Throwable {
        //初始化属性
        ServerProps.init();

        HttpServerProps props = HttpServerProps.getInstance();
        final String _host = props.getHost();
        final int _port = props.getPort();
        final String _name = props.getName();

        long time_start = System.currentTimeMillis();




        vertx.deployVerticle(new VertxHttpServer());
    }

    @Override
    public void stop() throws Throwable {
        if (vertx != null) {
            vertx.close();
            vertx = null;
        }
    }
}

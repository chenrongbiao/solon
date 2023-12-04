package org.noear.solon.boot.undertow;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.undertow.http.UtHttpContextServletHandler;
import org.noear.solon.boot.undertow.websocket.UtWsConnectionCallback;
import org.noear.solon.core.event.EventBus;

import static io.undertow.Handlers.websocket;

/**
 * @author  by: Yukai
 * @since : 2019/3/28 15:49
 */
public class UndertowServer extends UndertowServerBase implements ServerLifecycle {
    protected Undertow _server;
    private boolean isSecure;
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public void start(String host, int port) {
        try {
            setup(Solon.app(), host, port);

            _server.start();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.stop();
            _server = null;
        }
    }

    protected void setup(SolonApp app, String host, int port) throws Throwable {
        HttpHandler httpHandler = buildHandler();

        //************************** init server start******************
        Undertow.Builder builder = Undertow.builder();

        builder.setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false);

        if (ServerProps.request_maxHeaderSize > 0) {
            builder.setServerOption(UndertowOptions.MAX_HEADER_SIZE, ServerProps.request_maxHeaderSize);
        }

        if (ServerProps.request_maxBodySize > 0) {
            builder.setServerOption(UndertowOptions.MAX_ENTITY_SIZE, ServerProps.request_maxBodySize);
        }

        if (ServerProps.request_maxFileSize > 0) {
            builder.setServerOption(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE, ServerProps.request_maxFileSize);
        }


        if (props.getIdleTimeout() > 0) {
            builder.setServerOption(UndertowOptions.IDLE_TIMEOUT, (int) props.getIdleTimeout());
        }
        builder.setIoThreads(props.getCoreThreads());
        builder.setWorkerThreads(props.getMaxThreads(props.isIoBound()));

        if(enableHttp2){
            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
        }

        if (Utils.isEmpty(host)) {
            host = "0.0.0.0";
        }

        if (sslConfig.isSslEnable()) {
            //https
            builder.addHttpsListener(port, host, sslConfig.getSslContext());
            isSecure = true;
        } else {
            //http
            builder.addHttpListener(port, host);
        }

        //http add
        for(Integer portAdd: addHttpPorts){
            builder.addHttpListener(portAdd, host);
        }

        if (app.enableWebSocket()) {
            builder.setHandler(websocket(new UtWsConnectionCallback(), httpHandler));
        } else {
            builder.setHandler(httpHandler);
        }


        //1.1:分发事件（充许外部扩展）
        EventBus.publish(builder);

        _server = builder.build();

        //************************* init server end********************
    }

    protected HttpHandler buildHandler() throws Exception {
        DeploymentInfo builder = initDeploymentInfo();

        //添加servlet
        builder.addServlet(new ServletInfo("ACTServlet", UtHttpContextServletHandler.class).addMapping("/").setAsyncSupported(true));
        //builder.addInnerHandlerChainWrapper(h -> handler); //这个会使过滤器不能使用


        //开始部署
        final ServletContainer container = Servlets.defaultContainer();
        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();

        return manager.start();
    }
}

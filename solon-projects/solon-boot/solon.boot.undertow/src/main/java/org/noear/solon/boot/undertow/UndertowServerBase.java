package org.noear.solon.boot.undertow;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import org.noear.solon.Solon;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.prop.impl.HttpServerProps;
import org.noear.solon.boot.ssl.SslConfig;
import org.noear.solon.boot.undertow.http.UtContainerInitializer;
import org.noear.solon.boot.http.HttpServerConfigure;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.lang.Nullable;

import javax.net.ssl.SSLContext;
import javax.servlet.MultipartConfigElement;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;

abstract class UndertowServerBase implements ServerLifecycle, HttpServerConfigure {
    protected HttpServerProps props = HttpServerProps.getInstance();
    protected SslConfig sslConfig = new SslConfig(ServerConstants.SIGNAL_HTTP);
    protected boolean enableHttp2 = false;

    protected Set<Integer> addHttpPorts = new LinkedHashSet<>();

    /**
     * 是否允许Ssl
     */
    @Override
    public void enableSsl(boolean enable, @Nullable SSLContext sslContext) {
        sslConfig.set(enable, sslContext);
    }

    @Override
    public boolean isSupportedHttp2() {
        return true;
    }

    @Override
    public void enableHttp2(boolean enable) {
        this.enableHttp2 = enable;
    }

    /**
     * 添加 HttpPort（当 ssl 时，可再开个 http 端口）
     */
    @Override
    public void addHttpPort(int port) {
        addHttpPorts.add(port);
    }

    @Override
    public void setExecutor(Executor executor) {
        LogUtil.global().warn("Undertow does not support user-defined executor");
    }

    public HttpServerProps getProps() {
        return props;
    }

    protected DeploymentInfo initDeploymentInfo() {
        MultipartConfigElement configElement = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(XPluginImp.class.getClassLoader())
                .setDeploymentName("solon")
                .setContextPath("/")
                .setDefaultEncoding(ServerProps.request_encoding)
                .setDefaultMultipartConfig(configElement)
                .setClassIntrospecter(DefaultClassIntrospector.INSTANCE);

        //添加容器初始器
        builder.addServletContainerInitializer(UtContainerInitializer.info());
        builder.setEagerFilterInit(true);

        if (ServerProps.session_timeout > 0) {
            builder.setDefaultSessionTimeout(ServerProps.session_timeout);
        }

        return builder;
    }

    protected String getResourceRoot() throws FileNotFoundException {
        URL rootURL = getRootPath();
        if (rootURL == null) {
            if (NativeDetector.inNativeImage()) {
                return "";
            }

            throw new FileNotFoundException("Unable to find root");
        }

        String resURL = rootURL.toString();

        if (Solon.cfg().isDebugMode() && (resURL.startsWith("jar:") == false)) {
            int endIndex = resURL.indexOf("target");
            return resURL.substring(0, endIndex) + "src/main/resources/";
        }

        return "";
    }

    protected URL getRootPath() {
        URL root = ResourceUtil.getResource("/");
        if (root != null) {
            return root;
        }
        try {
            URL temp = ResourceUtil.getResource("");
            if (temp == null) {
                return null;
            }

            String path = temp.toString();
            if (path.startsWith("jar:")) {
                int endIndex = path.indexOf("!");
                path = path.substring(0, endIndex + 1) + "/";
            } else {
                return null;
            }
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
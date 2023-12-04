package org.noear.solon.net.websocket;

import org.noear.solon.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 会话接口基础
 *
 * @author noear
 * @since 2.6
 */
public abstract class WebSocketBase implements WebSocket {
    static final Logger log = LoggerFactory.getLogger(WebSocketBase.class);

    private final Map<String, Object> attrMap = new HashMap<>();
    private Handshake handshake;
    private boolean isClosed;
    private String pathNew;
    private String id = Utils.guid();

    protected void init(URI uri) {
        this.handshake = new HandshakeImpl(uri);
    }

    public boolean isClosed() {
        return isClosed;
    }


    /**
     * 会话id
     * */
    @Override
    public String id() {
        return id;
    }

    /**
     * 获取请求地址
     * */
    @Override
    public String url() {
        return handshake.getUrl();
    }

    /**
     * 获取请求路径
     */
    @Override
    public String path() {
        if (pathNew == null) {
            return handshake.getUri().getPath();
        } else {
            return pathNew;
        }
    }

    @Override
    public void pathNew(String pathNew) {
        this.pathNew = pathNew;
    }

    @Override
    public Map<String, String> paramMap() {
        return handshake.getParamMap();
    }

    @Override
    public String param(String name) {
        return handshake.getParamMap().get(name);
    }

    @Override
    public String paramOrDefault(String name, String def) {
        return handshake.getParamMap().getOrDefault(name, def);
    }

    @Override
    public void param(String name, String value) {
        handshake.getParamMap().put(name, value);
    }

    @Override
    public Map<String, Object> attrMap() {
        return attrMap;
    }

    @Override
    public boolean attrHas(String name) {
        return attrMap.containsKey(name);
    }

    @Override
    public <T> T attr(String name) {
        return (T) attrMap.get(name);
    }

    @Override
    public <T> T attrOrDefault(String name, T def) {
        return (T) attrMap.getOrDefault(name, def);
    }

    @Override
    public <T> void attr(String name, T value) {
        attrMap.put(name, value);
    }

    @Override
    public void close() {
        isClosed = true;
    }
}

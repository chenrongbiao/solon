package org.noear.solon.net.stomp;


import org.noear.solon.net.websocket.WebSocket;
import org.noear.solon.net.websocket.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * websocket转stomp处理
 *
 * @author limliu
 * @since 2.7
 */
public abstract class ToStompWebSocketListener implements WebSocketListener {
    static Logger log = LoggerFactory.getLogger(StompListenerImpl.class);

    private List<IStompListener> listenerList = new ArrayList<>();

    public ToStompWebSocketListener() {
        this(null);
    }

    public ToStompWebSocketListener(IStompListener listener) {
        this.addListener(new StompListenerImpl(), listener);
    }

    public void addListener(IStompListener... listeners) {
        if (listeners == null || listeners.length == 0) {
            return;
        }
        for (IStompListener listener : listeners) {
            if (listener == null) {
                continue;
            }
            listenerList.add(listener);
        }
    }


    @Override
    public void onOpen(WebSocket socket) {
        for (IStompListener listener : listenerList) {
            listener.onOpen(socket);
        }
    }

    @Override
    public void onMessage(WebSocket socket, String text) throws IOException {
        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
        StompUtil.msgCodec.decode(text, msg -> {
            atomicBoolean.set(Boolean.FALSE);
            String command = msg.getCommand() == null ? "" : msg.getCommand();
            switch (command) {
                case Commands.CONNECT: {
                    for (IStompListener listener : listenerList) {
                        listener.onConnect(socket, msg);
                    }
                    break;
                }
                case Commands.DISCONNECT: {
                    for (IStompListener listener : listenerList) {
                        listener.onDisconnect(socket, msg);
                    }
                    break;
                }
                case Commands.SUBSCRIBE: {
                    for (IStompListener listener : listenerList) {
                        listener.onSubscribe(socket, msg);
                    }
                    break;
                }
                case Commands.UNSUBSCRIBE: {
                    for (IStompListener listener : listenerList) {
                        listener.onUnsubscribe(socket, msg);
                    }
                    break;
                }
                case Commands.SEND: {
                    for (IStompListener listener : listenerList) {
                        listener.onSend(socket, msg);
                    }
                    break;
                }
                case Commands.ACK:
                case Commands.NACK: {
                    for (IStompListener listener : listenerList) {
                        listener.onAck(socket, msg);
                    }
                    break;
                }
                default: {
                    //未知命令
                    log.warn("session unknown, {}\r\n{}", socket.id(), text);
                    doSend(socket, new Message(Commands.UNKNOWN, text));
                }
            }
        });

        if (atomicBoolean.get()) {
            if (log.isDebugEnabled()) {
                log.debug("session ping, {}", socket.id());
            }
            //可能是ping，响应
            doSend(socket, new Message(Commands.MESSAGE, text));
        }
    }

    protected void doSend(WebSocket socket, Message message) {
        StompUtil.send(socket, message);
    }

    @Override
    public void onMessage(WebSocket socket, ByteBuffer binary) throws IOException {
        String txt = Charset.forName("UTF-8").decode(binary).toString();
        this.onMessage(socket, txt);
    }

    @Override
    public void onClose(WebSocket socket) {
        for (IStompListener listener : listenerList) {
            listener.onClose(socket);
        }
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
        log.error("", error);
    }
}
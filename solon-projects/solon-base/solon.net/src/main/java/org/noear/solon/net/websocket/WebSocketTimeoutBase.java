package org.noear.solon.net.websocket;

import org.noear.solon.core.util.RunUtil;

import java.util.concurrent.ScheduledFuture;

/**
 * @author noear
 * @since 2.6
 */
public abstract class WebSocketTimeoutBase extends WebSocketBase {

    private long idleTimeout;
    private ScheduledFuture<?> idleTimeoutFuture;
    private long liveTime;

    /**
     * 当发送时
     */
    public void onSend() {
        liveTime = System.currentTimeMillis();
    }

    /**
     * 当接收时
     */
    public void onReceive() {
        liveTime = System.currentTimeMillis();
    }

    @Override
    public long getIdleTimeout() {
        return idleTimeout;
    }

    @Override
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        this.liveTime = System.currentTimeMillis();

        checkIdleTimeoutFuture();
    }

    private synchronized void checkIdleTimeoutFuture() {
        if (idleTimeout > 0) {
            if (liveTime + idleTimeout < System.currentTimeMillis()) {
                if (this.isValid()) {
                    if (log.isDebugEnabled()) {
                        log.debug("WebSocket idle timeout, will close!");
                    }

                    RunUtil.runAndTry(this::close);
                    WebSocketRouter.getInstance().getListener().onClose(this);
                    return;
                }
            }

            if (idleTimeoutFuture != null) {
                idleTimeoutFuture.cancel(true);
            }

            idleTimeoutFuture = RunUtil.delay(this::checkIdleTimeoutFuture, idleTimeout);
        } else {
            if (idleTimeoutFuture != null) {
                idleTimeoutFuture.cancel(true);
                idleTimeoutFuture = null;
            }
        }
    }
}
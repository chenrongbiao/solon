package org.noear.solon.net.annotation;

import java.lang.annotation.*;

/**
 * @author noear
 * @since 2.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClientEndpoint {
    /**
     * 连接地址
     */
    String url();

    /**
     * 是否自动重链
     * */
    boolean autoReconnect() default true;

    /**
     * 心跳频率（单位：秒）
     */
    int heartbeatRate() default 20;
}
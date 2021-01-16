package org.noear.solon.extend.cloud.service;

import org.noear.solon.extend.cloud.model.Config;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author noear
 * @since 1.2
 */
public interface CloudConfigService {
    /**
     * 获取配置
     */
    Config get(String group, String key);

    /**
     * 设置配置
     */
    boolean set(String group, String key, String value);

    /**
     * 移除配置
     */
    boolean remove(String group, String key);

    /**
     * 关注配置
     */
    void attention(String group, String key, Consumer<Config> observer);
}

package org.noear.solon.data.cache;

import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * 支持标签的缓存服务
 *
 * @author 胡高
 * @since 1.10
 */
public interface CacheTagsService extends CacheService {

    /**
     * 获取或者存储
     *
     * @param key      缓存键
     * @param seconds  缓存秒数
     * @param supplier Represents a supplier of results.
     * @param tags     缓存标签
     */
    default <T> T getOrStoreTag(String key, Type type, int seconds, Supplier<T> supplier, String... tags) {
        Object obj = this.get(key, type);
        if (obj == null) {
            obj = supplier.get();
            for (String tag : tags) {
                this.storeTag(key, obj, seconds, tag);
            }
        }

        return (T) obj;
    }

    /**
     * 获取或者存储
     *
     * @param key      缓存键
     * @param seconds  缓存秒数
     * @param supplier Represents a supplier of results.
     * @param tags     缓存标签
     * @since 2.8
     */
    default <T> T getOrStoreTag(String key, Class<T> clz, int seconds, Supplier<T> supplier, String... tags) {
        return getOrStoreTag(key, (Type) clz, seconds, supplier, tags);
    }

    /**
     * 移除
     *
     * @param tags 缓存标签
     */
    void removeTag(String... tags);

    /**
     * 保存
     *
     * @param key     缓存键
     * @param obj     缓存对象
     * @param seconds 缓存秒数
     * @param tags    缓存标签
     */
    void storeTag(String key, Object obj, int seconds, String... tags);

}
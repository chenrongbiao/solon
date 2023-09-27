package org.noear.solon.boot.web;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.lang.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * @author noear
 * @since 1.11
 * @since 2.3
 */
public abstract class WebContextBase extends Context {

    @Override
    public String contentType() {
        return header(Constants.HEADER_CONTENT_TYPE);
    }


    private String contentCharset;

    @Override
    public String contentCharset() {
        if (contentCharset == null) {
            contentCharset = HeaderUtils.extractQuotedValueFromHeader(contentType(), "charset");

            if (Utils.isEmpty(contentCharset)) {
                contentCharset = ServerProps.request_encoding;
            }

            if (Utils.isEmpty(contentCharset)) {
                contentCharset = Solon.encoding();
            }
        }

        return contentCharset;
    }

    /**
     * 输出为文件
     */
    @Override
    public void outputAsFile(DownloadedFile file) throws IOException {
        OutputUtils.global().outputFile(this, file, file.isAttachment());
    }

    /**
     * 输出为文件
     */
    @Override
    public void outputAsFile(File file) throws IOException {
        OutputUtils.global().outputFile(this, file, true);
    }


    /**
     * 获取 sessionId
     */
    @Override
    public final String sessionId() {
        return sessionState().sessionId();
    }

    /**
     * 获取 session 状态
     *
     * @param name 状态名
     */
    @Override
    public final <T> T session(String name, Class<T> clz) {
        return sessionState().sessionGet(name, clz);
    }

    /**
     * 获取 session 状态（泛型转换，存在风险）
     *
     * @param name 状态名
     */
    @Override
    public final <T> T sessionOrDefault(String name, @NonNull T def) {
        if (def == null) {
            return (T) session(name, Object.class);
        }

        Object tmp = session(name, def.getClass());
        if (tmp == null) {
            return def;
        } else {
            return (T) tmp;
        }
    }

    /**
     * 获取 session 状态，并以 int 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final int sessionAsInt(String name) {
        return sessionAsInt(name, 0);
    }

    /**
     * 获取 session 状态，并以 int 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final int sessionAsInt(String name, int def) {
        Object tmp = session(name, Object.class);
        if (tmp == null) {
            return def;
        } else {
            if (tmp instanceof Number) {
                return ((Number) tmp).intValue();
            } else if (tmp instanceof String) {
                String str = (String) tmp;
                if (str.length() > 0) {
                    return Integer.parseInt(str);
                }
            }

            return def;
        }
    }

    /**
     * 获取 session 状态，并以 long 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final long sessionAsLong(String name) {
        return sessionAsLong(name, 0L);
    }

    /**
     * 获取 session 状态，并以 long 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final long sessionAsLong(String name, long def) {
        Object tmp = session(name, Object.class);
        if (tmp == null) {
            return def;
        } else {
            if (tmp instanceof Number) {
                return ((Number) tmp).longValue();
            } else if (tmp instanceof String) {
                String str = (String) tmp;
                if (str.length() > 0) {
                    return Long.parseLong(str);
                }
            }

            return def;
        }
    }

    /**
     * 获取 session 状态，并以 double 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final double sessionAsDouble(String name) {
        return sessionAsDouble(name, 0.0D);
    }

    /**
     * 获取 session 状态，并以 double 型输出
     *
     * @param name 状态名
     * @since 1.6
     */
    @Override
    public final double sessionAsDouble(String name, double def) {
        Object tmp = session(name, Object.class);
        if (tmp == null) {
            return def;
        } else {
            if (tmp instanceof Number) {
                return ((Number) tmp).doubleValue();
            } else if (tmp instanceof String) {
                String str = (String) tmp;
                if (str.length() > 0) {
                    return Double.parseDouble(str);
                }
            }

            return def;
        }
    }

    /**
     * 设置 session 状态
     *
     * @param name 状态名
     * @param val  值
     */
    @Override
    public final void sessionSet(String name, Object val) {
        sessionState().sessionSet(name, val);
    }

    /**
     * 移除 session 状态
     *
     * @param name 状态名
     */
    @Override
    public final void sessionRemove(String name) {
        sessionState().sessionRemove(name);
    }

    /**
     * 清空 session 状态
     */
    @Override
    public final void sessionClear() {
        sessionState().sessionClear();
    }


    //一些特殊的boot才有效
    protected void innerCommit() throws IOException {
    }
}

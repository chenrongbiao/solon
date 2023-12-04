package org.noear.solon.core.util;

/**
 * 日志打印小工具（仅限内部使用）
 *
 * @author noear
 * @since 1.10
 * */
public class LogUtil {
    private static LogUtil global;

    static {
        //（静态扩展约定：org.noear.solon.extend.impl.XxxxExt）
        global = ClassUtil.tryInstance("org.noear.solon.extend.impl.LogUtilExt");

        if (global == null) {
            global = new LogUtil();
        }
    }

    public static LogUtil global() {
        return global;
    }

    public void trace(String content) {
        System.out.print("[Solon] ");
        PrintUtil.purpleln(content);
    }

    public void debugAsync(String content) {
        RunUtil.async(() -> {
            debug(content);
        });
    }

    public void debug(String content) {
        System.out.print("[Solon] ");
        PrintUtil.blueln(content);
    }

    public void infoAsync(String content) {
        RunUtil.async(() -> {
            info(content);
        });
    }

    public void info(String content) {
        System.out.println("[Solon] " + content);
    }

    public void warn(String content) {
        warn(content, null);
    }

    public void warn(String content, Throwable throwable) {
        System.out.print("[Solon] ");
        PrintUtil.yellowln("WARN: " + content);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    public void error(String content) {
        error(content, null);
    }

    public void error(String content, Throwable throwable) {
        System.out.print("[Solon] ");
        PrintUtil.redln("ERROR: " + content);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}

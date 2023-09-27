package org.noear.solon.web.staticfiles;

import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.boot.web.OutputUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 静态文件资源处理
 *
 * @author noear
 * @since 1.0
 */
public class StaticResourceHandler implements Handler {
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final Date modified_time = new Date();


    @Override
    public void handle(Context ctx) throws Exception {
        if (ctx.getHandled()) {
            return;
        }

        if (MethodType.GET.name.equals(ctx.method()) == false) {
            return;
        }

        String path = ctx.pathNew();

        //找后缀名
        String suffix = findByExtName(path);
        if (Utils.isEmpty(suffix)) {
            return;
        }

        //找内容类型(先用配置的，再用jdk的)
        String conentType = StaticMimes.findByExt(suffix);

        if (Utils.isEmpty(conentType)) {
            conentType = Utils.mime(suffix);
        }

        //说明没有支持的mime
        if (Utils.isEmpty(conentType)) {
            return;
        }

        //找资源
        URL uri = StaticMappings.find(path);

        if (uri != null) {
            ctx.setHandled(true);

            String modified_since = ctx.header("If-Modified-Since");
            String modified_now = modified_time.toString();

            if (modified_since != null && StaticConfig.getCacheMaxAge() > 0) {
                if (modified_since.equals(modified_now)) {
                    ctx.headerSet(CACHE_CONTROL, "max-age=" + StaticConfig.getCacheMaxAge());//单位秒
                    ctx.headerSet(LAST_MODIFIED, modified_now);
                    ctx.status(304);
                    return;
                }
            }

            if (StaticConfig.getCacheMaxAge() > 0) {
                ctx.headerSet(CACHE_CONTROL, "max-age=" + StaticConfig.getCacheMaxAge());//单位秒
                ctx.headerSet(LAST_MODIFIED, modified_time.toString());
            }


            //一般情况下，文本类型的文件gzip才有价值,图片,excel这类文件gzip压缩率很低
            if (conentType.startsWith("text/")
                    || "application/json".equals(conentType)
                    || "application/javascript".equals(conentType)
            ) {
                //判断客户端是否支持gzip
                String acceptEncoding=ctx.headerOrDefault("Accept-Encoding", "");
                if (acceptEncoding.contains("br")) {
                    URL zipedFile = StaticMappings.find(path + ".br");
                    if (zipedFile != null) {
                        try (InputStream stream = zipedFile.openStream()) {
                            ctx.contentType(conentType);
                            ctx.headerSet("Vary", "Accept-Encoding");
                            ctx.headerSet("Content-Encoding", "br");
                            OutputUtils.global().outputStreamAsRange(ctx, stream, stream.available());
                        }
                        return;
                    }
                }
                if (acceptEncoding.contains("gzip")) {
                    URL zipedFile = StaticMappings.find(path + ".gz");
                    if (zipedFile != null) {
                        try (InputStream stream = zipedFile.openStream()) {
                            ctx.contentType(conentType);
                            ctx.headerSet("Vary", "Accept-Encoding");
                            ctx.headerSet("Content-Encoding", "gzip");
                            OutputUtils.global().outputStreamAsRange(ctx, stream, stream.available());
                        }
                        return;
                    }
                }
            }

            OutputUtils.global().outputFile(ctx, uri, conentType, StaticConfig.getCacheMaxAge() >= 0);
        }
    }


    /**
     * 尝试查找路径的后缀名
     */
    private String findByExtName(String path) {
        int pos = path.lastIndexOf(35); //'#'
        if (pos > 0) {
            path = path.substring(0, pos - 1);
        }

        pos = path.lastIndexOf(46); //'.'
        pos = Math.max(pos, path.lastIndexOf(47)); //'/'
        pos = Math.max(pos, path.lastIndexOf(63)); //'?'

        if (pos != -1 && path.charAt(pos) == '.') {
            return path.substring(pos).toLowerCase();
        } else {
            return null;
        }
    }
}

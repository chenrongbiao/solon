package org.noear.solon.view.enjoy;

import com.jfinal.template.Directive;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;
import com.jfinal.template.source.FileSourceFactory;
import org.noear.solon.Solon;
import org.noear.solon.core.AppClassLoader;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.Render;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.core.util.SupplierEx;
import org.noear.solon.view.ViewConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;

/**
 * Enjoy 视图渲染器
 *
 * @author noear
 * @since 1.0
 * */
public class EnjoyRender implements Render {
    static final Logger log = LoggerFactory.getLogger(EnjoyRender.class);

    private static EnjoyRender _global;

    public static EnjoyRender global() {
        if (_global == null) {
            _global = new EnjoyRender();
        }

        return _global;
    }



    private ClassLoader classLoader;
    private Engine provider = null;
    private Engine providerOfDebug = null;

    /**
     * 引擎提供者
     * */
    public Engine getProvider() {
        return provider;
    }

    /**
     * 引擎提供者（调试模式）
     * */
    public Engine getProviderOfDebug() {
        return providerOfDebug;
    }

    //不要要入参，方便后面多视图混用
    //
    public EnjoyRender() {
        this(AppClassLoader.global());
    }

    public EnjoyRender(ClassLoader classLoader) {
        this.classLoader = classLoader;

        //开始中文支持
        Engine.setChineseExpression(true);

        forDebug();
        forRelease();

        Solon.app().shared().forEach((k, v) -> {
            putVariable(k, v);
        });

        Solon.app().onSharedAdd((k, v) -> {
            putVariable(k, v);
        });
    }


    private void forDebug() {
        if (Solon.cfg().isDebugMode() == false) {
            return;
        }

        if (Solon.cfg().isFilesMode() == false) {
            return;
        }

        if (providerOfDebug != null) {
            return;
        }

        //添加调试模式
        URL rooturi = ResourceUtil.getResource("/");
        if (rooturi == null) {
            return;
        }

        providerOfDebug = Engine.create("debug");
        providerOfDebug.setDevMode(true);


        String rootdir = rooturi.toString().replace("target/classes/", "");
        File dir = null;

        if (rootdir.startsWith("file:")) {
            String dir_str = rootdir + "src/main/resources" + ViewConfig.getViewPrefix();
            dir = new File(URI.create(dir_str));
            if (!dir.exists()) {
                dir_str = rootdir + "src/main/webapp" + ViewConfig.getViewPrefix();
                dir = new File(URI.create(dir_str));
            }
        }

        try {
            if (dir != null && dir.exists()) {
                providerOfDebug.setBaseTemplatePath(dir.getPath());
                providerOfDebug.setSourceFactory(new FileSourceFactory());
            }

            //通过事件扩展
            EventBus.publish(providerOfDebug);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void forRelease() {
        if (provider != null) {
            return;
        }

        provider = Engine.use();
        provider.setDevMode(Solon.cfg().isDebugMode());

        try {
            provider.setBaseTemplatePath(ViewConfig.getViewPrefix());
            provider.setSourceFactory(new ClassPathSourceFactory2(classLoader));

            //通过事件扩展
            EventBus.publish(provider);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 添加共享指令（自定义标签）
     */
    public void putDirective(String name, Class<? extends Directive> clz) {
        try {
            provider.addDirective(name, clz);

            if (providerOfDebug != null) {
                providerOfDebug.addDirective(name, clz);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 添加共享变量
     */
    public void putVariable(String name, Object value) {
        try {
            provider.addSharedObject(name, value);

            if (providerOfDebug != null) {
                providerOfDebug.addSharedObject(name, value);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 添加共享模板函数
     */
    public void putFunction(String path) {
        try {
            provider.addSharedFunction(path);

            if (providerOfDebug != null) {
                providerOfDebug.addSharedFunction(path);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public void render(Object obj, Context ctx) throws Throwable {
        if (obj == null) {
            return;
        }

        if (obj instanceof ModelAndView) {
            render_mav((ModelAndView) obj, ctx, () -> ctx.outputStream());
        } else {
            ctx.output(obj.toString());
        }
    }

    @Override
    public String renderAndReturn(Object obj, Context ctx) throws Throwable {
        if (obj == null) {
            return null;
        }

        if (obj instanceof ModelAndView) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            render_mav((ModelAndView) obj, ctx, () -> outputStream);

            return outputStream.toString();
        } else {
            return obj.toString();
        }
    }

    public void render_mav(ModelAndView mv, Context ctx, SupplierEx<OutputStream> outputStream) throws Throwable {
        if (ctx.contentTypeNew() == null) {
            ctx.contentType("text/html;charset=utf-8");
        }

        if (ViewConfig.isOutputMeta()) {
            ctx.headerSet(ViewConfig.HEADER_VIEW_META, "EnjoyRender");
        }

        //添加 context 变量
        mv.putIfAbsent("context", ctx);

        Template template = null;

        if (providerOfDebug != null) {
            try {
                template = providerOfDebug.getTemplate(mv.view());
            } catch (Exception e) {
                //忽略不计
            }
        }

        if (template == null) {
            template = provider.getTemplate(mv.view());
        }

        PrintWriter writer = new PrintWriter(outputStream.get());
        template.render(mv.model(), writer);
        writer.flush();
    }
}

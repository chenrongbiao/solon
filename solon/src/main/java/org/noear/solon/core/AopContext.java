package org.noear.solon.core;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.core.handle.*;
import org.noear.solon.core.message.Listener;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.util.*;
import org.noear.solon.core.wrap.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Aop 上下文（ 为全局对象；热插拨的插件，会产生独立的上下文）
 *
 * 主要实现四个动作：
 * 1.bean 构建
 * 2.bean 注入（字段 或 参数）
 * 3.bean 提取
 * 4.bean 拦截
 *
 * @author noear
 * @since 1.0
 * */
public class AopContext extends BeanContainer {

    public AopContext() {
        this(null, null);
    }

    public AopContext(ClassLoader classLoader, Props props) {
        super(classLoader, props);
        initialize();
    }

    private final Map<Method, MethodWrap> methodCached = new HashMap<>();

    public MethodWrap methodGet(Method method) {
        MethodWrap mw = methodCached.get(method);
        if (mw == null) {
            synchronized (method) {
                mw = methodCached.get(method);
                if (mw == null) {
                    mw = new MethodWrap(this, method);
                    methodCached.put(method, mw);
                }
            }
        }

        return mw;
    }

    @Override
    public void clear() {
        super.clear();

        methodCached.clear();
        tryCreateCached.clear();
        loadDone = false;
        loadEvents.clear();
    }

    @Override
    protected BeanWrap wrapCreate(Class<?> type, Object bean, String name) {
        return new BeanWrap(this, type, bean, name);
    }

    public AopContext copy() {
        return copy(null, null);
    }

    public AopContext copy(ClassLoader classLoader, Props props) {
        AopContext tmp = new AopContext(classLoader, props);
        copyTo(tmp);
        return tmp;
    }

    /**
     * ::初始化（独立出 initialize，方便重写）
     */
    protected void initialize() {

        //注册 @Configuration 构建器
        beanBuilderAdd(Configuration.class, (clz, bw, anno) -> {
            //尝试导入注解配置（先导）//v1.12
            cfg().loadAdd(clz.getAnnotation(PropertySource.class));

            //尝试导入（可能会导入属性源，或小饼依赖的组件）
            for (Annotation a1 : clz.getAnnotations()) {
                if (a1 instanceof Import) {
                    beanImport((Import) a1);
                } else {
                    beanImport(a1.annotationType().getAnnotation(Import.class));
                }
            }


            //尝试注入属性
            beanInjectProperties(clz, bw.raw());

            //构建小饼
            for (Method m : ClassWrap.get(bw.clz()).getMethods()) {
                Bean m_an = m.getAnnotation(Bean.class);

                if (m_an != null) {
                    //增加条件检测
                    if (ConditionUtil.test(this, m) == false) {
                        continue;
                    }

                    //支持非公有函数
                    m.setAccessible(true);

                    MethodWrap mWrap = methodGet(m);

                    //有参数的bean，采用线程池处理；所以需要锁等待
                    //
                    tryBuildBean(m_an, mWrap, bw);

                }
            }

            //添加bean形态处理
            addBeanShape(clz, bw, 0,clz);

            //注册到容器 //Configuration 不进入二次注册
            //beanRegister(bw,bw.name(),bw.typed());

            //支持基类注册
            beanRegisterSup0(bw);
        });

        //注册 @Component 构建器
        beanBuilderAdd(Component.class, (clz, bw, anno) -> {
            String beanName = Utils.annoAlias(anno.value(), anno.name());

            bw.nameSet(beanName);
            bw.tagSet(anno.tag());
            bw.typedSet(anno.typed());

            //添加bean形态处理
            addBeanShape(clz, bw, anno.index(), clz);

            //注册到容器
            beanRegister(bw, beanName, anno.typed());

            //尝试提取函数
            beanExtract(bw);

            //单例，进行事件通知
            if (bw.singleton()) {
                EventBus.push(bw.raw()); //@deprecated
                //EventBus.push(bw); //@deprecated
                wrapPublish(bw);
            }
        });

        //注册 @Remoting 构建器
        beanBuilderAdd(Remoting.class, (clz, bw, anno) -> {
            //设置remoting状态
            bw.remotingSet(true);
            //注册到容器
            beanRegister(bw, "", false);
        });

        //注册 @Controller 构建器
        beanBuilderAdd(Controller.class, (clz, bw, anno) -> {
            new HandlerLoader(bw).load(Solon.app());
        });

        //注册 @ServerEndpoint 构建器
        beanBuilderAdd(ServerEndpoint.class, (clz, wrap, anno) -> {
            if (Listener.class.isAssignableFrom(clz)) {
                Listener l = wrap.raw();
                Solon.app().router().add(Utils.annoAlias(anno.value(), anno.path()), anno.method(), l);
            }
        });


        //注册 @Inject 注入器
        beanInjectorAdd(Inject.class, ((fwT, anno) -> {
            beanInject(fwT, anno.value(), anno.required(), anno.autoRefreshed());
        }));
    }

    /**
     * 添加bean的不同形态
     */
    private void addBeanShape(Class<?> clz, BeanWrap bw, int index, AnnotatedElement annoEl) {
        //Plugin
        if (Plugin.class.isAssignableFrom(clz)) {
            //如果是插件，则插入
            Solon.app().plug(bw.raw());
            return;
        }

        //EventListener
        if (EventListener.class.isAssignableFrom(clz)) {
            addEventListener(clz, bw);
            return;
        }

        //LoadBalance.Factory
        if (LoadBalance.Factory.class.isAssignableFrom(clz)) {
            Bridge.upstreamFactorySet(bw.raw());
        }

        //Handler
        if (Handler.class.isAssignableFrom(clz)) {
            Mapping mapping = annoEl.getAnnotation(Mapping.class);
            if (mapping != null) {
                Handler handler = bw.raw();
                Set<MethodType> v0 = MethodTypeUtil.findAndFill(new HashSet<>(), t -> annoEl.getAnnotation(t) != null);
                if (v0.size() == 0) {
                    v0 = new HashSet<>(Arrays.asList(mapping.method()));
                }
                Solon.app().add(mapping, v0, handler);
            }
        }

        //Filter
        if (Filter.class.isAssignableFrom(clz)) {
            Solon.app().filter(index, bw.raw());
        }

        //RouterInterceptor
        if(RouterInterceptor.class.isAssignableFrom(clz)){
            Solon.app().routerInterceptor(index, bw.raw());
        }
    }

    //添加事件监听
    private void addEventListener(Class<?> clz, BeanWrap bw) {
        Class<?>[] ets = GenericUtil.resolveTypeArguments(clz, EventListener.class);
        if (ets != null && ets.length > 0) {
            EventBus.subscribe(ets[0], bw.raw());
        }
    }

    //::提取

    /**
     * 为一个对象提取函数
     */
    public void beanExtract(BeanWrap bw) {
        if (bw == null) {
            return;
        }

        if (beanExtractors.size() == 0) {
            return;
        }

        ClassWrap clzWrap = ClassWrap.get(bw.clz());

        for (Method m : clzWrap.getMethods()) {
            for (Annotation a : m.getAnnotations()) {
                BeanExtractor be = beanExtractors.get(a.annotationType());

                if (be != null) {
                    try {
                        be.doExtract(bw, m, a);
                    } catch (Throwable e) {
                        e = Utils.throwableUnwrap(e);
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }


    //::注入

    /**
     * 为一个对象注入（可以重写）
     */
    public void beanInject(Object obj) {
        if (obj == null) {
            return;
        }

        ClassWrap clzWrap = ClassWrap.get(obj.getClass());

        //支持所有类注入，可能影响启动速度 //目前只支持 @Configuration 类注入
        //beanInjectProperties(clzWrap.clz(), obj);

        //支持父类注入
        for (Map.Entry<String, FieldWrap> kv : clzWrap.getFieldAllWraps().entrySet()) {
            Annotation[] annS = kv.getValue().annoS;
            if (annS.length > 0) {
                VarHolder varH = kv.getValue().holder(this, obj);
                tryInject(varH, annS);
            }
        }
    }

    ////////////

    /**
     * 根据配置导入bean
     */
    public void beanImport(Import anno) {
        if (anno != null) {
            //导入类（beanMake）
            for (Class<?> clz : anno.value()) {
                beanMake(clz);
            }

            //扫描包（beanScan）
            for (String pkg : anno.scanPackages()) {
                beanScan(pkg);
            }

            //扫描包（beanScan）
            for (Class<?> src : anno.scanPackageClasses()) {
                beanScan(src);
            }
        }
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(Class<?> source) {
        //确定文件夹名
        if (source.getPackage() != null) {
            beanScan(source.getClassLoader(), source.getPackage().getName());
        }
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(String basePackage) {
        beanScan(getClassLoader(), basePackage);
    }

    /**
     * ::扫描源下的所有 bean 及对应处理
     */
    public void beanScan(ClassLoader classLoader,String basePackage) {
        if (Utils.isEmpty(basePackage)) {
            return;
        }

        if (classLoader == null) {
            return;
        }

        String dir = basePackage.replace('.', '/');

        //扫描类文件并处理（采用两段式加载，可以部分bean先处理；剩下的为第二段处理）
        ScanUtil.scan(classLoader, dir, n -> n.endsWith(".class"))
                .stream()
                .sorted(Comparator.comparing(s -> s.length()))
                .forEach(name -> {
                    String className = name.substring(0, name.length() - 6);
                    className = className.replace("/", ".");

                    Class<?> clz = Utils.loadClass(classLoader, className);
                    if (clz != null) {
                        tryCreateBean(clz);
                    }
                });
    }

    /**
     * ::制造 bean 及对应处理
     */
    public BeanWrap beanMake(Class<?> clz) {
        //增加条件检测
        if(ConditionUtil.test(this, clz) == false) {
            return null;
        }

        //包装
        BeanWrap bw = wrap(clz, null);

        tryCreateBean(bw);

        //尝试入库
        putWrap(clz, bw);

        return bw;
    }


    ////////////////////////////////////////////////////
    //
    //

    /**
     * 尝试为bean注入
     */
    protected void tryInject(VarHolder varH, Annotation[] annS) {
        for (Annotation a : annS) {
            BeanInjector bi = beanInjectors.get(a.annotationType());
            if (bi != null) {
                bi.doInject(varH, a);
            }
        }
    }

    /**
     * 尝试生成 bean
     */
    protected void tryCreateBean(Class<?> clz) {
        tryCreateBean0(clz, (c, a) -> {
            //包装
            BeanWrap bw = this.wrap(clz, null);
            c.doBuild(clz, bw, a);
            //尝试入库
            this.putWrap(clz, bw);
        });
    }

    protected void tryCreateBean(BeanWrap bw) {
        tryCreateBean0(bw.clz(), (c, a) -> {
            c.doBuild(bw.clz(), bw, a);
        });
    }

    private final Set<Class<?>> tryCreateCached = new HashSet<>();

    protected void tryCreateBean0(Class<?> clz, BiConsumerEx<BeanBuilder, Annotation> consumer) {
        Annotation[] annS = clz.getDeclaredAnnotations();

        if (annS.length > 0) {
            //去重处理
            if (tryCreateCached.contains(clz)) {
                return;
            } else {
                tryCreateCached.add(clz);
            }

            //增加条件检测
            if(ConditionUtil.test(this, clz) == false){
                return;
            }

            for (Annotation a : annS) {
                BeanBuilder builder = beanBuilders.get(a.annotationType());
                if (builder != null) {
                    try {
                        consumer.accept(builder, a);
                    } catch (Throwable e) {
                        e = Utils.throwableUnwrap(e);
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 尝试构建 bean
     *
     * @param anno  bean 注解
     * @param mWrap 方法包装器
     * @param bw    bean 包装器
     */
    protected void tryBuildBean(Bean anno, MethodWrap mWrap, BeanWrap bw) throws Exception {
        int size2 = mWrap.getParamWraps().length;

        if (size2 == 0) {
            //0.没有参数
            tryBuildBeanDo(anno, mWrap, bw, new Object[]{});
//            Object raw = mWrap.invoke(bw.raw(), new Object[]{});
//            tryBuildBean0(mWrap, anno, raw);
        } else {
            //1.构建参数
            VarGather gather = new VarGather(bw, size2, (args2) -> {
                try {
                    //
                    //变量收集完成后，会回调此处
                    //
                    tryBuildBeanDo(anno, mWrap, bw, args2);
//                    Object raw = mWrap.invoke(bw.raw(), args2);
//                    tryBuildBean0(mWrap, anno, raw);
                } catch (Throwable e) {
                    e = Utils.throwableUnwrap(e);
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            });

            //1.1.添加要收集的参数；并为参数注入（注入是异步的；全部完成后，VarGather 会回调）
            for (ParamWrap p1 : mWrap.getParamWraps()) {
                VarHolder p2 = gather.add(p1.getParameter());
                tryParameterInject(p2, p1.getParameter());
            }
        }
    }

    protected void tryBuildBeanDo(Bean anno, MethodWrap mWrap, BeanWrap bw, Object[] args)throws Exception {
        Object raw = mWrap.invoke(bw.raw(), args);
        tryBuildBean0(mWrap, anno, raw);
    }

    protected void tryParameterInject(VarHolder varH, Parameter p) {
        Annotation[] annoS = p.getDeclaredAnnotations();

        if (annoS.length == 0) {
            beanInject(varH, null);
        } else {
            for (Annotation anno : annoS) {
                BeanInjector injector = beanInjectors.get(anno.annotationType());
                if (injector != null) {
                    injector.doInject(varH, anno);
                    break;
                }
            }
        }
    }

    protected void tryBuildBean0(MethodWrap mWrap, Bean anno, Object raw) {
        if (raw != null) {
            Class<?> beanClz = mWrap.getReturnType();
            Type beanGtp = mWrap.getGenericReturnType();

            //产生的bean，不再支持二次注入

            BeanWrap m_bw = null;
            if (raw instanceof BeanWrap) {
                m_bw = (BeanWrap) raw;
            } else {
                //@Bean 动态构建的bean, 可通过事件广播进行扩展
                EventBus.push(raw);//@deprecated

                //动态构建的bean，都用新生成wrap（否则会类型混乱）
                m_bw = wrapCreate(beanClz, raw, null);
            }

            String beanName = Utils.annoAlias(anno.value(), anno.name());

            m_bw.nameSet(beanName);
            m_bw.tagSet(anno.tag());
            m_bw.typedSet(anno.typed());

            //添加bean形态处理
            addBeanShape(m_bw.clz(), m_bw, anno.index(), mWrap.getMethod());

            //注册到容器
            beanRegister(m_bw, beanName, anno.typed());

            //尝试泛型注册(通过 name 实现)
            if (beanGtp instanceof ParameterizedType) {
                putWrap(beanGtp.getTypeName(), m_bw);
            }

            //@Bean 动态产生的 beanWrap（含 name,tag,attrs），进行事件通知
            //EventBus.push(m_bw); //@deprecated
            wrapPublish(m_bw);
        }
    }

    /////////

    //加载完成标志
    private boolean loadDone;
    //加载事件
    private final Set<RankEntity<Consumer<AopContext>>> loadEvents = new LinkedHashSet<>();

    //::bean事件处理

    /**
     * 添加bean加载完成事件
     */
    @Note("添加bean加载完成事件")
    public void beanOnloaded(Consumer<AopContext> fun) {
        beanOnloaded(0, fun);
    }

    @Note("添加bean加载完成事件")
    public void beanOnloaded(int index, Consumer<AopContext> fun) {
        loadEvents.add(new RankEntity<>(fun, index));

        //如果已加载完成，则直接返回
        if (loadDone) {
            fun.accept(this);
        }
    }

    /**
     * 完成加载时调用，会进行事件通知
     */
    public void beanLoaded() {
        loadDone = true;

        //执行加载事件（不用函数包装，是为了减少代码）
        loadEvents.stream()
                .sorted(Comparator.comparingInt(m -> m.index))
                .forEach(m -> m.target.accept(this));
    }
}
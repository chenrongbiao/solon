package org.noear.solon.core.aspect;

import org.noear.solon.core.wrap.MethodHolder;
import org.noear.solon.core.wrap.ParamWrap;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 方法调用者
 *
 * @author noear
 * @since 1.3
 */
public class Invocation {
    private final Object target;
    private final Object[] args;
    private Map<String, Object> argsMap;
    private final MethodHolder method;
    private final List<InterceptorEntity> interceptors;
    private int interceptorIndex = 0;

    public Invocation(Object target, Object[] args, MethodHolder method, List<InterceptorEntity> interceptors) {
        this.target = target;
        this.args = args;
        this.method = method;
        this.interceptors = interceptors;
    }

    /**
     * 目标对象
     */
    public Object target() {
        return target;
    }

    /**
     * 目标对象类
     */
    public Class<?> getTargetClz(){
        return target.getClass();
    }

    /**
     * 目标对象类注解
     */
    public  <T extends Annotation> T getTargetAnnotation(Class<T> annoClz) {
        return target.getClass().getAnnotation(annoClz);
    }

    /**
     * 参数
     */
    public Object[] args() {
        return args;
    }

    /**
     * 参数Map模式
     */
    public Map<String, Object> argsAsMap() {
        if (argsMap == null) {
            Map<String, Object> tmp = new LinkedHashMap<>();

            ParamWrap[] params = method.getParamWraps();

            for (int i = 0, len = params.length; i < len; i++) {
                tmp.put(params[i].getName(), args[i]);
            }

            //变成只读
            argsMap = Collections.unmodifiableMap(tmp);
        }


        return argsMap;
    }

    /**
     * 函数
     */
    public MethodHolder method() {
        return method;
    }

    /**
     * 函数注解
     */
    public  <T extends Annotation> T getMethodAnnotation(Class<T> annoClz) {
        return method.getAnnotation(annoClz);
    }

    /**
     * 调用
     */
    public Object invoke() throws Throwable {
        return interceptors.get(interceptorIndex++).doIntercept(this);
    }
}

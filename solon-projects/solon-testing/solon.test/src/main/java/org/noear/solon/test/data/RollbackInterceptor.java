package org.noear.solon.test.data;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.util.RunnableEx;
import org.noear.solon.data.annotation.TranAnno;
import org.noear.solon.data.tran.TranUtils;
import org.noear.solon.test.annotation.Rollback;
import org.noear.solon.test.annotation.TestRollback;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 回滚拦截器
 *
 * @author noear
 * @since 1.10
 */
public class RollbackInterceptor implements Interceptor {
    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        if (Solon.app() == null) {
            //没有容器，没法运行事务回滚
            return inv.invoke();
        } else {
            AtomicReference valRef = new AtomicReference();

            //尝试找函数上的
            Rollback anno = inv.getMethodAnnotation(Rollback.class);
            if (anno == null) {
                TestRollback annoTmp = inv.getMethodAnnotation(TestRollback.class);
                if (annoTmp != null) {
                    anno = new RollbackAnno(annoTmp);
                }
            }

            //尝试找类上的
            if (anno == null) {
                anno = inv.getTargetAnnotation(Rollback.class);
            }

            if (anno == null || anno.value() == false) {
                //如果没有注解，或者不需要强制回滚
                return inv.invoke();
            } else {
                //如果需要强制回滚
                rollbackDo(() -> {
                    valRef.set(inv.invoke());
                });

                return valRef.get();
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollbackDo(RunnableEx runnable) throws Throwable {
        try {
            //应用 //添加路由拦截器（放到最里层）
            Solon.app().chainManager().addInterceptorIfAbsent(RollbackRouterInterceptor.getInstance(), Integer.MAX_VALUE);

            //当前
            TranUtils.execute(new TranAnno(), () -> {
                runnable.run();
                throw new RollbackException();
            });
        } catch (Throwable e) {
            e = Utils.throwableUnwrap(e);
            if (e instanceof RollbackException) {
                System.out.println("@Rollback: the transaction has been rolled back!");
            } else {
                throw e;
            }
        } finally {
            //应用 //移除路由拦截器（恢复原状）
            Solon.app().chainManager().removeInterceptor(RollbackRouterInterceptor.class);
        }
    }
}

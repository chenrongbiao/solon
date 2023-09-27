package org.noear.solon.core.util;

import org.noear.solon.core.InjectGather;
import org.noear.solon.core.VarHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author noear
 * @since 2.5
 */
public class IndexUtil {
    /**
     * 构建生命周期执行顺序位
     */
    public static int buildLifecycleIndex(Class<?> clz) {
        return new IndexBuilder().buildIndex(clz);
    }

    /**
     * 构建变量收集器的检查顺序位
     */
    public static int buildGatherIndex(InjectGather g1, List<InjectGather> gathers) {
        Set<Class<?>> clazzStack = new HashSet<>();
        return buildGatherIndex0(g1, gathers, clazzStack);
    }

    private static int buildGatherIndex0(InjectGather g1, List<InjectGather> gathers, Set<Class<?>> clazzStack) {
        if (g1.index > 0) {
            return g1.index;
        }

        for (VarHolder v1 : g1.getVars()) {
            if (v1.isDone() == false) {
                if (clazzStack.contains(v1.getType())) {
                    //避免死循环
                    Optional<InjectGather> tmp = gathers.stream()
                            .filter(g2 -> g2.getOutType().equals(v1.getType()))
                            .findFirst();

                    if(tmp.isPresent()){
                        int index = tmp.get().index + 1;
                        if(g1.index < index){
                            g1.index = index;
                        }
                    }

                    continue;
                } else {
                    clazzStack.add(v1.getType());
                }

                Optional<InjectGather> tmp = gathers.stream()
                        .filter(g2 -> g2.getOutType().equals(v1.getType()))
                        .findFirst();

                if (tmp.isPresent()) {
                    int index = buildGatherIndex0(tmp.get(), gathers, clazzStack) + 1;

                    if (g1.index < index) {
                        g1.index = index;
                    }
                }
            }
        }

        return g1.index;
    }
}
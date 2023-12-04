package org.noear.solon.core.route;

import org.noear.solon.core.Constants;
import org.noear.solon.core.handle.*;

import java.util.*;

/**
 * @author noear
 * @since 1.3
 */
public class RouterDefault implements Router{
    //for handler
    private final RoutingTable<Handler>[] routesH;

    public RouterDefault() {
        routesH = new RoutingTableDefault[3];

        routesH[0] = new RoutingTableDefault<>();//before:0
        routesH[1] = new RoutingTableDefault<>();//main
        routesH[2] = new RoutingTableDefault<>();//after:2
    }
    /**
     * 添加路由关系 for Handler
     *
     * @param path 路径
     * @param endpoint 处理点
     * @param method 方法
     * @param index 顺序位
     * @param handler 处理接口
     */
    @Override
    public void add(String path, Endpoint endpoint, MethodType method, int index, Handler handler) {
        RoutingDefault routing = new RoutingDefault<>(path, method, index, handler);

        if (path.contains("*") || path.contains("{")) {
            routesH[endpoint.code].add(routing);
        } else {
            //没有*号的，优先
            routesH[endpoint.code].add(0, routing);
        }
    }

    @Override
    public void remove(String pathPrefix) {
        routesH[Endpoint.before.code].remove(pathPrefix);
        routesH[Endpoint.main.code].remove(pathPrefix);
        routesH[Endpoint.after.code].remove(pathPrefix);
    }

    /**
     * 获取某个处理点的所有路由记录
     *
     * @param endpoint 处理点
     * @return 处理点的所有路由记录
     * */
    @Override
    public Collection<Routing<Handler>> getAll(Endpoint endpoint){
        return routesH[endpoint.code].getAll();
    }



    /**
     * 区配一个处理（根据上下文）
     *
     * @param ctx 上下文
     * @param endpoint 处理点
     * @return 一个匹配的处理
     */
    @Override
    public Handler matchOne(Context ctx, Endpoint endpoint) {
        String pathNew = ctx.pathNew();
        MethodType method = MethodTypeUtil.valueOf(ctx.method());

        return routesH[endpoint.code].matchOne(pathNew, method);
    }

    @Override
    public Handler matchMain(Context ctx) {
        //不能从缓存里取，不然 pathNew 会有问题
        String pathNew = ctx.pathNew();
        MethodType method = MethodTypeUtil.valueOf(ctx.method());

        Result<Handler> result = routesH[Endpoint.main.code].matchOneAndStatus(pathNew, method);

        if (result.getData() != null) {
            ctx.attrSet(Constants.mainHandler, result.getData());
        } else {
            ctx.attrSet(Constants.mainStatus, result.getCode());
        }

        return result.getData();
    }

    /**
     * 区配多个处理（根据上下文）
     *
     * @param ctx 上下文
     * @param endpoint 处理点
     * @return 一批匹配的处理
     */
    @Override
    public List<Handler> matchMore(Context ctx, Endpoint endpoint) {
        String pathNew = ctx.pathNew();
        MethodType method = MethodTypeUtil.valueOf(ctx.method());

        return routesH[endpoint.code].matchMore(pathNew, method);
    }

    /**
     * 清空路由关系
     */
    @Override
    public void clear() {
        routesH[0].clear();
        routesH[1].clear();
        routesH[2].clear();
    }
}

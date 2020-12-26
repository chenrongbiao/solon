package org.noear.solon.extend.consul;

import com.ecwid.consul.v1.ConsulClient;

import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.*;

import java.util.*;

/**
 * 集成Consul,配置application.properties:
 *
 * #应用ID
 * application.name=solon-consul-test
 * #应用服务ID,其中应用ID用来标示唯一的一个服务，服务ID标示一组相同的服务
 * application.service=solon-consul-test
 * #服务发现，把自身注册到consul，默认为true
 * consul.discovery.enable=true
 * #开启负载均衡，可在使用Nami,Feign等插件时，直接使用`服务ID`调用RPC服务,默认false
 * consul.locator.enable=true
 * 负载自动刷新，单位ms,值<=0时不自动刷新,默认为10000（10s）
 * consul.locator.interval=10000
 * #开启配置获取，默认为false
 * consul.config.enable=true
 * #配置自动刷新，单位ms,值<=0时不自动刷新,默认为10000（10s）
 * consul.config.interval=10000
 *
 * @author 夜の孤城
 * @since 1.2
 * */
public class XPluginImp implements Plugin {
    private Timer clientTimer = new Timer();
    private ConsulClient client;
    private String serviceId;
    @Override
    public void start(SolonApp app) {
        String host = app.cfg().get(Constants.HOST);

        if (Utils.isEmpty(host)) {
            return;
        }

        client = new ConsulClient(host);
        serviceId=app.cfg().get(Constants.APP_NAME) + "-" + app.port();
        // 1.Discovery::尝试注册服务
        if (app.cfg().getBool(Constants.DISCOVERY_ENABLE, true)) {
            new ConsulRegisterTask(client).run();
        }

        // 2.Config::尝试获取配置
        if (app.cfg().getBool(Constants.CONFIG_ENABLE, false)) {
            ConsulConfigTask configTask=new ConsulConfigTask(client);
            //开始先获取一下配置，避免使用@Inject("${prop.name}")这种配置方式获取的值位null
            configTask.run();
            long interval=app.cfg().getLong(Constants.CONFIG_INTERVAL,10000);
            if(interval>0){
                clientTimer.schedule(configTask, interval, interval);
            }

        }

        // 3.Locator::尝试获取负载
        if (app.cfg().getBool(Constants.LOCATOR_ENABLE, false)) {
            LoadBalanceSimpleFactory factory = new LoadBalanceSimpleFactory();
            Bridge.upstreamFactorySet(factory);
            long interval=app.cfg().getLong(Constants.LOCATOR_INTERVAL,10000);
            ConsulLocatorTask locatorTask=new ConsulLocatorTask(client, factory);
            locatorTask.run();
            if(interval>0){
                clientTimer.schedule(locatorTask, interval, interval);
            }
        }
    }

    @Override
    public void stop() throws Throwable {
        if(client!=null){
            client.agentServiceDeregister(serviceId);
        }
    }
}

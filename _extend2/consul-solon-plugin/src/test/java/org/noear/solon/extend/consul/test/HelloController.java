package org.noear.solon.extend.consul.test;

import org.noear.nami.annotation.NamiClient;
import org.noear.solon.Solon;
import org.noear.solon.SolonProps;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.Props;

@Controller
public class HelloController {

    @NamiClient("solon-consul-test")
    HelloInterface helloInterface;

    @Mapping("/hello")
    public String sayHello(){

        return "config:"+Solon.cfg().get("hello")+",rpc:"+helloInterface.hello0();
    }

    @Mapping("/hello0")
    public String sayHello0(){
        return "hello0";
    }
}

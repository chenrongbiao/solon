package features;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;

/**
 * @author noear 2021/12/17 created
 */
@Slf4j
@Component
public class App {
    public static void main(String[] args) {
        Solon.start(App.class, args);
    }

//    @Init
//    public void init() {
//        throw new IllegalStateException("测试一下");
//    }
}

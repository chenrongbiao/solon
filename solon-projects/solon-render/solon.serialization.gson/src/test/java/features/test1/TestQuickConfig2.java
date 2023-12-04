package features.test1;

import features.model.UserDo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.serialization.gson.GsonRenderFactory;
import org.noear.solon.test.SolonJUnit5Extension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 只对时间进行格式化
 */
@Import(profiles = "classpath:features2_test1-2.yml")
@ExtendWith(SolonJUnit5Extension.class)
public class TestQuickConfig2 {
    @Inject
    GsonRenderFactory renderFactory;

    @Test
    public void hello2() throws Throwable{
        UserDo userDo = new UserDo();

        Map<String, Object> data = new HashMap<>();
        data.put("time", new Date(1673861993477L));
        data.put("long", 12L);
        data.put("int", 12);
        data.put("null", null);

        userDo.setMap1(data);

        ContextEmpty ctx = new ContextEmpty();
        renderFactory.create().render(userDo, ctx);
        String output = ctx.attr("output");

        System.out.println(output);

        assert ONode.load(output).count() == 5;

        //完美
        assert "{\"s1\":\"noear\",\"b1\":true,\"n1\":1,\"d1\":1.0,\"map1\":{\"time\":1673861993477,\"long\":12,\"int\":12}}".equals(output);
    }
}

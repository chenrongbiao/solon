package features;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import webapp.App;
import webapp.demo6_aop.beans.DnBean;
import webapp.demo6_aop.beans.DsBean;
import webapp.demo6_aop.beans.GtBean;

import java.util.List;
import java.util.Map;

/**
 * @author noear 2023/10/10 created
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(App.class)
public class InjectTest2 {
    @Inject
    Map<String, DnBean> dnBeanMap;

    @Inject
    List<DsBean> dsBeanList;

    @Inject
    List<GtBean> gtBeanList;


    @Test
    public void inject_dsBeanList() {
        assert dsBeanList != null;
        assert dsBeanList.size() == 2;
    }

    @Test
    public void inject_dnBeanMap() {
        assert dnBeanMap != null;
        assert dnBeanMap.size() == 2;
        assert dnBeanMap.get("DnBean2") != null;
    }

    @Test
    public void inject_gtBeanList() {
        assert gtBeanList != null;
        assert gtBeanList.size() == 2;
    }
}

package features.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.i18n.I18nBundle;
import org.noear.solon.i18n.I18nService;
import org.noear.solon.i18n.I18nUtil;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import webapp.App;

import java.util.List;
import java.util.Locale;

/**
 * @author noear 2021/9/19 created
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(App.class)
public class I18nUtilTest {
    @Test
    public void test() {
        assert "登录".equals(I18nUtil.getMessage(Locale.CHINA, "login.title"));
        assert "login-us".equals(I18nUtil.getMessage(Locale.US, "login.title"));
    }

    @Test
    public void test1() {
        I18nBundle bundle = I18nUtil.getBundle(I18nUtil.getMessageBundleName(), Locale.CHINA);
        assert "登录".equals(bundle.get("login.title"));
    }

    I18nService service = new I18nService(I18nUtil.getMessageBundleName());

    @Test
    public void test2() {
        assert "登录".equals(service.get(Locale.CHINA, "login.title"));
        assert "login-us".equals(service.get(Locale.US, "login.title"));
    }

    @Test
    public void test3() {
        assert "xxx".equals(service.get(Locale.CHINA, "login.name"));
        assert "登录".equals(service.get(Locale.JAPAN, "login.title"));
    }

    @Test
    public void test4(){
         List list = service.toProps(Locale.CHINA).getBean("site.urls", List.class);

         assert list!=null;
         assert list.size() == 2;
    }
}

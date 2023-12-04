package features;


import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.snack.ONode;
import org.noear.solon.boot.web.Constants;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import webapp.App;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(App.class)
public class HeaderTest extends HttpTester {
    @Test
    public void test1() throws Exception {
        assert path("/demo1/run0/?str=").get().equals("不是null(ok)");

        Map<String, String> map = new LinkedHashMap<>();
        map.put("address", "192.168.1.1:9373");
        map.put("service", "wateradmin");
        map.put("meta", "");
        map.put("check_type", "0");
        map.put("is_unstable", "0");
        map.put("check_url", "/_run/check/");

        assert path("/demo2/header/")
                .header("Water-Trace-Id", "")
                .header("Water-From", "wateradmin@192.168.1.1:9373")
                .data(map).post().equals("");
    }

    @Test
    public void test1_header2() throws Exception {
        String json = path("/demo2/header2/")
                .headerAdd("test", "a")
                .headerAdd("test", "b")
                .get();

        assert json.length() > 0;
        assert json.contains("a");
        assert json.contains("b");
    }

    @Test
    public void test1_remote() throws IOException {
        String json = path("/demo2/remote/").get();

        assert json.length() > 0;
        ONode oNode = ONode.load(json);
        assert oNode.isArray();
        assert oNode.get(1).val().getRaw() instanceof Number;
        assert oNode.get(1).getInt() > 80;
    }

    @Test
    public void test2() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("address", "192.168.1.1:9373");
        map.put("service", "wateradmin");
        map.put("meta", "");
        map.put("check_type", "0");
        map.put("is_unstable", "0");
        map.put("check_url", "/_run/check/");

        assert path("/demo2/header/")
                .header("Water-Trace-Id", "a")
                .header("Water-From", "wateradmin@192.168.1.1:9373")
                .data(map).post().equals("a");
    }

    @Test
    public void test3() throws Exception {
        Response res = path("/demo2/cookie/").exec("GET");

        List<String> tmp = res.headers("Set-Cookie");

        assert tmp.size() >= 2;
    }

    @Test
    public void testContentLength() throws Exception {
        Response res = path("/demo1/header/hello").exec("GET");

        List<String> tmp = res.headers(Constants.HEADER_CONTENT_LENGTH);
        assert tmp != null;
        assert tmp.size() == 1;
        long size = Long.parseLong(tmp.get(0));
        byte[] bytes = res.body().bytes();
        assert size == bytes.length;
        assert "Hello world!".equals(new String(bytes));
    }

    @Test
    public void testContentType() throws Exception {
        String rst = path("/demo2/header/ct").data("name", "solon").post();
        assert rst.equals("POST::application/x-www-form-urlencoded::solon");


        rst = path("/demo2/header/ct").data("name", "solon").multipart(true).post();
        assert rst.startsWith("POST::multipart/form-data");
        assert rst.endsWith("::solon");
        assert rst.equals("POST::multipart/form-data::solon") == false;


        rst = path("/demo2/header/ct?name=solon").get();
        assert rst.equals("GET::null::solon");
    }
}

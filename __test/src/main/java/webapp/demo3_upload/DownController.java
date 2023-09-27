package webapp.demo3_upload;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.boot.web.OutputUtils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.util.ResourceUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author noear 2021/2/10 created
 */
@Mapping("/demo3/down")
@Controller
public class DownController {
    @Mapping("f1")
    public DownloadedFile down1() {
        InputStream stream = new ByteArrayInputStream("{code:1}".getBytes(StandardCharsets.UTF_8));

        //使用 InputStream 实例化
        return new DownloadedFile("text/json", stream, "test.json");
    }

    @Mapping("f2")
    public DownloadedFile down2() {
        byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);

        //使用 byte[] 实例化
        return new DownloadedFile("text/json", bytes, "test.txt");

    }

    @Mapping("f3")
    public File down3() {
        String filePath = ResourceUtil.getResource("WEB-INF/static/debug.htm").getFile();

        return new File(filePath);
    }

    @Mapping("f3_2")
    public DownloadedFile down3_2() throws Exception {
        String filePath = ResourceUtil.getResource("WEB-INF/static/debug.htm").getFile();

        File file = new File(filePath);
        return new DownloadedFile(file).asAttachment(false);
    }

    @Mapping("f4")
    public void down4(Context ctx) throws IOException {
        String filePath = ResourceUtil.getResource("WEB-INF/static/debug.htm").getFile();

        File file = new File(filePath);

        ctx.outputAsFile(file);
    }

    @Mapping("f4_2")
    public void down4_2(Context ctx) throws IOException {
        try (InputStream stream = ResourceUtil.getResource("WEB-INF/static/debug.htm").openStream()) {
            OutputUtils.global().outputStreamAsGzip(ctx, stream);
        }
    }
}

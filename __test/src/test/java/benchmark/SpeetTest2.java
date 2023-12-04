package benchmark;

import org.junit.jupiter.api.Test;
import org.noear.solon.web.staticfiles.StaticMimes;

public class SpeetTest2 {
    long time_start;
    long time_end;

    @Test
    public void test1() {

        String path1 = "/file.txt";
        String path2 = "/file.eot";


        System.out.println(StaticMimes.findByFileName(path1));
        time_start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            StaticMimes.findByFileName(path1);
        }
        time_end = System.currentTimeMillis();
        System.out.println("path1: " + (time_end - time_start));


        System.out.println(StaticMimes.findByFileName(path2));
        time_start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            StaticMimes.findByFileName(path2);
        }
        time_end = System.currentTimeMillis();
        System.out.println("path2: " + (time_end - time_start));
    }
}

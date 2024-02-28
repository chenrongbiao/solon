package org.noear.solon.docs.util;

import org.noear.solon.docs.DocDocket;
import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * @author noear
 * @since 2.3
 */
public class BasicAuthUtil {
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String base64DecodeToStr(String value) {
        byte[] decodedValue = Base64.getDecoder().decode(value);
        return new String(decodedValue, UTF_8);
    }

    /**
     * WWW-Authenticate 简单认证
     */
    public static boolean basicAuth(Context ctx, DocDocket docket) throws IOException {
        if (docket.basicAuth() == null || docket.basicAuth().size() == 0) {
            // 未启用简单认证
            return true;
        }

        String authorization = ctx.header("Authorization");
        if (Utils.isBlank(authorization)) {
            // 请求头无认证信息
            return false;
        }


        String nameAndPwd = base64DecodeToStr(authorization.substring(6));
        String[] upArr = nameAndPwd.split(":");

        if (upArr.length != 2) {
            return false;
        }
        String iptName = upArr[0];
        String iptPwd = upArr[1];

        return iptPwd.equals(docket.basicAuth().get(iptName));
    }

    public static void response401(Context ctx) throws IOException {
        ctx.status(401);
        ctx.headerSet("WWW-Authenticate", "Basic realm=\"knife4j\"");
        ctx.output("无权限访问");
    }
}

package org.noear.solon.boot.rapidoid_http_fast;

import org.noear.solon.XApp;
import org.noear.solon.core.XPlugin;

import java.io.Closeable;
import java.io.IOException;

public final class XPluginImp implements XPlugin {


    public static String solon_boot_ver(){
        return "jlhttp 2.4/1.0.3.6";
    }

    @Override
    public  void start(XApp app) {
        if(app.enableWeb() == false){
            return;
        }
    }

    @Override
    public void stop() throws Throwable {

    }
}

package com.fujieid.jap.solon.http.controller;

import com.fujieid.jap.core.result.JapResponse;
import com.fujieid.jap.http.adapter.jakarta.JakartaRequestAdapter;
import com.fujieid.jap.http.adapter.jakarta.JakartaResponseAdapter;
import com.fujieid.jap.solon.HttpServletRequestWrapperImpl;
import com.fujieid.jap.solon.JapProps;
import com.fujieid.jap.solon.JapSolonConfig;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 颖
 * @author work
 */
public class SimpleController extends JapController {

    @Inject
    JapProps japProperties;
    @Inject
    JapSolonConfig japSolonConfig;

    @Post
    @Mapping("/login")
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        request = new HttpServletRequestWrapperImpl(Context.current(), request);

        JapResponse japResponse = this.japSolonConfig.getSimpleStrategy()
                .authenticate(
                        this.japProperties.getSimple(),
                        new JakartaRequestAdapter(request),
                        new JakartaResponseAdapter(response)
                );

        return this.simpleResponse(japResponse);
    }

}

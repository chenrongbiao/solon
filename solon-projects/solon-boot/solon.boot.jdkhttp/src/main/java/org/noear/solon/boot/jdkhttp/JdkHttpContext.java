package org.noear.solon.boot.jdkhttp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.noear.solon.Utils;
import org.noear.solon.boot.web.HeaderUtils;
import org.noear.solon.boot.web.WebContextBase;
import org.noear.solon.boot.web.Constants;
import org.noear.solon.boot.web.RedirectUtils;
import org.noear.solon.core.handle.ContextAsyncListener;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.core.NvMap;
import org.noear.solon.core.util.IgnoreCaseMap;
import org.noear.solon.core.util.IoUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JdkHttpContext extends WebContextBase {
    private HttpExchange _exchange;
    private Map<String, Object> _parameters;
    protected Map<String, List<UploadedFile>> _fileMap;

    private boolean _isAsync;
    private long _asyncTimeout = 30000L; //默认30秒
    private CompletableFuture<Object> _asyncFuture;
    private List<ContextAsyncListener> _asyncListeners = new ArrayList<>();

    protected boolean innerIsAsync() {
        return _isAsync;
    }


    public JdkHttpContext(HttpExchange exchange) {
        _exchange = exchange;
        _parameters = (Map<String, Object>) _exchange.getAttribute("parameters");
        _fileMap = new HashMap<>();
    }

    private boolean _loadMultipartFormData = false;

    private void loadMultipartFormData() throws IOException {
        if (_loadMultipartFormData) {
            return;
        } else {
            _loadMultipartFormData = true;
        }

        //文件上传需要
        if (isMultipartFormData()) {
            MultipartUtil.buildParamsAndFiles(this);
        }
    }

    @Override
    public Object request() {
        return _exchange;
    }


    @Override
    public String remoteIp() {
        return _exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public int remotePort() {
        return _exchange.getRemoteAddress().getPort();
    }

    @Override
    public String method() {
        return _exchange.getRequestMethod();
    }

    @Override
    public String protocol() {
        return _exchange.getProtocol();
    }

    private URI _uri;

    @Override
    public URI uri() {
        if (_uri == null) {
            _uri = URI.create(url());
        }

        return _uri;
    }

    @Override
    public boolean isSecure() {
        return "https".equals(uri().getScheme());
    }

    private String _url;

    @Override
    public String url() {
        if (_url == null) {
            _url = _exchange.getRequestURI().toString();

            if (_url != null) {
                if (_url.startsWith("/")) {
                    String host = header(Constants.HEADER_HOST);

                    if (host == null) {
                        host = header(":authority");
                        String scheme = header(":scheme");

                        if (host == null) {
                            host = "localhost";
                        }

                        if (scheme != null) {
                            _url = "https://" + host + _url;
                        } else {
                            _url = scheme + "://" + host + _url;
                        }

                    } else {
                        _url = "http://" + host + _url;
                    }
                }

                int idx = _url.indexOf("?");
                if (idx > 0) {
                    _url = _url.substring(0, idx);
                }
            }
        }

        return _url;
    }

    @Override
    public long contentLength() {
        return HeaderUtils.getContentLengthLong(this);
    }

    @Override
    public String queryString() {
        return _exchange.getRequestURI().getQuery();
    }

    @Override
    public InputStream bodyAsStream() throws IOException {
        return _exchange.getRequestBody();
    }

    private NvMap _paramMap;

    @Override
    public NvMap paramMap() {
        if (_paramMap == null) {
            _paramMap = new NvMap();

            try {
                if (autoMultipart()) {
                    loadMultipartFormData();
                }

                _parameters.forEach((k, v) -> {
                    if (v instanceof List) {
                        _paramMap.put(k, ((List<String>) v).get(0));
                    } else {
                        _paramMap.put(k, (String) v);
                    }
                });
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return _paramMap;
    }

    private Map<String, List<String>> _paramsMap;

    @Override
    public Map<String, List<String>> paramsMap() {
        if (_paramsMap == null) {
            _paramsMap = new LinkedHashMap<>();

            try {
                if (autoMultipart()) {
                    loadMultipartFormData();
                }

                _parameters.forEach((k, v) -> {
                    if (v instanceof List) {
                        _paramsMap.put(k, (List<String>) v);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add((String) v);
                        _paramsMap.put(k, list);
                    }
                });
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        }

        return _paramsMap;
    }

    @Override
    public Map<String, List<UploadedFile>> filesMap() throws IOException {
        if (isMultipartFormData()) {
            loadMultipartFormData();

            return _fileMap;
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public NvMap cookieMap() {
        if (_cookieMap == null) {
            _cookieMap = new NvMap();

            String tmp = headerOrDefault(Constants.HEADER_COOKIE, "");
            String[] ss = tmp.split(";");
            for (String s : ss) {
                String[] kv = s.split("=");
                if (kv.length > 1) {
                    _cookieMap.put(kv[0].trim(), kv[1].trim());
                } else {
                    _cookieMap.put(kv[0].trim(), null);
                }
            }
        }

        return _cookieMap;
    }

    private NvMap _cookieMap;

    @Override
    public NvMap headerMap() {
        if (_headerMap == null) {
            resolveHeaders();
        }

        return _headerMap;
    }

    private NvMap _headerMap;

    private void resolveHeaders(){
        _headerMap = new NvMap();
        _headersMap = new IgnoreCaseMap<>();

        Headers headers = _exchange.getRequestHeaders();

        if (headers != null) {
            headers.forEach((k, l) -> {
                if (l.size() > 0) {
                    _headerMap.put(k, l.get(0));
                }
                _headersMap.put(k,l);
            });
        }
    }

    @Override
    public Map<String, List<String>> headersMap() {
        if (_headersMap == null) {
            resolveHeaders();
        }
        return _headersMap;
    }
    private Map<String, List<String>> _headersMap;

    @Override
    public Object response() {
        return _exchange;
    }

    @Override
    protected void contentTypeDoSet(String contentType) {
        if (charset != null) {
            if (contentType.indexOf(";") < 0) {
                headerSet(Constants.HEADER_CONTENT_TYPE, contentType + ";charset=" + charset);
                return;
            }
        }

        headerSet(Constants.HEADER_CONTENT_TYPE, contentType);
    }


    private ByteArrayOutputStream _outputStreamTmp;

    @Override
    public OutputStream outputStream() throws IOException {
        sendHeaders(false);

        if (_allows_write) {
            return _exchange.getResponseBody();
        } else {
            if (_outputStreamTmp == null) {
                _outputStreamTmp = new ByteArrayOutputStream();
            } else {
                _outputStreamTmp.reset();
            }

            return _outputStreamTmp;
        }
    }

    @Override
    public void output(byte[] bytes) {
        try {
            OutputStream out = outputStream();

            if (!_allows_write) {
                return;
            }

            out.write(bytes);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void output(InputStream stream) {
        try {
            OutputStream out = outputStream();

            if (!_allows_write) {
                return;
            }

            IoUtil.transferTo(stream, out);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void headerSet(String key, String val) {
        _exchange.getResponseHeaders().set(key, val);
    }

    @Override
    public void headerAdd(String key, String val) {
        _exchange.getResponseHeaders().add(key, val);
    }


    @Override
    public String headerOfResponse(String name) {
        return _exchange.getResponseHeaders().getFirst(name);
    }

    @Override
    public void cookieSet(String key, String val, String domain, String path, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append("=").append(val).append(";");

        if (Utils.isNotEmpty(path)) {
            sb.append("path=").append(path).append(";");
        }

        if (maxAge >= 0) {
            sb.append("max-age=").append(maxAge).append(";");
        }

        if (Utils.isNotEmpty(domain)) {
            sb.append("domain=").append(domain.toLowerCase()).append(";");
        }

        headerAdd(Constants.HEADER_SET_COOKIE, sb.toString());
    }

    @Override
    public void redirect(String url, int code) {
        url = RedirectUtils.getRedirectPath(url);

        headerSet(Constants.HEADER_LOCATION, url);
        statusDoSet(code);
    }

    @Override
    public int status() {
        return _status;
    }

    private int _status = 200;

    @Override
    protected void statusDoSet(int status) {
        _status = status;
    }

    @Override
    public void flush() throws IOException {
        if (_allows_write) {
            outputStream().flush();
        }
    }

    @Override
    public void close() throws IOException {
        _exchange.close();
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public void asyncStart(long timeout, ContextAsyncListener listener) {
        if (_isAsync == false) {
            _isAsync = true;

            _asyncFuture = new CompletableFuture<>();

            if (listener != null) {
                _asyncListeners.add(listener);
            }

            if (timeout != 0) {
                _asyncTimeout = timeout;
            }
        }
    }


    @Override
    public void asyncComplete() throws IOException {
        if (_isAsync) {
            try {
                innerCommit();
            } finally {
                _asyncFuture.complete(this);
            }
        }
    }

    protected void asyncAwait() throws InterruptedException, ExecutionException, IOException{
        if(_isAsync){
            if (_asyncTimeout > 0) {
                try {
                    _asyncFuture.get(_asyncTimeout, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    for (ContextAsyncListener listener1 : _asyncListeners) {
                        listener1.onTimeout(this);
                    }
                }
            } else {
                _asyncFuture.get();
            }
        }
    }


    @Override
    protected void innerCommit() throws IOException {
        if (getHandled() || status() >= 200) {
            sendHeaders(true);
        } else {
            status(404);
            sendHeaders(true);
        }
    }

    private boolean _allows_write = true;
    private boolean _headers_sent = false;

    private void sendHeaders(boolean isCommit) throws IOException {
        if (!_headers_sent) {
            _headers_sent = true;

            if ("HEAD".equals(method())) {
                _allows_write = false;
            }

            if (sessionState() != null) {
                sessionState().sessionPublish();
            }

            if (isCommit || _allows_write == false) {
                _exchange.sendResponseHeaders(status(), -1L);
            } else {
                List<String> tmp = _exchange.getResponseHeaders().get(Constants.HEADER_CONTENT_LENGTH);

                if (tmp != null && tmp.size() > 0) {
                    _exchange.sendResponseHeaders(status(), Long.parseLong(tmp.get(0)));
                } else {
                    _exchange.sendResponseHeaders(status(), 0L);
                }
            }
        }
    }
}

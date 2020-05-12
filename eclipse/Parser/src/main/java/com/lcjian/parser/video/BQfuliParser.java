package com.lcjian.parser.video;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.Base64;
import com.lcjian.util.Crypto;
import com.lcjian.util.StringUtils;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class BQfuliParser {

    private static final String PC_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Mobile Safari/537.36";

    public String parse(String url) {
        String result = parserMobile(url);
        if (StringUtils.isEmpty(result)) {
            result = parserPC(url);
        }
        if (result == null || (result != null && result.contains("404"))) {
            result = "";
        }
        return result;
    }

    private String parserMobile(String url) {
        String result = null;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", MOBILE_USER_AGENT);

        try {
            String content = getContent(url, headers);
            // 请求网页获取iframe地址
            String iframeUrl = StringUtils.r1(content, "scrolling=\"no\" src=\"(.*?)\"");
            if (StringUtils.isEmpty(iframeUrl)) {
                // 获取video地址
                result = StringUtils.r1(getContent(url, headers), "video src=\"(.*?)\"");
            } else {
                String host = iframeUrl.substring(0, iframeUrl.indexOf("/", 8));
                if (iframeUrl.contains("'+encodeURIComponent(encrypted)+'")) {
                    String h = String.valueOf(System.currentTimeMillis()).substring(0, 8);
                    String rgfgb = "content+" + h + "@" + host;
                    String a = Base64.encode(h.getBytes());
                    String b = Base64.encode(rgfgb.getBytes());
                    String encrypted = Base64.encode((a + b).getBytes());
                    iframeUrl = iframeUrl.replace("'+encodeURIComponent(encrypted)+'", URLEncoder.encode(encrypted, "UTF-8"));
                }

                // 请求网页iframe获取解析地址
                headers.put("Referer", url);
                String iframeContent = getContent(iframeUrl, headers);
                if (iframeContent.contains("$.post")) {
                    String path = StringUtils.r1(iframeContent, "\\$\\.post\\(\"(.*?)\"");
                    String keydata = StringUtils.r1(iframeContent, "\"key\":(.*?),");
                    String map = StringUtils.r1(iframeContent, ", \\{(.*?)\\},");
                    String oKey = StringUtils.r1(keydata, "get\\(\"(.*?)\"\\)");
                    Map<String, String> parametes = new Gson().fromJson(
                            "{" + map.replace(keydata, "\"" + URLEncoder.encode(Crypto.encrypt(oKey), "UTF-8") + "\"") + "}",
                            new TypeToken<Map<String, String>>() {}.getType());
                    headers.put("Referer", iframeUrl);
                    result = StringUtils.r1(getContent(buildUrl(host, path), headers, parametes), "\"url\":\"(.*?)\"");
                } else {
                    result = StringUtils.r1(iframeContent, "video src=\"(.*?)\"");
                    if (StringUtils.isEmpty(result)) {

                        String yunPath = StringUtils.r1(iframeContent, "src=\"(.*?)\"></iframe>");
                        if (!StringUtils.isEmpty(yunPath)) {
                            // 云解析
                            headers.put("Referer", iframeUrl);
                            String yunUrl = buildUrl(host, yunPath);
                            String yunData = getContent(yunUrl, headers);
                            String yunParseParameterUrl = StringUtils.r1(yunData, "url    : '(.*?)',");
                            String yunParseWebPath = StringUtils.r1(yunData, "webpath: '(.*?)',");

                            // 请求云解析
                            headers.put("Referer", yunUrl);
                            String yunPathUrl = host
                                    + (yunParseWebPath.startsWith("/") ? yunParseWebPath : "/" + yunParseWebPath)
                                    + (yunParseWebPath.endsWith("/") ? "" : "/") + "api.php";
                            Map<String, String> parameter = new HashMap<String, String>();
                            parameter.put("url", yunParseParameterUrl);
                            parameter.put("up", "0");
                            result = StringUtils.r1(getContent(yunPathUrl, headers, parameter), "\"url\":\"(.*?)\"");

                            // 请求yunParseDataUrl
                            String parseData = StringUtils.r1(getContent(result.replace("https", "http"), headers),
                                    "\"data\":\"(.*?)\"");
                            if (!StringUtils.isEmpty(parseData)) {
                                // 获取最后的数据
                                parameter.put("data", parseData);
                                result = StringUtils.r1(getContent(yunPathUrl, headers, parameter), "\"url\":\"(.*?)\"");
                            }
                        } else {
                            result = null;
                        }
                    } else {
                        if (!result.contains("http") && !result.contains("https")) {
                            result = buildUrl(host, result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private String parserPC(String url) {
        url = url.replace("www.11wa.com", "www.zxfuli.com");
        String result = null;

        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", PC_USER_AGENT);

            String content = getContent(url, headers);
            // 请求网页获取iframe地址
            String iframeUrl = StringUtils.r1(content, "scrolling=\"no\" src=\"(.*?)\"");
            String host = iframeUrl.substring(0, iframeUrl.indexOf("/", 8));
            // 请求网页iframe获取video地址
            headers.put("Referer", url);
            String iframeContent = getContent(iframeUrl, headers);
            String flashvarsA = StringUtils.r1(iframeContent, "a:'(.*?)',");
            String flashvarsF = StringUtils.r1(iframeContent, "f:'(.*?)',");
            if (StringUtils.isEmpty(flashvarsA)) {
                if (StringUtils.isEmpty(flashvarsF)) {
                    String yunPath = StringUtils.r1(iframeContent, "src=\"(.*?)\"></iframe>");
                    // 云解析
                    headers.put("Referer", iframeUrl);
                    String yunUrl = buildUrl(host, yunPath);
                    String yunData = getContent(yunUrl, headers);
                    String yunParseParameterUrl = StringUtils.r1(yunData, "url    : '(.*?)',");
                    String yunParseWebPath = StringUtils.r1(yunData, "webpath: '(.*?)',");
                    String yunPathUrl = host
                            + (yunParseWebPath.startsWith("/") ? yunParseWebPath : "/" + yunParseWebPath)
                            + (yunParseWebPath.endsWith("/") ? "" : "/") + "api.php?url=" + yunParseParameterUrl;

                    headers.put("Referer", yunUrl);
                    result = StringUtils.r1(getContent(yunPathUrl, headers), "\\[CDATA\\[(.*?)\\]\\]");
                } else {
                    // 请求video获取解析地址
                    headers.put("Referer", iframeUrl);
                    result = StringUtils.r1(getContent(buildUrl(host, flashvarsF), headers),
                            "\\[CDATA\\[(.*?)\\]\\]");
                }
            } else {
                result = URLDecoder.decode(StringUtils.r1(iframeContent, "video=\\['(.*?)'\\];"), "utf-8");
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private String buildUrl(String host, String path) {
        return (host.endsWith("/") ? host.substring(0, host.length() - 1) : host)
                + (path.startsWith("/") ? path : "/" + path);
    }

    private String getContent(String url, Map<String, String> headers) {
        return getContent(url, headers, null);
    }

    private String getContent(String url, Map<String, String> headers, Map<String, String> form) {
        Request.Builder requsetBuilder = new Request.Builder().url(url).headers(Headers.of(headers));
        if (form != null) {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Entry<String, String> entry : form.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
            requsetBuilder.post(formBodyBuilder.build());
        }
        Request request = requsetBuilder.build();
        Response response;
        try {
            response = OkHttpClientSingleton.getSingleton().newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}

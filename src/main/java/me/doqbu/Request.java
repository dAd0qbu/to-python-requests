package me.doqbu;

import burp.api.montoya.http.message.ContentType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
    public String method;
    public String url;
    public String route;
    public String safeRoute;
    public Map<String, Object> headers;
    public String rawBody;
    public String dataParameterName;
    public String params;
    public String data;
    public Map<String, Object> cookies;
    public Map<String, Object> files;

    public Request(HttpRequest request) {
        this.method = request.method();
        this.url = request.url();
        this.route = request.pathWithoutQuery();
        this.safeRoute = this.route.replaceAll("[^a-zA-Z0-9]", "_");
        this.headers = getHeaders(request.headers());
        this.rawBody = request.bodyToString();
        this.dataParameterName = request.contentType() == ContentType.JSON ? "json" : "data";
        this.params = request.query();
        this.cookies = getCookies(request.header("Cookie"));
        this.data = formatData(request);
    }

    private String formatData(HttpRequest request) {
        if (request.body().length() < 1) return null;

        ContentType contentType = request.contentType();
        if (contentType != ContentType.URL_ENCODED && contentType != ContentType.JSON)
            return "\"" + CodeGenerator.escapeString(rawBody) + "\"";


        List<ParsedHttpParameter> parameters;
        if (contentType == ContentType.URL_ENCODED) {
            parameters = request.parameters(HttpParameterType.URL);
        } else {
            parameters = request.parameters(HttpParameterType.JSON);
        }
        Map<String, Object> data = new HashMap<String, Object>() {
            @Override
            public String toString() {
                Json json = Json.make(this);
                return json.toString();
            }
        };
        for (ParsedHttpParameter parameter : parameters) {
            data.put(parameter.name(), parameter.value());
        }

        return data.isEmpty() ? null : data.toString();
    }

    private Map<String, Object> getHeaders(List<HttpHeader> headersList) {
        Map<String, Object> headers = new HashMap<String, Object>();
        for (HttpHeader header : headersList) {
            headers.put(header.name(), header.value());
        }
        return headers.isEmpty() ? null : headers;
    }

    private Map<String, Object> getCookies(HttpHeader cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\s*([^=;]+)\\s*=\\s*\\\"?([^\\\";]*)\\\"?\\s*;?");
        Matcher matcher = pattern.matcher(cookieHeader.value());

        Map<String, Object> cookies = new HashMap<String, Object>();
        while (matcher.find()) {
            cookies.put(matcher.group(1), matcher.group(2));
        }
        return cookies;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            try {
                map.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        return map;
    }
}
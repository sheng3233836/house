package com.whitley.house;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;

/**
 * @author yuanxin
 * @date 2022/8/24
 */
public abstract class HttpClient {

    public String get(String url, Map<String, Object> paramMap) throws Exception {
        return get(getH5BaseUrl(url, paramMap));
    }

    public String get(String url) throws Exception {
        randomSleep();
        HttpGet httpPost = new HttpGet(url);
        setHeader(httpPost);
        httpPost.setConfig(RequestConfig.custom().build());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != 200) {
            System.out.println(JSON.toJSONString(response));
        }
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }

    public String post(String url, String json) throws Exception {
        randomSleep();
        System.out.println(json);
        HttpPost httpPost = new HttpPost(url);
        StringEntity postingString = new StringEntity(json, StandardCharsets.UTF_8);
        httpPost.setEntity(postingString);
        setHeader(httpPost);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setConfig(RequestConfig.custom().build());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != 200) {
            System.out.println(JSON.toJSONString(response));
        }
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }


    private String getH5BaseUrl(String url, Map<String, Object> paramMap) {
        return url + "?" + map2String(paramMap);
    }

    private String map2String(Map<String, Object> map) {
        if (null == map || map.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }

        return sb.toString();
    }

    public abstract void setHeader(HttpRequestBase requestBase);

    private void randomSleep() throws InterruptedException {
        Thread.sleep(2000 + new Random().nextInt(2000));
    }
}

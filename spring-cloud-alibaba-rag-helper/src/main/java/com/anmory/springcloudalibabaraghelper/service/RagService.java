package com.anmory.springcloudalibabaraghelper.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-29 下午10:02
 */

@Service
public class RagService {
    private final String baseUrl = "http://localhost:8003";
    public String query(String question) throws IOException {
        // 把问题编码
        String finalQuestion = URLEncoder.encode(question, StandardCharsets.UTF_8);
        // 构造url
        String url = baseUrl + "/query?question=" + finalQuestion;

        // 创建http客户端
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 构造响应体
        Response response = client.newCall(request).execute();

        if(!response.isSuccessful()) {
            throw new IOException("请求失败，状态码: " + response);
        }

        // 获取响应体
        String body = response.body().string();
        System.out.println("响应体: " + body);

        // 使用gson解析json
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(body, JsonObject.class);
        JsonArray relevant = jsonResponse.getAsJsonArray("results");
        System.out.println("匹配结果"+relevant);
        String text = "";
        for(int i = 0; i < relevant.size(); i++) {
            text = relevant.get(i).getAsJsonObject().get("text").getAsString();
        }
        return text;
    }
}

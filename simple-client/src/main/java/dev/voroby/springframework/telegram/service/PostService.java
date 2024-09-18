package dev.voroby.springframework.telegram.service;

import dev.voroby.springframework.telegram.entity.ProxyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class PostService {

    private final WebClient webClient;

    @Autowired
    public PostService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:8984").build(); // 设置基础 URL
    }

    /**
     * 发送 POST 请求
     *
     * @param endpoint API 端点
     * @param requestBody 请求体
     * @return 响应内容
     */
    public Mono<String> sendPostRequest(String endpoint, Map<String, Object> requestBody) {
        return webClient.post()
                .uri(endpoint)  // 指定请求的 URI
                .contentType(MediaType.APPLICATION_JSON)  // 设置请求头
                .bodyValue(requestBody)  // 设置请求体
                .retrieve()  // 发起请求
                .bodyToMono(String.class);  // 将响应体转换为 String
    }
}

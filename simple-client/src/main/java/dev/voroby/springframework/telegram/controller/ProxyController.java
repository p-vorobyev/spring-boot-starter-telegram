package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.entity.ProxyVO;
import dev.voroby.springframework.telegram.exception.TelegramClientTdApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static dev.voroby.springframework.telegram.service.TelegramClientService.PROXY_COUNT;

@RestController
@Slf4j
@RequestMapping(value = "/api/proxy")
@CrossOrigin
public class ProxyController {
    private final TelegramClient telegramClient;


    public ProxyController(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    record Credential(@NotBlank String value){}

    record _ProxyType(
        String username,
        String password,
        boolean httpOnly,
        String secret
    ){}

    record _Proxy(Integer id,@NotBlank String server, @NotNull int port, @NotNull boolean enable, _ProxyType _proxyType){}

    public TdApi.ProxyType getType(_ProxyType _proxyType){
        TdApi.ProxyType type;
        if (_proxyType.secret != null){
            type = new TdApi.ProxyTypeMtproto(_proxyType.secret);
        }else if (_proxyType.httpOnly){
            type = new TdApi.ProxyTypeHttp(_proxyType.username, _proxyType.password, true);
        }else {
            type = new TdApi.ProxyTypeSocks5(_proxyType.username, _proxyType.password);
        }
        return type;
    }

    @PostMapping(value = "/ping", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ProxyVO>  ping(@RequestBody @Valid List<Integer> credentials) throws ExecutionException, InterruptedException {
//        log.info("credentials: \t {}", credentials);
        List<ProxyVO> proxyVOS = new ArrayList<>();
        for (Integer credential : credentials){
            try {
                var future = telegramClient.sendAsync(new TdApi.PingProxy(credential));
                ProxyVO proxyVO = new ProxyVO();
                proxyVO.setSeconds(future.get().object());
                proxyVO.id = credential;
                proxyVOS.add(proxyVO);
            }catch (Exception e){
                log.error("ping", e);
            }
        }

        return proxyVOS;
    }

    @PostMapping(value = "/proxyCount", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Integer setProxyCount(@RequestBody @Valid Credential credential) throws ExecutionException, InterruptedException {
        PROXY_COUNT = Integer.parseInt(credential.value);
        return PROXY_COUNT;
    }

    @PostMapping(value = "/GetProxyLink", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, TdApi.HttpUrl> GetProxyLink(@RequestBody @Valid List<Integer> credentials) throws ExecutionException, InterruptedException {
        Map<Integer, TdApi.HttpUrl> linkMap = new  Hashtable <>();
//        log.info("credentials: \t {}", credentials);
        for (Integer credential : credentials){
            try {
                var future = telegramClient.sendAsync(new TdApi.GetProxyLink(credential));
                linkMap.put(credential, future.get().object());
            }catch (Exception e){
                log.error("GetProxyLink", e);
            }
        }
        return linkMap;
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, TdApi.Ok> deleteProxy(@RequestBody @Valid List<Integer> credentials) throws ExecutionException, InterruptedException {
        Map<Integer, TdApi.Ok> PingMap = new  Hashtable <>();
//        log.info("credentials: \t {}", credentials);
        for (Integer credential : credentials){
            var future = telegramClient.sendAsync(new TdApi.RemoveProxy(credential));
            PingMap.put(credential, future.get().object());
        }
        return PingMap;
    }

    @GetMapping()
    public List<ProxyVO> getProxies() throws ExecutionException, InterruptedException {
        TdApi.Proxies proxies = telegramClient.sendAsync(new TdApi.GetProxies()).get().object();
        List<ProxyVO> proxyVOS = new ArrayList<>();
        for (TdApi.Proxy proxy : proxies.proxies){
            TdApi.HttpUrl httpUrl = null;
            try {
                httpUrl = telegramClient.sendAsync(new TdApi.GetProxyLink(proxy.id)).get().object();
            }catch (Exception e){
                log.error("getProxies", e);
            }
            proxyVOS.add(new ProxyVO(proxy, httpUrl));
        }
        return proxyVOS;
    }

    @PutMapping()
    public List<TdApi.Proxy> addProxies(@RequestBody @Valid List<_Proxy> addProxies) throws ExecutionException, InterruptedException {
//        log.info("addProxy message:\n{}", addProxies);
        List<TdApi.Proxy> proxies = new ArrayList<>();
        for (_Proxy addProxy : addProxies){
            proxies.add(addProxy(new TdApi.AddProxy(addProxy.server, addProxy.port, addProxy.enable, getType(addProxy._proxyType))));
        }
        return proxies;
    }

    @PostMapping(value = "/edit")
    public TdApi.Proxy editProxy(@RequestBody @Valid _Proxy _proxy) throws ExecutionException, InterruptedException {
//        log.info("proxyVO:\n{}", _proxy );
        return telegramClient.sendAsync(new TdApi.EditProxy(_proxy.id, _proxy.server, _proxy.port, _proxy.enable, getType(_proxy._proxyType))).get().object();
    }

    private TdApi.Proxy addProxy(TdApi.AddProxy addProxy) throws ExecutionException, InterruptedException {
        TdApi.Proxies proxies = telegramClient.sendAsync(new TdApi.GetProxies()).get().object();
        for (TdApi.Proxy proxy : proxies.proxies){
            if (proxy.server.equals(addProxy.server) && proxy.port == addProxy.port){
                return telegramClient.sendAsync(new TdApi.EditProxy(proxy.id, addProxy.server, addProxy.port, addProxy.enable, addProxy.type)).get().object();
            }
        }
        return telegramClient.sendAsync(new TdApi.AddProxy(addProxy.server, addProxy.port, addProxy.enable, addProxy.type)).get().object();
    }

    @PostMapping(value = "/enable")
    public TdApi.Ok enableProxies(@RequestBody @Valid Credential credential) throws ExecutionException, InterruptedException {
        log.info("Credential:\n{}", credential);
        return telegramClient.sendAsync(new TdApi.EnableProxy(Integer.parseInt(credential.value))).get().object();
    }

}

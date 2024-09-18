package dev.voroby.springframework.telegram.entity;

import dev.voroby.springframework.telegram.client.TdApi;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ProxyVO extends TdApi.Proxy {
    public TdApi.Seconds seconds;

    public TdApi.HttpUrl httpUrl;

    public ProxyVO() {

    }
    public ProxyVO(TdApi.Proxy proxy, TdApi.HttpUrl httpUrl) {
        super(proxy.id, proxy.server, proxy.port, proxy.lastUsedDate, proxy.isEnabled, proxy.type);
        this.httpUrl = httpUrl;
    }
    public ProxyVO(TdApi.Proxy proxy, TdApi.Seconds seconds, TdApi.HttpUrl httpUrl) {
        super(proxy.id, proxy.server, proxy.port, proxy.lastUsedDate, proxy.isEnabled, proxy.type);
        this.seconds = seconds;
        this.httpUrl = httpUrl;
    }

    @Override
    public String toString() {
        return "ProxyVO{" +
                "seconds=" + seconds +
                ", httpUrl=" + httpUrl +
                ", id=" + id +
                ", server='" + server + '\'' +
                ", port=" + port +
                ", lastUsedDate=" + lastUsedDate +
                ", isEnabled=" + isEnabled +
                ", type=" + type +
                '}';
    }
}

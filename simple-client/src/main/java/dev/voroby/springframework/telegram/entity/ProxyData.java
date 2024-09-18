package dev.voroby.springframework.telegram.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * (ProxyData)实体类
 *
 * @author makejava
 * @since 2023-08-27 13:09:56
 */
public class ProxyData implements Serializable {
    private static final long serialVersionUID = 445958220676035064L;

    private Integer poxyid;

    private String poxyaddress;

    private Integer poxyport;

    private String poxysecret;

    private String poxytype;

    private String proxyname;

    private String username;

    private String password;

    private Integer proxyimg;

    private Date createdAt;

    private Date updatedAt;


    public Integer getPoxyid() {
        return poxyid;
    }

    public void setPoxyid(Integer poxyid) {
        this.poxyid = poxyid;
    }

    public String getPoxyaddress() {
        return poxyaddress;
    }

    public void setPoxyaddress(String poxyaddress) {
        this.poxyaddress = poxyaddress;
    }

    public Integer getPoxyport() {
        return poxyport;
    }

    public void setPoxyport(Integer poxyport) {
        this.poxyport = poxyport;
    }

    public String getPoxysecret() {
        return poxysecret;
    }

    public void setPoxysecret(String poxysecret) {
        this.poxysecret = poxysecret;
    }

    public String getPoxytype() {
        return poxytype;
    }

    public void setPoxytype(String poxytype) {
        this.poxytype = poxytype;
    }

    public String getProxyname() {
        return proxyname;
    }

    public void setProxyname(String proxyname) {
        this.proxyname = proxyname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getProxyimg() {
        return proxyimg;
    }

    public void setProxyimg(Integer proxyimg) {
        this.proxyimg = proxyimg;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}


package com.raiser.rpc.protocol;

import com.raiser.rpc.util.JsonUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 2:54 PM
 */
public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = -1102180003395190700L;

    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfoList;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }

    public static RpcProtocol fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcProtocol.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcProtocol that = (RpcProtocol) o;
        return port == that.port && Objects.equals(host, that.host) && Objects.equals(serviceInfoList, that.serviceInfoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList);
    }
}

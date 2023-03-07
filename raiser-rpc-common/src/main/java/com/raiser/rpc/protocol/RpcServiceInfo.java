package com.raiser.rpc.protocol;

import com.raiser.rpc.util.JsonUtil;

import java.util.Objects;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 11:41 AM
 */
public class RpcServiceInfo {
    private String interfaceName;
    private String version;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(interfaceName, that.interfaceName) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, version);
    }

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}

package com.raiser.rpc.util;

import io.netty.util.internal.StringUtil;

/**
 * @author: zhengyangxin
 * @date: 8/30/2022 4:49 PM
 */
public class ServiceUtil {
    // 不知道为何为#
    public static final String makeServiceKey = "#";

    public static String makeServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if (!StringUtil.isNullOrEmpty(version)) {
            serviceKey += makeServiceKey.concat(version);
        }
        return serviceKey;
    }

}

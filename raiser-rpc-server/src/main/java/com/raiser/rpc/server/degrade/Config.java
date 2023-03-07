package com.raiser.rpc.server.degrade;

import lombok.Data;

/**
 * @author charles
 * @date 2022/10/31 17:45
 */
@Data
public class Config {
    /**
     * Closed 状态进入 Open 状态的错误个数阀值
     */
    private int failureCount = 5;

    /**
     * failureCount 统计时间窗口（超过该时间重置失败次数）
     */
    private long failureTimeInterval = 2 * 1000;

    /**
     * Open 状态进入 Half-Open 状态的超时时间
     */
    private int halfOpenTimeout = 5 * 1000;

    /**
     * Half-Open 状态进入 Open 状态的成功个数阀值
     */
    private int halfOpenSuccessCount = 2;

}


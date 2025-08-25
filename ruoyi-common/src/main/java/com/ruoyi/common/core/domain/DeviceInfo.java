package com.ruoyi.common.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备信息
 *
 * @author YY
 */
@Data
public class DeviceInfo implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 登录IP地址
     */
    private String ipAddr;

    /**
     * 登录地点
     */
    private String ipAddress;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录平台
     */
    private String platform;
}

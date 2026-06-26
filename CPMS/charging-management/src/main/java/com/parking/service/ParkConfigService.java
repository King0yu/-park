package com.parking.service;

import com.parking.entity.ParkConfig;

/**
 * 系统配置服务接口
 *
 * 定义系统配置相关的业务方法
 */
public interface ParkConfigService {

    /**
     * 获取系统配置
     *
     * @return 系统配置实体
     */
    ParkConfig getConfig();

    /**
     * 更新系统配置
     *
     * @param parkConfig 系统配置实体
     * @return 是否更新成功
     */
    boolean updateConfig(ParkConfig parkConfig);
}

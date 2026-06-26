package com.parking.service.impl;

import com.parking.entity.ParkConfig;
import com.parking.mapper.ParkConfigMapper;
import com.parking.service.ParkConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * 系统配置服务实现类
 *
 * 实现系统配置相关的业务逻辑
 * 服务启动时若表为空，会自动初始化一条默认数据
 */
@Service
public class ParkConfigServiceImpl implements ParkConfigService {

    @Autowired
    private ParkConfigMapper parkConfigMapper;

    /**
     * 默认系统配置
     */
    private static final String DEFAULT_SYSTEM_NAME = "小区停车场管理系统";
    private static final String DEFAULT_COPYRIGHT = "© 2024 小区停车场管理系统 版权所有";

    /**
     * 服务启动时初始化默认系统配置
     * 若 park_config 表为空，则插入一条默认数据
     */
    @PostConstruct
    public void initDefaultConfig() {
        if (parkConfigMapper.selectCount(null) == 0) {
            ParkConfig config = new ParkConfig();
            config.setSystemName(DEFAULT_SYSTEM_NAME);
            config.setCopyright(DEFAULT_COPYRIGHT);
            Date now = new Date();
            config.setCreateTime(now);
            config.setUpdateTime(now);
            parkConfigMapper.insert(config);
        }
    }

    @Override
    public ParkConfig getConfig() {
        return parkConfigMapper.selectOne(null);
    }

    @Override
    public boolean updateConfig(ParkConfig parkConfig) {
        ParkConfig existingConfig = getConfig();
        if (existingConfig == null) {
            return false;
        }

        if (StringUtils.hasText(parkConfig.getSystemName())) {
            existingConfig.setSystemName(parkConfig.getSystemName().trim());
        }
        if (StringUtils.hasText(parkConfig.getCopyright())) {
            existingConfig.setCopyright(parkConfig.getCopyright().trim());
        }
        existingConfig.setUpdateTime(new Date());

        return parkConfigMapper.updateById(existingConfig) > 0;
    }
}

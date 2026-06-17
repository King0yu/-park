package com.parking.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * 
 * 主要配置：
 * 1. 分页插件（PaginationInnerInterceptor）
 *    - 支持 MySQL 数据库分页
 *    - 支持请求参数中指定分页参数
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis Plus 插件
     * 
     * @return MybatisPlusInterceptor 插件集合
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型为 MySQL
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 设置分页溢出后是否处理
        paginationInnerInterceptor.setOverflow(true);
        
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        return interceptor;
    }
}
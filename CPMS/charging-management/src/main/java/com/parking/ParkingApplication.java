package com.parking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小区停车场管理系统 - SpringBoot启动类
 *
 * 功能说明：
 * 1. @SpringBootApplication 标注这是SpringBoot应用的主启动类
 *    - @Configuration：配置类
 *    - @EnableAutoConfiguration：启用自动配置
 *    - @ComponentScan：组件扫描（扫描本包及子包的组件）
 *
 * 2. @MapperScan 配置MyBatis Mapper接口扫描路径
 *    - 所有继承BaseMapper的接口会自动注册为MyBatis的Mapper
 *
 * 3. SpringApplication.run() 启动SpringBoot应用
 */
@SpringBootApplication
@MapperScan("com.parking.mapper")  // 扫描Mapper接口所在的包
public class ParkingApplication {

    public static void main(String[] args) {
        // 启动SpringBoot应用
        SpringApplication.run(ParkingApplication.class, args);

        // 启动成功后打印提示信息
        System.out.println("========================================");
        System.out.println("小区停车场管理系统启动成功！");
        System.out.println("访问地址：http://localhost:8080/api");
        System.out.println("========================================");
    }
}

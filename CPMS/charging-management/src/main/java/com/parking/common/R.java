package com.parking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一API响应结果封装类 - 小区停车场管理系统
 *
 * 功能说明：
 * 统一前后端数据交互格式，所有接口返回此格式的数据
 *
 * 使用示例：
 * {
 *   "code": 200,           // 状态码
 *   "msg": "操作成功",      // 提示信息
 *   "data": {...}          // 返回数据（可选）
 * }
 *
 * @param <T> 泛型：返回数据的类型
 */
@Data
@NoArgsConstructor          // 无参构造函数
@AllArgsConstructor         // 全参构造函数
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200: 成功
     * 400: 请求参数错误
     * 401: 未登录或登录过期
     * 403: 无权限访问
     * 404: 资源不存在
     * 500: 服务器内部错误
     */
    private int code;

    /**
     * 响应消息
     * 用于前端展示给用户的提示信息
     */
    private String msg;

    /**
     * 响应数据
     * 泛型T可以是任意类型：对象、列表、Map等
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @return 成功的响应结果
     */
    public static R<?> success() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 返回的数据
     * @param <T>  数据类型
     * @return 成功的响应结果
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带自定义消息）
     *
     * @param msg  自定义提示信息
     * @param data 返回的数据
     * @param <T>  数据类型
     * @return 成功的响应结果
     */
    public static <T> R<T> success(String msg, T data) {
        return new R<>(200, msg, data);
    }

    /**
     * 失败响应（无数据）
     *
     * @param msg 错误提示信息
     * @return 失败的响应结果
     */
    public static R<?> error(String msg) {
        return new R<>(500, msg, null);
    }

    /**
     * 失败响应（带状态码）
     *
     * @param code 状态码
     * @param msg  错误提示信息
     * @return 失败的响应结果
     */
    public static R<?> error(int code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 判断响应是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
}

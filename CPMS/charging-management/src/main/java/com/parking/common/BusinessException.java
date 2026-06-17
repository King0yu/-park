package com.parking.common;

import lombok.Getter;

/**
 * 业务异常类
 *
 * 功能说明：
 * 用于主动抛出的业务逻辑错误，比系统异常更明确
 *
 * 使用场景：
 * 1. 用户未登录
 * 2. 权限不足
 * 3. 数据不存在
 * 4. 业务规则校验失败
 *
 * 使用示例：
 * if (user == null) {
 *     throw new BusinessException("用户不存在");
 * }
 *
 * // 或者指定错误码
 * throw new BusinessException(401, "登录已过期，请重新登录");
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * 与R类的code对应
     * - 400: 请求参数错误
     * - 401: 未登录或登录过期
     * - 403: 无权限访问
     * - 404: 资源不存在
     * - 500: 服务器内部错误
     */
    private final int code;

    /**
     * 构造函数（只传入错误信息）
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;  // 默认错误码为500
    }

    /**
     * 构造函数（传入错误码和错误信息）
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 快速创建：未登录异常
     *
     * @return 未登录异常
     */
    public static BusinessException unauthorized() {
        return new BusinessException(401, "未登录或登录已过期");
    }

    /**
     * 快速创建：无权限异常
     *
     * @return 无权限异常
     */
    public static BusinessException forbidden() {
        return new BusinessException(403, "无权限访问");
    }

    /**
     * 快速创建：资源不存在异常
     *
     * @return 资源不存在异常
     */
    public static BusinessException notFound() {
        return new BusinessException(404, "资源不存在");
    }
}

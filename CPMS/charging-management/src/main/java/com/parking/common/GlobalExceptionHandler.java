package com.parking.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 小区停车场管理系统
 *
 * 功能说明：
 * 统一处理项目中所有未捕获的异常，将异常信息封装为统一的JSON格式返回给前端
 *
 * 处理范围：
 * 1. 业务异常（BusinessException）
 * 2. 参数校验异常（ValidationException）
 * 3. 绑定异常（BindException）
 * 4. 空指针异常（NullPointerException）
 * 5. 其他未处理异常
 *
 * 返回格式：
 * {
 *   "code": 500,
 *   "msg": "错误提示信息",
 *   "data": null
 * }
 */
@RestControllerAdvice  // @RestControllerAdvice = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 用于主动抛出的业务逻辑错误
     *
     * @param e       业务异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)  // 返回200状态码，但code为业务错误码
    public R<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 打印错误日志（包含请求路径）
        System.err.println("业务异常[" + request.getRequestURI() + "]: " + e.getMessage());

        // 返回业务错误响应
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid校验失败）
     * 当Controller方法参数使用@Valid或@Validated注解校验失败时触发
     *
     * @param e       参数校验异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        // 获取第一个校验失败的错误信息
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");

        // 打印错误日志
        System.err.println("小区停车场管理系统-参数校验异常[" + request.getRequestURI() + "]: " + errorMessage);

        // 返回参数错误响应
        return R.error(400, errorMessage);
    }

    /**
     * 处理表单绑定异常
     * 当请求参数绑定到方法参数失败时触发
     *
     * @param e       绑定异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleBindException(BindException e, HttpServletRequest request) {
        // 获取第一个绑定失败的错误信息
        String errorMessage = e.getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数绑定失败");

        // 打印错误日志
        System.err.println("小区停车场管理系统-参数绑定异常[" + request.getRequestURI() + "]: " + errorMessage);

        // 返回参数错误响应
        return R.error(400, errorMessage);
    }

    /**
     * 处理约束违规异常
     * 当使用@Validated注解校验方法参数或路径变量时失败
     *
     * @param e       约束违规异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        // 获取第一个约束违规的错误信息
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("约束校验失败");

        // 打印错误日志
        System.err.println("小区停车场管理系统-约束校验异常[" + request.getRequestURI() + "]: " + errorMessage);

        // 返回参数错误响应
        return R.error(400, errorMessage);
    }

    /**
     * 处理空指针异常
     * 防止代码中的空指针导致服务崩溃
     *
     * @param e       空指针异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        // 打印完整异常堆栈（方便开发调试）
        e.printStackTrace();
        System.err.println("小区停车场管理系统-空指针异常[" + request.getRequestURI() + "]: " + e.getMessage());

        // 返回服务器错误响应（不暴露具体异常信息给前端）
        return R.error(500, "服务器内部错误，请稍后重试");
    }

    /**
     * 处理其他未处理的异常
     * 作为最后的异常处理防线
     *
     * @param e       未知异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e, HttpServletRequest request) {
        // 打印完整异常堆栈（方便开发调试）
        e.printStackTrace();
        System.err.println("小区停车场管理系统-系统异常[" + request.getRequestURI() + "]: " + e.getMessage());

        // 返回服务器错误响应（不暴露具体异常信息给前端）
        return R.error(500, "系统繁忙，请稍后重试");
    }
}

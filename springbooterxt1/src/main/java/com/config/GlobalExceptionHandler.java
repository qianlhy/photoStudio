package com.config;

import com.entity.EIException;
import com.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局统一异常处理：业务异常返回友好提示，系统异常记录完整堆栈并对外脱敏。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：返回自定义状态码与提示 */
    @ExceptionHandler(EIException.class)
    public R handleBusiness(EIException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {}", request.getRequestURI(), e.getMsg());
        return R.error(e.getCode(), e.getMsg());
    }

    /** 参数缺失/校验失败：返回 400 与提示 */
    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            IllegalArgumentException.class})
    public R handleBadRequest(Exception e, HttpServletRequest request) {
        log.warn("参数异常 [{}] {}", request.getRequestURI(), e.getMessage());
        return R.error(400, "请求参数有误");
    }

    /** 兜底：记录完整堆栈，对外只返回通用提示，避免泄露内部细节 */
    @ExceptionHandler(Exception.class)
    public R handleAll(Exception e, HttpServletRequest request) {
        log.error("系统异常 [" + request.getRequestURI() + "]", e);
        return R.error(500, "系统繁忙，请稍后重试");
    }
}

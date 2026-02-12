package com.adminplus.common.pojo;

import com.adminplus.utils.XssUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * XSS 请求包装器
 * 对请求参数、请求头进行 XSS 过滤，防止跨站脚本攻击
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 构造函数
     *
     * @param request 原始 HTTP 请求
     */
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取参数值，并进行 XSS 过滤
     *
     * @param name 参数名
     * @return 过滤后的参数值
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return XssUtils.sanitize(value);
    }

    /**
     * 获取参数值数组，并对每个值进行 XSS 过滤
     *
     * @param name 参数名
     * @return 过滤后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = XssUtils.sanitize(values[i]);
        }
        return sanitizedValues;
    }

    /**
     * 获取请求头，并进行 XSS 过滤
     *
     * @param name 请求头名称
     * @return 过滤后的请求头值
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return XssUtils.sanitize(value);
    }
}
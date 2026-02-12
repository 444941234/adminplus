package com.adminplus.filter;

import com.adminplus.common.pojo.XssRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * XSS 过滤器
 * 对所有请求进行 XSS 过滤，防止跨站脚本攻击
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 对 GET 请求和 POST 请求都进行 XSS 过滤
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        XssRequestWrapper wrappedRequest = new XssRequestWrapper(httpRequest);

        chain.doFilter(wrappedRequest, response);
    }
}
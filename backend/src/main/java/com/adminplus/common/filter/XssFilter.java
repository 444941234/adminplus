package com.adminplus.common.filter;

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

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 跳过 multipart 请求（文件上传），避免破坏文件内容
        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {
            chain.doFilter(request, response);
            return;
        }

        // 对普通请求进行 XSS 过滤
        XssRequestWrapper wrappedRequest = new XssRequestWrapper(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }
}
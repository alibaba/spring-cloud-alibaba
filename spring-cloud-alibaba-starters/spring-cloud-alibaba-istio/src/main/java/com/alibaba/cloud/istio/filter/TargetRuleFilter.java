package com.alibaba.cloud.istio.filter;

import com.alibaba.cloud.istio.rules.auth.TargetRule;
import com.alibaba.cloud.istio.rules.manager.TargetRuleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class TargetRuleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String host = servletRequest.getRemoteHost();
        int port = servletRequest.getLocalPort();
        String method = ((HttpServletRequest) servletRequest).getMethod();
        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        if (TargetRuleManager.isValid(host, port, method, path)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}

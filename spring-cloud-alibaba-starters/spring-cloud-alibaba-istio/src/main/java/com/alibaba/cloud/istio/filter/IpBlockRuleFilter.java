package com.alibaba.cloud.istio.filter;

import com.alibaba.cloud.istio.rules.auth.IpBlockRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * IP黑白名单
 */
@Component
public class IpBlockRuleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }
}

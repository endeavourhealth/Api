package org.endeavourhealth.coreui.framework;

import org.endeavourhealth.common.config.ConfigManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        httpResponse.addHeader("Access-Control-Allow-Origin", getAllowedOrigins());
        httpResponse.addHeader("Access-Control-Allow-Methods", getAllowedMethods());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private String getAllowedOrigins() {
        return ConfigManager.getConfiguration("allowed-origins");
    }

    private String getAllowedMethods() {
        return ConfigManager.getConfiguration("allowed-methods");
    }
}

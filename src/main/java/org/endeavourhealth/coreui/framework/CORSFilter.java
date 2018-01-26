package org.endeavourhealth.coreui.framework;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CORSFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(CORSFilter.class);
    private static Map<String, String> _CORSConfig = null;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        for(Map.Entry<String, String> header: getCorsConfig().entrySet())
            httpResponse.addHeader(header.getKey(), header.getValue());

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private Map<String, String> getCorsConfig() {
        if (_CORSConfig == null) {
            String corsJson = ConfigManager.getConfiguration("cors");
            if (corsJson != null)
                _CORSConfig = parseCorsConfigJson(corsJson);
            else
                return new HashMap<>();
        }
        return _CORSConfig;
    }

    private HashMap<String, String> parseCorsConfigJson(String corsJson) {
        HashMap<String, String> CORSConfig = new HashMap<>();
        try {
            JsonNode cors = ObjectMapperPool.getInstance().readTree(corsJson);
            Iterator<Map.Entry<String,JsonNode>> corsFields = cors.fields();
            while(corsFields.hasNext()) {
                Map.Entry<String, JsonNode> field = corsFields.next();
                CORSConfig.put(field.getKey(), field.getValue().asText());
            }
        } catch (IOException e) {
            LOG.error("Error getting cors configuration!", e);
            return CORSConfig;
        }
        return CORSConfig;
    }
}

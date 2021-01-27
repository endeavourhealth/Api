package org.endeavourhealth.coreui.framework;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpStatus;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.usermanager.caching.UserCache;
import org.endeavourhealth.coreui.framework.models.KeycloakConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

public class ExternalAPIAccessFilter implements Filter {
    private static final Logger logger = Logger.getLogger(ExternalAPIAccessFilter.class.getName());
    private static String appName = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        appName = filterConfig.getInitParameter("app_name");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        logger.info("Get Patient Record request received ");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String reg_url = httpServletRequest.getRequestURL().toString();
        // no token validation  for healthCheck request
        if (reg_url.contains("healthCheck")) {
            chain.doFilter(request, response);
        } else {

            String headerAuthToken = ((HttpServletRequest) request).getHeader("Authorization");
            String userID = "";

            // validate the incoming authorization token by calling dev keycloak and produce the principal user identifier associated with the token
            Client client = ClientBuilder.newClient();

            KeycloakConfig kc = getConfig();

            String url = kc.getAuthServerUrl();
            String path = kc.getPathPrefix() + "/" + kc.getRealm() + "/" + kc.getPathSuffix();

            WebTarget target = client.target(url).path(path);

            Boolean isUserAllowedAccess = false;

            try {
                Response kcResponse = target
                        .request()
                        .header("Authorization", "Bearer " + headerAuthToken)
                        .get();

                String entityResponse = kcResponse.readEntity(String.class);

                JSONParser parser = new JSONParser();
                JSONObject users = (JSONObject) parser.parse(entityResponse);
                userID = users.get("sub").toString();

                ((HttpServletRequest) request).getHeader("Authorization");

                MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(httpServletRequest);

                mutableRequest.putHeader("user_id", userID);

                if (kcResponse.getStatus() == HttpStatus.SC_OK) { // user is authorized in keycloak, so get the user record and ID associated with the token
               /* String entityResponse = kcResponse.readEntity(String.class);
                JSONParser parser = new JSONParser();
                JSONObject users = (JSONObject) parser.parse(entityResponse);
                userID = users.get("sub").toString();
                logger.info("userId: "+userID);*/

                    isUserAllowedAccess = UserCache.getExternalUserApplicationAccess(userID, appName);
                    if (isUserAllowedAccess) {

                        chain.doFilter(mutableRequest, response);
                    } else {
                        httpServletResponse.sendError(403, "Access is Forbidden");
                    }

                } else { // user is not authorized with this token
                    httpServletResponse.sendError(403, "Access is Forbidden");
                    return;
                }

            } catch (Exception ex) {
                httpServletResponse.sendError(403, "Access is Forbidden");
                return;
            }

        }
    }

    @Override
    public void destroy() {

    }

    private KeycloakConfig getConfig() throws IOException {
        JsonNode jsonnode =  ConfigManager.getConfigurationAsJson("keycloak_filter","ex_access_filter");
        KeycloakConfig keycloakConfig = new KeycloakConfig();
        keycloakConfig.setAuthServerUrl(jsonnode.get("auth-server-url").asText());
        keycloakConfig.setRealm(jsonnode.get("realm").asText());
        keycloakConfig.setPathPrefix(jsonnode.get("path_prefix").asText());
        keycloakConfig.setPathSuffix(jsonnode.get("path_suffix").asText());
        return keycloakConfig;
    }
}


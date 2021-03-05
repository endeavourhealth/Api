package org.endeavourhealth.coreui.framework;

import org.endeavourhealth.core.database.dal.usermanager.caching.UserCache;
import org.endeavourhealth.core.database.rdbms.usermanager.models.UserProjectEntity;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppAccessFilter implements Filter {
    private static final Logger logger = Logger.getLogger(AppAccessFilter.class.getName());
    private static String appName = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        appName = filterConfig.getInitParameter("app_name");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        //check for authenticated user
        Principal principal = httpServletRequest.getUserPrincipal();
        if (principal != null) {

            //basic servlet request info
            String userId = httpServletRequest.getUserPrincipal().getName();

            String userProjectId = httpServletRequest.getHeader("userProjectId");

            String pathURI = httpServletRequest.getRequestURI();

            String appId = appName;

            // boolean isUserAllowedAccess = appId.equalsIgnoreCase("User Manager");
            Boolean isUserAllowedAccess = false;

            if (userProjectId == null && (pathURI.contains("getUserProfile") || pathURI.contains("getProjects"))) {
                isUserAllowedAccess = true;
            } else {
                try {

                    UserProjectEntity userProject = UserCache.getUserProject(userProjectId);
                    isUserAllowedAccess = UserCache.getUserProjectApplicationAccess(userId, userProject.getProjectId(), appId);

                } catch (Exception e) {
                    logger.log(Level.INFO, "Error: " + e.getMessage());
                    httpServletResponse.sendError(403, "Access is Forbidden");
                }
            }
            if (!isUserAllowedAccess) {

                httpServletResponse.sendError(403, "Access is Forbidden");
                return;

            } else {
                chain.doFilter(request, response);
            }
        } else {
            //continue valve pipeline
            chain.doFilter(request, response);
        }


    }

    @Override
    public void destroy() {

    }
}

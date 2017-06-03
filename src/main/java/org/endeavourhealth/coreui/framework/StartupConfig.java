package org.endeavourhealth.coreui.framework;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class StartupConfig implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(StartupConfig.class);

    public void contextInitialized(ServletContextEvent contextEvent) {
        LOG.trace("Attempt to get config id from context");
        String configId = contextEvent.getServletContext().getInitParameter("app_id");
        if (configId == null || configId.isEmpty()) {
            LOG.warn("Application id (app_id) not set in context file (web.xml), reverting to 'default'");
            configId = "default";
        }
        LOG.trace("ConfigId : [" + configId + "]");

        LOG.trace("Initializing configuration manager");
        try {
            ConfigManager.Initialize(configId);
        } catch (ConfigManagerException e) {
            LOG.error(e.getMessage());
        }
        LOG.trace("Configuration manager initialized");
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }


}

package org.endeavourhealth.coreui.framework;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

public final class StartupConfig implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(StartupConfig.class);

    private static final Map<String, ContextShutdownHook> shutdownHooks = new HashMap<>();

    public static void registerShutdownHook(String name, ContextShutdownHook hook){
        if (shutdownHooks.containsKey(name))
            throw new IllegalArgumentException("A hook with name "+name+" has already been registered");

        shutdownHooks.put(name, hook);
        LOG.trace("Registered shutdown hook ["+name+"]");
    }


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
        LOG.trace("Shutting down...");
        for (Map.Entry<String, ContextShutdownHook> entry : shutdownHooks.entrySet()) {
            LOG.trace("\tShutting down hook [" + entry.getKey() + "]");
            entry.getValue().contextShutdown();
        }
        LOG.trace("...Shutdown done.");
    }


}

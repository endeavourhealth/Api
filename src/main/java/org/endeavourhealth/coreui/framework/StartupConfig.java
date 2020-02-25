package org.endeavourhealth.coreui.framework;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.core.database.dal.usermanager.caching.CacheManager;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
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

        LOG.trace("Initializing configuration manager for app ID [" + configId + "]");
        try {
            ConfigManager.Initialize(configId);
        } catch (ConfigManagerException e) {
            LOG.error(e.getMessage());
        }
        LOG.trace("Configuration manager initialized");

        //now we've set our app name, we can start our heartbeat reporting to Graphite
        MetricsHelper.startHeartbeat();
        LOG.trace("Heartbeat metric started");
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
        LOG.trace("Shutting down...");

        cleanupConnectionManager();
        cleanupJDBCDrivers();
        cleanupShutdownHooks();
        cleanupCacheService();

        // Cleanup logback last
        cleanupConfigManagerLogback();

        // Only use console from this point
        System.out.println("...Shutdown done.");
    }

    private void cleanupShutdownHooks() {
        for (Map.Entry<String, ContextShutdownHook> entry : shutdownHooks.entrySet()) {
            LOG.trace("\tShutting down hook [" + entry.getKey() + "]");
            entry.getValue().contextShutdown();
        }
    }

    private void cleanupConnectionManager() {
        LOG.trace("Shutting down connection manager...");
        ConnectionManager.shutdown();
        LOG.trace("Connection manager shutdown done...");

        LOG.trace("Shutting down UM connection manager...");
        // org.endeavourhealth.core.um.models.ConnectionManager.shutdown();
        LOG.trace("Connection manager shutdown done...");
    }

    private void cleanupCacheService() {
        LOG.trace("Shutting down cache service...");
        CacheManager.stopScheduler();
        LOG.trace("cache service shutdown done...");
    }

    private void cleanupJDBCDrivers() {
        LOG.trace("Cleaning up JDBC drivers...");

        LOG.info("Calling MySQL AbandonedConnectionCleanupThread shutdown");
        AbandonedConnectionCleanupThread.checkedShutdown();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();

            if (driver.getClass().getClassLoader() == cl) {

                try {
                    LOG.info("Deregistering JDBC driver {}", driver);
                    DriverManager.deregisterDriver(driver);

                } catch (SQLException ex) {
                    LOG.error("Error deregistering JDBC driver {}", driver, ex);
                }

            } else {
                LOG.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
            }
        }
    }

    private void cleanupConfigManagerLogback() {
        System.out.println("Cleaning up ConfigManager/Logback...");
        ConfigManager.shutdownLogback();
        System.out.println("ConfigManager/Logback cleanup done.");
    }

}

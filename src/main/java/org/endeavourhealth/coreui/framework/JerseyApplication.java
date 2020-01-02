package org.endeavourhealth.coreui.framework;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * this class registers the MetricRegistry within the servlet container so the @Timed annotations
 * work in the endpoint classes. See https://metrics.dropwizard.io/4.1.2/manual/jersey.html
 *
 * Note that this class will only be instantiated if referenced in web.xml
 */
public class JerseyApplication extends ResourceConfig {
    private static final Logger LOG = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {

        //simply register the MetricsRegistry (which is already configured to connect to Graphite) with the app
        MetricRegistry registry = MetricsHelper.getRegistry();
        register(new InstrumentedResourceMethodApplicationListener(registry));
    }
}

package org.endeavourhealth.coreui.framework;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;
import org.endeavourhealth.common.utility.MetricsHelper;

/**
 * servlet listener to capture general metrics on Tomcat endpoint calls, such as total count.
 *
 * Note that the details metrics on individual endpoints, captured by the @Timed annotation, have nothing
 * to do with this class (see InstrumentedFilterContextListener)
 */
public class MetricsContextListener extends InstrumentedFilterContextListener {

    @Override
    protected MetricRegistry getMetricRegistry() {
        return MetricsHelper.getRegistry();
    }
}


package play.modules.metrics.reporter;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import play.Play;
import play.modules.metrics.MetricsPlugin;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteReporter.Builder;

public class GraphiteReporterAdapter extends ScheduledReporterAdapter<GraphiteReporter> {

    private Builder builder;

    public GraphiteReporterAdapter() {
        super(readLongFromConf(GraphiteReporter.class, ScheduledReporterAdapter.INTERVAL, 1), 
            readTimeUnitFromConf(GraphiteReporter.class, INTERVAL_UNIT, TimeUnit.SECONDS),
            readBooleanFromConf(GraphiteReporter.class, ENABLED, false));
        
        this.builder = GraphiteReporter.forRegistry(MetricsPlugin.PLUGIN_REGISTRY);
    }

    @Override
    protected ScheduledReporter build() {
        // TODO Auto-generated method stub
        Graphite graphite = new Graphite(new InetSocketAddress(Play.configuration.getProperty(
            "graphite.ip", "192.168.1.101"), readIntFromConf(GraphiteReporter.class, "graphite.id", 2003)));
        
        return builder.prefixedWith(
            readStringFromConf(GraphiteReporter.class, "prefix", "play.modules.metrics.graphite"))
        .convertRatesTo(readTimeUnitFromConf(GraphiteReporter.class, RATES_UNIT, TimeUnit.SECONDS))
        .convertDurationsTo(
            readTimeUnitFromConf(GraphiteReporter.class, DURATION_UNIT, TimeUnit.MILLISECONDS))
        .filter(MetricFilter.ALL).build(graphite);
    }

}

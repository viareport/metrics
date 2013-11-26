package play.modules.metrics.reporter;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import play.modules.metrics.MetricsPlugin;

import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.Builder;

public class Slf4jReporterAdapter extends ScheduledReporterAdapter<Slf4jReporter> {

    private Builder builder;

    public Slf4jReporterAdapter() {
        super(readLongFromConf(Slf4jReporter.class, ScheduledReporterAdapter.INTERVAL, 1), 
              readTimeUnitFromConf(Slf4jReporter.class, INTERVAL_UNIT, TimeUnit.SECONDS),
              readBooleanFromConf(Slf4jReporter.class, ENABLED, false));
        this.builder = Slf4jReporter.forRegistry(MetricsPlugin.PLUGIN_REGISTRY);
    }

    @Override
    protected ScheduledReporter build() {
        return builder.outputTo(LoggerFactory.getLogger("play.modules.metrics"))
            .convertRatesTo(readTimeUnitFromConf(Slf4jReporter.class, RATES_UNIT, TimeUnit.SECONDS))
            .convertDurationsTo(
                readTimeUnitFromConf(Slf4jReporter.class, DURATION_UNIT, TimeUnit.MILLISECONDS))
            .build();
    }
}

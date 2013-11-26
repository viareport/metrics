package play.modules.metrics.reporter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import play.modules.metrics.MetricsPlugin;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.ScheduledReporter;

public class CsvReporterAdapter extends ScheduledReporterAdapter<CsvReporter> {

    private static final String CSV_DIR = "csvDir";
    private CsvReporter.Builder builder;

    public CsvReporterAdapter() {
        super(readLongFromConf(CsvReporter.class, ScheduledReporterAdapter.INTERVAL, 1), 
            readTimeUnitFromConf(CsvReporter.class, INTERVAL_UNIT, TimeUnit.SECONDS),
            readBooleanFromConf(CsvReporter.class, ENABLED, false));
        this.builder = CsvReporter.forRegistry(MetricsPlugin.PLUGIN_REGISTRY);
    }
    @Override
    protected ScheduledReporter build() {
        // TODO Auto-generated method stub
        return builder.formatFor(Locale.US)
            .convertRatesTo(readTimeUnitFromConf(CsvReporter.class, RATES_UNIT, TimeUnit.SECONDS))
            .convertDurationsTo(
                readTimeUnitFromConf(CsvReporter.class, DURATION_UNIT, TimeUnit.MILLISECONDS))
            .build(new File(readStringFromConf(CsvReporter.class, CSV_DIR, "metrics")));
    }

}

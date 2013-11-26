package play.modules.metrics.reporter;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ScheduledReporter;

abstract class ScheduledReporterAdapter<R extends ScheduledReporter> extends
    ReporterAdapter<ScheduledReporter> {

    protected long period;
    protected TimeUnit unit;
    
    protected static final String INTERVAL_UNIT = "intervalUnit";
    protected static final String INTERVAL = "interval";

    protected static final String DURATION_UNIT = "durationUnit";
    protected static final String RATES_UNIT = "ratesUnit";
    
    public ScheduledReporterAdapter(long period, TimeUnit unit, boolean enabled) {
        super(enabled);
        this.period = period;
        this.unit = unit;
    }

    @Override
    protected void doStart() {
        this.reporter.start(period, unit);
    }

    @Override
    public void stop() {
        this.reporter.stop();
    }
}
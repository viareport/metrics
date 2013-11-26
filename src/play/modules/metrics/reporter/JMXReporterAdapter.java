package play.modules.metrics.reporter;

import play.modules.metrics.MetricsPlugin;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.JmxReporter.Builder;

class JMXReporterAdapter extends ReporterAdapter<JmxReporter> {

    private Builder builder;

    public JMXReporterAdapter() {
        super(readBooleanFromConf(JMXReporterAdapter.class, ENABLED, false));
        this.builder = JmxReporter.forRegistry(MetricsPlugin.PLUGIN_REGISTRY);
    }

    @Override
    protected void doStart() {
        this.reporter.start();
    }

    @Override
    public void stop() {
        this.reporter.stop();
    }

    @Override
    protected JmxReporter build() {
        return builder.build();
    }
}
package play.modules.metrics.reporter;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import play.Play;

abstract class ReporterAdapter<R> {

    protected R reporter;
    private boolean enabled = true;

    private static final String REPORTER_CONF_PREFIX = "metrics";

    protected static final String ENABLED = "enabled";

    public ReporterAdapter(boolean enabled) {
        this.enabled = enabled;
    }

    public void start() {
        init();
        if (this.enabled) {
            doStart();
        }
    }

    public Class getReporterClass() {
        init();
        return reporter.getClass();
    }

    protected final void init() {
        this.reporter = build();
    }
    
    protected abstract R build();
    
    protected abstract void doStart();

    public abstract void stop();

    public final void enable() {
        this.enabled = true;
        start();
    }
    
    public void disable() {
        this.enabled = false;
        stop();
    }
    
    static long readLongFromConf(Class reporterClass, String prop, long defaultValue) {
        String confString = getConfString(reporterClass, prop);
        if (!StringUtils.isBlank(confString)) {
            return Long.valueOf(confString);
        } else {
            return defaultValue;
        }
    }

    static boolean readBooleanFromConf(Class reporterClass, String prop, boolean defaultValue) {
        String confString = getConfString(reporterClass, prop);
        if (!StringUtils.isBlank(confString)) {
            return Boolean.valueOf(confString);
        } else {
            return defaultValue;
        }
    }

    static int readIntFromConf(Class reporterClass, String prop, int defaultValue) {
        String confString = getConfString(reporterClass, prop);
        if (!StringUtils.isBlank(confString)) {
            return Integer.valueOf(confString);
        } else {
            return defaultValue;
        }
    }

    static String readStringFromConf(Class reporterClass, String prop, String defaultValue) {
        return Play.configuration.getProperty(getConfKey(reporterClass, prop), defaultValue);
    }

    static TimeUnit readTimeUnitFromConf(Class reporterClass, String prop, TimeUnit defaultValue) {
        String confString = getConfString(reporterClass, prop);
        if (!StringUtils.isBlank(confString)) {
            if ("ms".equals(confString)) {
                return TimeUnit.MILLISECONDS;
            } else if ("s".equals(confString)) {
                return TimeUnit.SECONDS;
            } else if ("m".equals(confString)) {
                return TimeUnit.MINUTES;
            } else if ("h".equals(confString)) {
                return TimeUnit.HOURS;
            } else if ("d".equals(confString)) {
                return TimeUnit.DAYS;
            } else {
                throw new IllegalArgumentException(
                    String.format("%s not supported for Time unit", confString));
            }
        } else {
            return defaultValue;
        }
    }
    
    static String getConfString(Class reporterClass, String prop) {
        return Play.configuration.getProperty(getConfKey(reporterClass, prop));
    }
    
    static String getConfKey(Class reporterClass, String prop) {
        return String.format("%s.%s.%s", REPORTER_CONF_PREFIX,
            ReporterPlugin.getReporterKey(reporterClass), prop);
    }
}
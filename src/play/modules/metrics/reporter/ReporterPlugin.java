package play.modules.metrics.reporter;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.util.Map;

import play.PlayPlugin;

import com.google.common.collect.Maps;

public class ReporterPlugin extends PlayPlugin {

    private static Map<String, ReporterAdapter> reporters = Maps.newHashMap();

    @Override
    public void onApplicationStart() {
        register(new JmxReporterAdapter());
        register(new CsvReporterAdapter());
        register(new Slf4jReporterAdapter());
        register(new GraphiteReporterAdapter());

        startAll();
    }

    public static void stopReporter(String reporter) {
        reporters.get(reporter).stop();
    }

    public static void startReporter(String reporter) {
        reporters.get(reporter).start();
    }


    public static void enable(String reporter) {
        reporters.get(reporter).enable();
    }

    public static void disable(String reporter) {
        reporters.get(reporter).disable();
    }
    
    private static void startAll() {
        for (ReporterAdapter<?> reporter : reporters.values()) {
            reporter.start();
        }
    }
    
    private static void register(ReporterAdapter<?> reporter) {
        reporters.put(getReporterKey(reporter.getReporterClass()), reporter);
    }
    
    static String getReporterKey(Class reporterClass) {
        return UPPER_CAMEL.to(LOWER_HYPHEN, reporterClass.getSimpleName());
    }

}

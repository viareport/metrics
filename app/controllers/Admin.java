package controllers;

import play.modules.metrics.MetricsPlugin;
import play.modules.metrics.reporter.ReporterPlugin;
import play.mvc.Controller;

public class Admin  extends Controller {
    
    public static void reset() {
        MetricsPlugin.reset();
    }
    
    public static void start(String reporter) {
        ReporterPlugin.startReporter(reporter);
    }
    
    public static void stop(String reporter) {
        ReporterPlugin.stopReporter(reporter);
    }
    
    public static void enable(String reporter) {
        ReporterPlugin.enable(reporter);
    }
    
    public static void disable(String reporter) {
        ReporterPlugin.disable(reporter);
    }
}

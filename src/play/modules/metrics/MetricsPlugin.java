package play.modules.metrics;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class MetricsPlugin extends PlayPlugin {
    public static class TimerFactory {

        private String appNameSpace;

        public TimerFactory(String appNameSpace) {
            this.appNameSpace = appNameSpace;
        }

    }

    public static final MetricRegistry PLUGIN_REGISTRY = new MetricRegistry();
    protected static ThreadLocal<LinkedList<Context>> methodTimerContexts = new ThreadLocal<LinkedList<Context>>();
    protected static ThreadLocal<Integer> callDepth = new ThreadLocal<Integer>();

    
    @Override
    public void onApplicationStart() {
        JmxReporter reporter = JmxReporter.forRegistry(PLUGIN_REGISTRY).build();
        final CsvReporter csvReporter = CsvReporter.forRegistry(PLUGIN_REGISTRY)
            .formatFor(Locale.US)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build(new File("metrics/"));
        csvReporter.start(100, TimeUnit.MILLISECONDS);
        reporter.start();
        csvReporter.stop();
    }

    public static Timer getTimer(String namespaceToken, String ... namespaceTokens) {
        String timerName = PLUGIN_REGISTRY.name(namespaceToken, namespaceTokens);
        return PLUGIN_REGISTRY.timer(timerName);
    }
    
    @Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
//        new MetricsModelEnhancer().enhanceThisClass(applicationClass);
          new MetricAnnotationEnhancer().enhanceThisClass(applicationClass);
          //new MetricsAllEnhancer().enhanceThisClass(applicationClass);
    }
    
    private static String getIndentation(int depth) {
        StringBuffer sBuf = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            sBuf.append("-");
        }
        return sBuf.toString();
    }
    
    public static void enableTimer(String group, String type, String timerName) {
        int currentCallDepth = getCallDepth();
        //System.out.println(getIndentation(currentCallDepth) + "->Start::"+ timerName);
//        System.out.println("Activating metrics timer " + timerName);
        Timer timer = getTimer(group, type, timerName);
        LinkedList<Context> timerContexts =  methodTimerContexts.get();
        if (timerContexts == null) {
            timerContexts = new LinkedList<Context>();
            methodTimerContexts.set(timerContexts);
//            System.out.println("Metrics timer activated " + timerName);
        }
        
        timerContexts.addFirst(timer.time());
        callDepth.set(++currentCallDepth);
    }

    private static Integer getCallDepth() {
        Integer callDepthValue = callDepth.get();
        if (callDepthValue == null) {
            callDepthValue = new Integer(0);
            callDepth.set(callDepthValue);
        }
        return callDepthValue;
    }
    
    public static void stopTimer(String timerName) {
        int currentCallDepth = getCallDepth();
       // System.out.println(getIndentation(--currentCallDepth) + "->Stop::"+ timerName);
        LinkedList<Context> timerContexts =  methodTimerContexts.get();
        if (timerContexts != null) {
            timerContexts.pop().stop();
//            System.out.println("Metrics timer stopped");
        }
        callDepth.set(currentCallDepth);
    }

    public static MetricRegistry getRegistry() {
        return PLUGIN_REGISTRY;
    }

    public static void reset() {
        for (String timerName: PLUGIN_REGISTRY.getTimers().keySet()) {
            PLUGIN_REGISTRY.remove(timerName);
        }
    }
}

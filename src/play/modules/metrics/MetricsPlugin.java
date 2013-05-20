package play.modules.metrics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        public Timer createTimer(String group, String type, String timerName) {
            return PLUGIN_REGISTRY.timer(timerName);
        }
    }
    
    private final static Map<String, Timer> reponseTimers = new HashMap<String, Timer>();
    private final static Map<String, Timer> methodTimers = new HashMap<String, Timer>();
    private static final MetricRegistry PLUGIN_REGISTRY = new MetricRegistry();
    protected static ThreadLocal<Context> reponseTimerContext = new ThreadLocal<Context>();
    protected static ThreadLocal<LinkedList<Context>> methodTimerContexts = new ThreadLocal<LinkedList<Context>>();
    protected static ThreadLocal<Integer> callDepth = new ThreadLocal<Integer>();
    
    private static TimerFactory TIMER_FACTORY;
    
    @Override
    public void onApplicationStart() {
        initTimerFactory();
        JmxReporter reporter = JmxReporter.forRegistry(PLUGIN_REGISTRY).build();
        final CsvReporter csvReporter = CsvReporter.forRegistry(PLUGIN_REGISTRY)
            .formatFor(Locale.US)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build(new File("metrics/"));
        csvReporter.start(1, TimeUnit.SECONDS);
        reporter.start();
//        System.out.println("Metrics starts");
    }

    private static void initTimerFactory() {
        String applicationName = Play.applicationPath.toString().replace("/", ".");
        TIMER_FACTORY = new TimerFactory(applicationName);
    }
    
    @Override
    public void beforeActionInvocation(Method actionMethod) {
        String actionName = actionMethod.getName();
        Timer responseTimer = getTimer("inativ.com.requests.timer", actionMethod.getDeclaringClass().getSimpleName(), actionName, reponseTimers);    
        reponseTimerContext.set(responseTimer.time());
    }

    private static Timer getTimer(String group, String type, String timerName, Map<String, Timer> timerRegistry) {
        Timer responseTimer = timerRegistry.get(timerName);
        
        if (responseTimer == null) {
            responseTimer =  getTimerFactory().createTimer(group, type, timerName);
            timerRegistry.put(timerName, responseTimer);
        }
        
        return responseTimer;
    }

    private static TimerFactory getTimerFactory() {
        if (TIMER_FACTORY == null) {
            initTimerFactory();
        }
        return TIMER_FACTORY;
    }
    
    @Override
    public void afterActionInvocation() {
        Context responseTimerCtx = reponseTimerContext.get();
        
        if (responseTimerCtx != null)
            responseTimerCtx.stop();
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
        Timer timer = getTimer(group, type, timerName, methodTimers);
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
}
